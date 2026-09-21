package com.george.backgammon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-down premium wood/leather board inspired by the selected visual concept.
 * Game pieces and move indicators remain live Canvas elements rather than a baked background image.
 */
public class BackgammonBoardView extends View {
    public interface OnGameChangedListener { void onGameChanged(); }

    private static final int NO_SELECTION = Integer.MIN_VALUE;
    private static final int INVALID_NONE = Integer.MIN_VALUE;
    private static final int INVALID_OFF = Integer.MAX_VALUE;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint texturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private BackgammonGame game;
    private OnGameChangedListener listener;
    private int selectedFrom = NO_SELECTION;
    private int invalidDestination = INVALID_NONE;

    private float outerLeft, outerTop, outerRight, outerBottom;
    private float fieldLeft, fieldTop, fieldRight, fieldBottom;
    private float offLeft, offRight, barLeft, barRight, colWidth, triangleHeight;
    private float frame;

    public BackgammonBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setFocusable(true);
    }

    public void setGame(BackgammonGame game) { this.game = game; invalidate(); }
    public void setOnGameChangedListener(OnGameChangedListener l) { listener = l; }
    public void clearSelection() { selectedFrom = NO_SELECTION; clearInvalid(); invalidate(); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (game == null || getWidth() <= 0 || getHeight() <= 0) return;
        computeGeometry();

        drawBoardShell(c);
        drawPlayingField(c);
        drawPoints(c);
        drawCentralBar(c);
        drawOffTray(c);
        drawDice(c);
        drawMoveHints(c);

        // Pieces are deliberately drawn after destination glows so the board remains readable.
        for (int p = 1; p <= 24; p++) drawCheckersAtPoint(c, p, game.getPoint(p));
        drawBarCheckers(c);
        drawInvalidFeedback(c);
    }

    private void computeGeometry() {
        float w = getWidth(), h = getHeight();
        float pad = Math.max(5f, Math.min(w, h) * 0.012f);
        outerLeft = pad;
        outerTop = pad;
        outerRight = w - pad;
        outerBottom = h - pad;

        frame = Math.max(10f, h * 0.035f);
        float offWidth = Math.max(45f, w * 0.055f);
        offRight = outerRight - frame * 0.42f;
        offLeft = offRight - offWidth;

        fieldLeft = outerLeft + frame;
        fieldTop = outerTop + frame;
        fieldRight = offLeft - frame * 0.55f;
        fieldBottom = outerBottom - frame;

        float playWidth = fieldRight - fieldLeft;
        float barWidth = Math.max(34f, playWidth * 0.052f);
        colWidth = (playWidth - barWidth) / 12f;
        barLeft = fieldLeft + 6f * colWidth;
        barRight = barLeft + barWidth;
        triangleHeight = (fieldBottom - fieldTop) * 0.43f;
    }

    private float columnX(int visualIndex) {
        return fieldLeft + visualIndex * colWidth + (visualIndex >= 6 ? (barRight - barLeft) : 0);
    }

    private void drawBoardShell(Canvas c) {
        RectF outer = new RectF(outerLeft, outerTop, outerRight, outerBottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(outerLeft, outerTop, outerRight, outerBottom,
                new int[]{0xFF3E1E0E, 0xFF87502B, 0xFF4A2411, 0xFF9A5C31, 0xFF32170B},
                null, Shader.TileMode.CLAMP));
        c.drawRoundRect(outer, 18f, 18f, paint);
        paint.setShader(null);

        // Brass/gold inner edge.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.4f, frame * 0.11f));
        paint.setColor(0xFFB98546);
        c.drawRoundRect(new RectF(outerLeft + 3, outerTop + 3, outerRight - 3, outerBottom - 3), 16, 16, paint);

        // Fine wood grain for depth, intentionally subtle.
        texturePaint.setStyle(Paint.Style.STROKE);
        texturePaint.setStrokeWidth(1f);
        texturePaint.setColor(0x20401808);
        for (int i = 0; i < 18; i++) {
            float y = outerTop + (outerBottom - outerTop) * (i + 1) / 19f;
            float wobble = (i % 3 - 1) * 2.2f;
            c.drawLine(outerLeft + 8, y, outerRight - 8, y + wobble, texturePaint);
        }
    }

    private void drawPlayingField(Canvas c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(fieldLeft, fieldTop, fieldLeft, fieldBottom,
                0xFF2D211A, 0xFF1D1510, Shader.TileMode.CLAMP));
        c.drawRect(fieldLeft, fieldTop, fieldRight, fieldBottom, paint);
        paint.setShader(null);

        // Very soft leather texture.
        texturePaint.setStrokeWidth(1f);
        texturePaint.setColor(0x0EFFFFFF);
        for (int i = 0; i < 24; i++) {
            float y = fieldTop + (fieldBottom - fieldTop) * i / 24f;
            c.drawLine(fieldLeft, y, fieldRight, y + ((i & 1) == 0 ? 2f : -2f), texturePaint);
        }
    }

    private void drawPoints(Canvas c) {
        for (int i = 0; i < 12; i++) {
            float x0 = columnX(i);
            float x1 = x0 + colWidth;
            boolean even = (i % 2 == 0);
            drawTriangle(c, x0, x1, fieldTop, true, even ? 0xFFE9D7B7 : 0xFF8B4A28);
            drawTriangle(c, x0, x1, fieldBottom, false, even ? 0xFF8B4A28 : 0xFFE9D7B7);
        }
    }

    private void drawTriangle(Canvas c, float x0, float x1, float edgeY, boolean down, int baseColor) {
        float tipY = edgeY + (down ? triangleHeight : -triangleHeight);
        Path path = new Path();
        path.moveTo(x0 + 1f, edgeY);
        path.lineTo(x1 - 1f, edgeY);
        path.lineTo((x0 + x1) / 2f, tipY);
        path.close();

        int lighter = lighten(baseColor, 0.13f);
        int darker = darken(baseColor, 0.18f);
        paint.setShader(new LinearGradient(0, edgeY, 0, tipY,
                down ? lighter : darker,
                down ? darker : lighter,
                Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        c.drawPath(path, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.1f);
        paint.setColor(baseColor == 0xFFE9D7B7 ? 0xFFBCA47D : 0xFF5B2A16);
        c.drawPath(path, paint);
    }

    private void drawCentralBar(Canvas c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(barLeft, 0, barRight, 0,
                new int[]{0xFF4B2512, 0xFF93562E, 0xFF3A1B0D}, null, Shader.TileMode.CLAMP));
        c.drawRect(barLeft, fieldTop, barRight, fieldBottom, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(0xFFB98546);
        c.drawLine(barLeft + 2, fieldTop, barLeft + 2, fieldBottom, paint);
        c.drawLine(barRight - 2, fieldTop, barRight - 2, fieldBottom, paint);

        drawCompass(c, (barLeft + barRight) / 2f, (fieldTop + fieldBottom) / 2f,
                Math.min(barRight - barLeft, fieldBottom - fieldTop) * 0.34f);
    }

    private void drawCompass(Canvas c, float cx, float cy, float r) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.4f, r * 0.055f));
        paint.setColor(0xFFD5A85E);
        Path star = new Path();
        for (int i = 0; i < 16; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 8;
            float rr = (i % 2 == 0) ? r : r * 0.34f;
            float x = cx + (float)Math.cos(a) * rr;
            float y = cy + (float)Math.sin(a) * rr;
            if (i == 0) star.moveTo(x, y); else star.lineTo(x, y);
        }
        star.close();
        c.drawPath(star, paint);
        c.drawCircle(cx, cy, r * 0.14f, paint);
    }

    private void drawOffTray(Canvas c) {
        RectF tray = new RectF(offLeft + 4, fieldTop, offRight, fieldBottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF160F0B);
        c.drawRoundRect(tray, 10, 10, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f);
        paint.setColor(0xFF7B4A2B);
        c.drawRoundRect(tray, 10, 10, paint);

        float cx = (offLeft + offRight) / 2f;
        float mid = (fieldTop + fieldBottom) / 2f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD));
        paint.setTextSize(Math.max(11f, (offRight - offLeft) * 0.24f));
        paint.setColor(0xFFD9B978);
        c.drawText("OFF", cx, mid - 8, paint);

        drawOffCount(c, cx, fieldTop + (fieldBottom - fieldTop) * 0.22f, game.getWhiteOff(), true);
        drawOffCount(c, cx, fieldBottom - (fieldBottom - fieldTop) * 0.17f, game.getBlackOff(), false);
    }

    private void drawOffCount(Canvas c, float cx, float cy, int count, boolean white) {
        float r = Math.min((offRight - offLeft) * 0.28f, 18f);
        if (count > 0) {
            drawChecker(c, cx, cy, r, white, false);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
        paint.setTextSize(Math.max(12f, r * 0.92f));
        paint.setColor(white ? 0xFF2D2017 : 0xFFF0DFC2);
        if (count == 0) paint.setColor(0xFFD9B978);
        c.drawText(String.valueOf(count), cx, cy + paint.getTextSize() * 0.34f, paint);
    }

    private void drawCheckersAtPoint(Canvas c, int point, int value) {
        if (value == 0) return;
        boolean topPoint = point >= 13;
        int visualIndex = topPoint ? point - 13 : 12 - point;
        float cx = columnX(visualIndex) + colWidth / 2f;
        float r = checkerRadius();
        int count = Math.abs(value);
        float spacing = checkerSpacing(count, r);
        for (int i = 0; i < count; i++) {
            float cy = topPoint ? fieldTop + r + i * spacing : fieldBottom - r - i * spacing;
            boolean selected = selectedFrom == point && i == count - 1;
            drawChecker(c, cx, cy, r, value > 0, selected);
        }
        if (count > 5) {
            float cy = topPoint ? fieldTop + r + 4 * spacing : fieldBottom - r - 4 * spacing;
            drawCount(c, cx, cy, count, value > 0);
        }
    }

    private float checkerRadius() {
        return Math.min(colWidth * 0.41f, (fieldBottom - fieldTop) * 0.057f);
    }

    private float checkerSpacing(int count, float r) {
        return Math.min(r * 1.73f, triangleHeight / Math.max(5, count));
    }

    private void drawChecker(Canvas c, float cx, float cy, float r, boolean white, boolean selected) {
        float drawY = selected ? cy - Math.max(2f, r * 0.10f) : cy;

        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x70000000);
        paint.setShadowLayer(r * 0.20f, 0, r * 0.15f, 0x99000000);
        c.drawCircle(cx, drawY, r * 1.02f, paint);
        paint.clearShadowLayer();

        int center = white ? 0xFFFFF8E8 : 0xFF7A4328;
        int edge = white ? 0xFFD5C4A7 : 0xFF2B130B;
        paint.setShader(new RadialGradient(cx - r * 0.28f, drawY - r * 0.32f, r * 1.25f,
                new int[]{lighten(center, 0.12f), center, edge},
                new float[]{0f, 0.52f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(cx, drawY, r, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.3f, r * 0.075f));
        paint.setColor(white ? 0xFFE7D6B9 : 0xFFB57852);
        c.drawCircle(cx, drawY, r * 0.82f, paint);

        // Small glossy highlight.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(white ? 0x58FFFFFF : 0x42FFFFFF);
        c.drawCircle(cx - r * 0.28f, drawY - r * 0.30f, r * 0.19f, paint);

        if (selected) {
            // Blue selection ring/glow: the one strong blue cue in normal play.
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(3f, r * 0.13f));
            paint.setColor(0xFF43AFFF);
            paint.setShadowLayer(r * 0.34f, 0, 0, 0xFF178DFF);
            c.drawCircle(cx, drawY, r * 1.10f, paint);
            paint.clearShadowLayer();
        }
    }

    private void drawCount(Canvas c, float x, float y, int count, boolean white) {
        float r = checkerRadius();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(white ? 0xFF2B2018 : 0xFFF8EBD6);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
        paint.setTextSize(Math.max(12f, r * 0.72f));
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(String.valueOf(count), x, y + paint.getTextSize() * 0.34f, paint);
    }

    private void drawBarCheckers(Canvas c) {
        float cx = (barLeft + barRight) / 2f;
        float r = Math.min(checkerRadius() * 0.92f, (barRight - barLeft) * 0.42f);
        if (game.getWhiteBar() > 0) {
            float cy = fieldTop + (fieldBottom - fieldTop) * 0.38f;
            drawChecker(c, cx, cy, r, true,
                    selectedFrom == BackgammonGame.BAR && game.getCurrentPlayer() == BackgammonGame.WHITE);
            drawCount(c, cx, cy, game.getWhiteBar(), true);
        }
        if (game.getBlackBar() > 0) {
            float cy = fieldTop + (fieldBottom - fieldTop) * 0.62f;
            drawChecker(c, cx, cy, r, false,
                    selectedFrom == BackgammonGame.BAR && game.getCurrentPlayer() == BackgammonGame.BLACK);
            drawCount(c, cx, cy, game.getBlackBar(), false);
        }
    }

    private void drawDice(Canvas c) {
        if (!game.hasRolled()) return;
        float size = Math.min(colWidth * 0.70f, (fieldBottom - fieldTop) * 0.095f);
        float gap = size * 0.34f;
        float total = size * 2 + gap;
        float cx = fieldLeft + (fieldRight - fieldLeft) * 0.68f;
        float cy = (fieldTop + fieldBottom) / 2f;
        drawDie(c, cx - total * 0.27f, cy, size, game.getDieOne());
        drawDie(c, cx + total * 0.27f, cy, size, game.getDieTwo());
    }

    private void drawDie(Canvas c, float cx, float cy, float size, int value) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x75000000);
        paint.setShadowLayer(size * 0.16f, 0, size * 0.11f, 0xBB000000);
        c.drawRoundRect(new RectF(cx - size * 0.48f, cy - size * 0.43f,
                cx + size * 0.52f, cy + size * 0.57f), size * 0.17f, size * 0.17f, paint);
        paint.clearShadowLayer();

        RectF r = new RectF(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);
        paint.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom,
                0xFFFFFFFF, 0xFFE5D6BD, Shader.TileMode.CLAMP));
        c.drawRoundRect(r, size * 0.16f, size * 0.16f, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, size * 0.035f));
        paint.setColor(0xFFC6B18C);
        c.drawRoundRect(r, size * 0.16f, size * 0.16f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF241A14);
        float pr = size * 0.072f;
        float dx = size * 0.22f, dy = size * 0.22f;
        if (value % 2 == 1) c.drawCircle(cx, cy, pr, paint);
        if (value >= 2) { c.drawCircle(cx - dx, cy - dy, pr, paint); c.drawCircle(cx + dx, cy + dy, pr, paint); }
        if (value >= 4) { c.drawCircle(cx + dx, cy - dy, pr, paint); c.drawCircle(cx - dx, cy + dy, pr, paint); }
        if (value == 6) { c.drawCircle(cx - dx, cy, pr, paint); c.drawCircle(cx + dx, cy, pr, paint); }
    }

    /** Shows destinations only after the player selects a checker. No arrows/lines. */
    private void drawMoveHints(Canvas c) {
        if (!game.hasRolled() || selectedFrom == NO_SELECTION) return;
        List<BackgammonGame.Move> moves = game.getAllowedFirstMoves();
        List<Integer> destinations = new ArrayList<>();
        for (BackgammonGame.Move m : moves) {
            if (m.from == selectedFrom && !destinations.contains(m.to)) destinations.add(m.to);
        }

        for (int to : destinations) {
            if (to == BackgammonGame.OFF_WHITE || to == BackgammonGame.OFF_BLACK) {
                drawValidRing(c, (offLeft + offRight) / 2f, (fieldTop + fieldBottom) / 2f,
                        Math.min((offRight - offLeft) * 0.31f, checkerRadius() * 0.76f));
            } else {
                float[] center = destinationCenter(to);
                drawValidRing(c, center[0], center[1], checkerRadius() * 0.72f);
            }
        }
    }

    private float[] destinationCenter(int point) {
        boolean topPoint = point >= 13;
        int idx = topPoint ? point - 13 : 12 - point;
        float cx = columnX(idx) + colWidth / 2f;
        int count = Math.abs(game.getPoint(point));
        float r = checkerRadius();
        float spacing = checkerSpacing(Math.max(1, count + 1), r);
        // Show the exact next checker slot, but clamp it inside the triangle zone for large stacks.
        int slot = Math.min(count, 5);
        float cy = topPoint ? fieldTop + r + slot * spacing : fieldBottom - r - slot * spacing;
        return new float[]{cx, cy};
    }

    private void drawValidRing(Canvas c, float cx, float cy, float r) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x2538EF72);
        paint.setShadowLayer(r * 0.52f, 0, 0, 0xD02DEB6F);
        c.drawCircle(cx, cy, r * 0.82f, paint);
        paint.clearShadowLayer();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(3f, r * 0.16f));
        paint.setColor(0xFF65FF87);
        c.drawCircle(cx, cy, r, paint);
    }

    private void drawInvalidFeedback(Canvas c) {
        if (invalidDestination == INVALID_NONE) return;
        float cx, cy, r = checkerRadius() * 0.72f;
        if (invalidDestination == INVALID_OFF) {
            cx = (offLeft + offRight) / 2f;
            cy = (fieldTop + fieldBottom) / 2f;
        } else {
            float[] center = destinationCenter(invalidDestination);
            cx = center[0];
            cy = center[1];
        }

        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(Math.max(3f, r * 0.15f));
        paint.setColor(0xFFFF5A42);
        paint.setShadowLayer(r * 0.45f, 0, 0, 0xFFFF3B26);
        c.drawCircle(cx, cy, r, paint);
        float d = r * 0.42f;
        c.drawLine(cx - d, cy - d, cx + d, cy + d, paint);
        c.drawLine(cx + d, cy - d, cx - d, cy + d, paint);
        paint.clearShadowLayer();
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || game == null || !game.hasRolled()
                || game.getWinner() != 0 || game.canEndTurn()) return true;

        float x = event.getX(), y = event.getY();
        clearInvalid();

        if (x >= offLeft && x <= offRight && y >= fieldTop && y <= fieldBottom) {
            if (selectedFrom != NO_SELECTION) {
                int off = game.getCurrentPlayer() == BackgammonGame.WHITE
                        ? BackgammonGame.OFF_WHITE : BackgammonGame.OFF_BLACK;
                if (!tryDestination(off)) showInvalid(INVALID_OFF);
            }
            return true;
        }

        if (x >= barLeft && x <= barRight && y >= fieldTop && y <= fieldBottom) {
            if (selectedFrom == BackgammonGame.BAR) {
                selectedFrom = NO_SELECTION;
            } else if (hasMoveFrom(BackgammonGame.BAR)) {
                selectedFrom = BackgammonGame.BAR;
            } else if (selectedFrom != NO_SELECTION) {
                showInvalid(hitNearestPointBesideBar(x, y));
            }
            invalidate();
            return true;
        }

        int point = hitPoint(x, y);
        if (point == 0) return true;

        if (selectedFrom == point) {
            selectedFrom = NO_SELECTION;
            invalidate();
            return true;
        }

        if (selectedFrom != NO_SELECTION) {
            if (tryDestination(point)) return true;
            // Tapping another movable checker switches selection instead of showing an error.
            if (hasMoveFrom(point)) {
                selectedFrom = point;
                invalidate();
                return true;
            }
            showInvalid(point);
            return true;
        }

        if (hasMoveFrom(point)) {
            selectedFrom = point;
            invalidate();
        }
        return true;
    }

    private int hitNearestPointBesideBar(float x, float y) {
        boolean top = y < (fieldTop + fieldBottom) / 2f;
        return top ? (x < (barLeft + barRight) / 2f ? 18 : 19)
                : (x < (barLeft + barRight) / 2f ? 7 : 6);
    }

    private int hitPoint(float x, float y) {
        if (x < fieldLeft || x > fieldRight || y < fieldTop || y > fieldBottom
                || (x >= barLeft && x <= barRight)) return 0;
        int idx;
        if (x < barLeft) idx = (int)((x - fieldLeft) / colWidth);
        else idx = 6 + (int)((x - barRight) / colWidth);
        if (idx < 0 || idx > 11) return 0;
        if (y < (fieldTop + fieldBottom) / 2f) return 13 + idx;
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
        // the engine has already filtered any choice that would violate maximum-dice rules.
        BackgammonGame.Move chosen = candidates.get(0);
        for (BackgammonGame.Move m : candidates) if (m.die > chosen.die) chosen = m;
        if (game.applyMove(chosen)) {
            selectedFrom = NO_SELECTION;
            clearInvalid();
            if (listener != null) listener.onGameChanged();
            invalidate();
            return true;
        }
        return false;
    }

    private void showInvalid(int point) {
        invalidDestination = point;
        invalidate();
        postDelayed(() -> {
            if (invalidDestination == point) {
                invalidDestination = INVALID_NONE;
                invalidate();
            }
        }, 650);
    }

    private void clearInvalid() {
        invalidDestination = INVALID_NONE;
    }

    private static int lighten(int color, float amount) {
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        r += (int)((255 - r) * amount);
        g += (int)((255 - g) * amount);
        b += (int)((255 - b) * amount);
        return Color.rgb(Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }

    private static int darken(int color, float amount) {
        int r = (int)(Color.red(color) * (1f - amount));
        int g = (int)(Color.green(color) * (1f - amount));
        int b = (int)(Color.blue(color) * (1f - amount));
        return Color.rgb(Math.max(0, r), Math.max(0, g), Math.max(0, b));
    }
}
