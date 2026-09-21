package com.george.backgammon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BackgammonBoardView extends View {
    public interface OnGameChangedListener { void onGameChanged(); }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private BackgammonGame game;
    private OnGameChangedListener listener;
    private int selectedFrom = Integer.MIN_VALUE;

    private float left, top, boardRight, bottom, offLeft, barLeft, barRight, colWidth, triangleHeight;

    public BackgammonBoardView(Context context, AttributeSet attrs) { super(context, attrs); }

    public void setGame(BackgammonGame game) { this.game = game; invalidate(); }
    public void setOnGameChangedListener(OnGameChangedListener l) { listener = l; }
    public void clearSelection() { selectedFrom = Integer.MIN_VALUE; invalidate(); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (game == null) return;
        computeGeometry();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF9A6A3A);
        c.drawRoundRect(new RectF(left, top, boardRight, bottom), 18, 18, paint);

        paint.setColor(0xFF3B2418);
        c.drawRect(barLeft, top, barRight, bottom, paint);

        for (int i = 0; i < 12; i++) {
            float x0 = columnX(i);
            float x1 = x0 + colWidth;
            int colorTop = (i % 2 == 0) ? 0xFFE2B96E : 0xFF6E2E25;
            int colorBottom = (i % 2 == 0) ? 0xFF6E2E25 : 0xFFE2B96E;
            drawTriangle(c, x0, x1, top, true, colorTop);
            drawTriangle(c, x0, x1, bottom, false, colorBottom);
        }

        drawMoveHints(c);
        for (int p = 1; p <= 24; p++) drawCheckersAtPoint(c, p, game.getPoint(p));
        drawBar(c);
        drawOffTray(c);
        drawDice(c);
    }

    private void computeGeometry() {
        float w = getWidth(), h = getHeight();
        float pad = Math.max(8f, h * 0.025f);
        left = pad;
        top = pad;
        bottom = h - pad;
        float offWidth = Math.max(82f, w * 0.095f);
        offLeft = w - pad - offWidth;
        boardRight = offLeft - pad;
        float barWidth = Math.max(36f, (boardRight - left) * 0.055f);
        colWidth = (boardRight - left - barWidth) / 12f;
        barLeft = left + 6f * colWidth;
        barRight = barLeft + barWidth;
        triangleHeight = (bottom - top) * 0.43f;
    }

    private float columnX(int visualIndex) {
        return left + visualIndex * colWidth + (visualIndex >= 6 ? (barRight - barLeft) : 0);
    }

    private void drawTriangle(Canvas c, float x0, float x1, float edgeY, boolean down, int color) {
        Path path = new Path();
        path.moveTo(x0, edgeY);
        path.lineTo(x1, edgeY);
        path.lineTo((x0 + x1) / 2f, edgeY + (down ? triangleHeight : -triangleHeight));
        path.close();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        c.drawPath(path, paint);
    }

    private void drawCheckersAtPoint(Canvas c, int point, int value) {
        if (value == 0) return;
        boolean topPoint = point >= 13;
        int visualIndex = topPoint ? point - 13 : 12 - point;
        float cx = columnX(visualIndex) + colWidth / 2f;
        float r = Math.min(colWidth * 0.42f, (bottom - top) * 0.055f);
        int count = Math.abs(value);
        float spacing = Math.min(r * 1.78f, triangleHeight / Math.max(5, count));
        for (int i = 0; i < count; i++) {
            float cy = topPoint ? top + r + i * spacing : bottom - r - i * spacing;
            drawChecker(c, cx, cy, r, value > 0, selectedFrom == point && i == count - 1);
        }
        if (count > 5) {
            float cy = topPoint ? top + r + 4 * spacing : bottom - r - 4 * spacing;
            drawCount(c, cx, cy, count);
        }
    }

    private void drawChecker(Canvas c, float cx, float cy, float r, boolean white, boolean selected) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(white ? 0xFFF6EAD2 : 0xFF57251F);
        c.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(selected ? 5f : 2f);
        paint.setColor(selected ? 0xFFFFD768 : (white ? 0xFF6E5A45 : 0xFFD6A87C));
        c.drawCircle(cx, cy, r, paint);
    }

    private void drawCount(Canvas c, float x, float y, int count) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF17130F);
        paint.setTextSize(Math.max(14f, colWidth * 0.3f));
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(String.valueOf(count), x, y + paint.getTextSize() * 0.35f, paint);
    }

    private void drawBar(Canvas c) {
        float cx = (barLeft + barRight) / 2f;
        float r = Math.min(colWidth * 0.38f, (barRight - barLeft) * 0.42f);
        if (game.getWhiteBar() > 0) {
            float cy = top + (bottom - top) * 0.38f;
            drawChecker(c, cx, cy, r, true, selectedFrom == BackgammonGame.BAR && game.getCurrentPlayer() == BackgammonGame.WHITE);
            drawCount(c, cx, cy, game.getWhiteBar());
        }
        if (game.getBlackBar() > 0) {
            float cy = top + (bottom - top) * 0.62f;
            drawChecker(c, cx, cy, r, false, selectedFrom == BackgammonGame.BAR && game.getCurrentPlayer() == BackgammonGame.BLACK);
            drawCount(c, cx, cy, game.getBlackBar());
        }
    }

    private void drawOffTray(Canvas c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF2B211A);
        c.drawRoundRect(new RectF(offLeft, top, getWidth() - 8, bottom), 14, 14, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(0xFFF6EAD2);
        paint.setTextSize(Math.max(12f, (bottom - top) * 0.04f));
        float cx = (offLeft + getWidth() - 8) / 2f;
        c.drawText("WHITE OFF", cx, top + 28, paint);
        c.drawText(String.valueOf(game.getWhiteOff()), cx, top + 55, paint);
        paint.setColor(0xFFE0A58B);
        c.drawText("BLACK OFF", cx, bottom - 37, paint);
        c.drawText(String.valueOf(game.getBlackOff()), cx, bottom - 10, paint);
    }

    private void drawDice(Canvas c) {
        if (!game.hasRolled()) return;
        float size = Math.min((barRight - barLeft) * 0.78f, (bottom - top) * 0.09f);
        float cx = (barLeft + barRight) / 2f;
        drawDie(c, cx, (top + bottom) / 2f - size * 0.62f, size, game.getDieOne());
        drawDie(c, cx, (top + bottom) / 2f + size * 0.62f, size, game.getDieTwo());
    }

    private void drawDie(Canvas c, float cx, float cy, float size, int value) {
        RectF r = new RectF(cx - size/2, cy - size/2, cx + size/2, cy + size/2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFF4E7CC);
        c.drawRoundRect(r, size * 0.16f, size * 0.16f, paint);
        paint.setColor(0xFF2C211B);
        float pr = size * 0.075f;
        float dx = size * 0.22f, dy = size * 0.22f;
        if (value % 2 == 1) c.drawCircle(cx, cy, pr, paint);
        if (value >= 2) { c.drawCircle(cx-dx, cy-dy, pr, paint); c.drawCircle(cx+dx, cy+dy, pr, paint); }
        if (value >= 4) { c.drawCircle(cx+dx, cy-dy, pr, paint); c.drawCircle(cx-dx, cy+dy, pr, paint); }
        if (value == 6) { c.drawCircle(cx-dx, cy, pr, paint); c.drawCircle(cx+dx, cy, pr, paint); }
    }

    private void drawMoveHints(Canvas c) {
        if (!game.hasRolled()) return;
        List<BackgammonGame.Move> moves = game.getAllowedFirstMoves();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x8833D17A);
        for (BackgammonGame.Move m : moves) {
            if (selectedFrom != Integer.MIN_VALUE && m.from != selectedFrom) continue;
            if (m.to == BackgammonGame.OFF_WHITE || m.to == BackgammonGame.OFF_BLACK) {
                c.drawCircle((offLeft + getWidth() - 8) / 2f, (top + bottom) / 2f, 10, paint);
            } else {
                boolean topPoint = m.to >= 13;
                int idx = topPoint ? m.to - 13 : 12 - m.to;
                float cx = columnX(idx) + colWidth/2f;
                float cy = topPoint ? top + triangleHeight * 0.78f : bottom - triangleHeight * 0.78f;
                c.drawCircle(cx, cy, Math.max(7f, colWidth * 0.11f), paint);
            }
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || game == null || !game.hasRolled() || game.getWinner() != 0) return true;
        float x = event.getX(), y = event.getY();

        if (x >= offLeft) {
            if (selectedFrom != Integer.MIN_VALUE) tryDestination(game.getCurrentPlayer() == BackgammonGame.WHITE ? BackgammonGame.OFF_WHITE : BackgammonGame.OFF_BLACK);
            return true;
        }

        if (x >= barLeft && x <= barRight) {
            if (hasMoveFrom(BackgammonGame.BAR)) selectedFrom = BackgammonGame.BAR;
            invalidate();
            return true;
        }

        int point = hitPoint(x, y);
        if (point == 0) return true;
        if (selectedFrom != Integer.MIN_VALUE && tryDestination(point)) return true;
        if (hasMoveFrom(point)) selectedFrom = point;
        else selectedFrom = Integer.MIN_VALUE;
        invalidate();
        return true;
    }

    private int hitPoint(float x, float y) {
        if (x < left || x > boardRight || y < top || y > bottom || (x >= barLeft && x <= barRight)) return 0;
        int idx;
        if (x < barLeft) idx = (int)((x - left) / colWidth);
        else idx = 6 + (int)((x - barRight) / colWidth);
        if (idx < 0 || idx > 11) return 0;
        if (y < (top + bottom)/2f) return 13 + idx;
        return 12 - idx;
    }

    private boolean hasMoveFrom(int from) {
        for (BackgammonGame.Move m : game.getAllowedFirstMoves()) if (m.from == from) return true;
        return false;
    }

    private boolean tryDestination(int to) {
        List<BackgammonGame.Move> candidates = new ArrayList<>();
        for (BackgammonGame.Move m : game.getAllowedFirstMoves()) {
            if (m.from == selectedFrom && m.to == to) candidates.add(m);
        }
        if (candidates.isEmpty()) return false;

        // Same destination can occasionally be reached with different dice. Prefer the larger die;
        // the engine has already filtered moves that would violate the maximum-dice rule.
        BackgammonGame.Move chosen = candidates.get(0);
        for (BackgammonGame.Move m : candidates) if (m.die > chosen.die) chosen = m;
        if (game.applyMove(chosen)) {
            selectedFrom = Integer.MIN_VALUE;
            if (listener != null) listener.onGameChanged();
            invalidate();
            return true;
        }
        return false;
    }
}
