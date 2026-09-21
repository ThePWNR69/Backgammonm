package com.george.backgammon;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * v0.3 board: compact chrome, larger physical-board-inspired checkers,
 * occupied-point landing previews and reversible checker movement animations.
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

    private ValueAnimator moveAnimator;
    private ValueAnimator diceAnimator;
    private boolean animating = false;
    private boolean diceRolling = false;
    private boolean dicePreviewVisible = false;
    private float diceRollProgress = 0f;
    private int animatedDieOne = 1;
    private int animatedDieTwo = 1;
    private final Random diceVisualRandom = new Random();
    private int lastDiceVisualStep = -1;
    private boolean inputEnabled = true;
    private long normalMoveDurationMs = 720L;
    private long hitMoveDurationMs = 950L;
    private boolean animationUndo = false;
    private boolean animationHit = false;
    private float animationProgress = 0f;
    private int animationPlayer = 0;
    private BackgammonGame.Move animationMove;
    private Runnable animationCompletion;

    public BackgammonBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setFocusable(true);
    }

    public void setGame(BackgammonGame game) { this.game = game; invalidate(); }
    public void setOnGameChangedListener(OnGameChangedListener l) { listener = l; }
    public boolean isAnimating() { return animating || diceRolling; }
    public boolean isDiceRolling() { return diceRolling; }
    public void setInputEnabled(boolean enabled) { inputEnabled = enabled; }

    /** Kept as a setting internally so cosmetic movement styles can be plugged in later. */
    public void setMoveAnimationDurations(long normalMs, long hitMs) {
        normalMoveDurationMs = Math.max(250L, normalMs);
        hitMoveDurationMs = Math.max(normalMoveDurationMs, hitMs);
    }

    public void clearSelection() {
        selectedFrom = NO_SELECTION;
        clearInvalid();
        invalidate();
    }

    public void cancelAnimationsAndReset() {
        if (moveAnimator != null) moveAnimator.cancel();
        if (diceAnimator != null) diceAnimator.cancel();
        animating = false;
        diceRolling = false;
        dicePreviewVisible = false;
        animationMove = null;
        animationProgress = 0f;
        animationCompletion = null;
        clearSelection();
    }

    public void clearDicePreview() {
        dicePreviewVisible = false;
        invalidate();
    }

    /** Rolls the on-board dice with a tumble/bounce before settling on the supplied results. */
    public void animateDiceRoll(int finalDieOne, int finalDieTwo, Runnable completion) {
        if (diceRolling || animating) return;
        diceRolling = true;
        dicePreviewVisible = true;
        diceRollProgress = 0f;
        animatedDieOne = diceVisualRandom.nextInt(6) + 1;
        animatedDieTwo = diceVisualRandom.nextInt(6) + 1;
        lastDiceVisualStep = -1;
        if (listener != null) listener.onGameChanged();

        diceAnimator = ValueAnimator.ofFloat(0f, 1f);
        diceAnimator.setDuration(920L);
        diceAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        diceAnimator.addUpdateListener(a -> {
            diceRollProgress = (float)a.getAnimatedValue();
            int step = Math.min(11, (int)(diceRollProgress * 12f));
            if (step != lastDiceVisualStep && diceRollProgress < 0.88f) {
                lastDiceVisualStep = step;
                animatedDieOne = diceVisualRandom.nextInt(6) + 1;
                animatedDieTwo = diceVisualRandom.nextInt(6) + 1;
            }
            if (diceRollProgress >= 0.88f) {
                animatedDieOne = finalDieOne;
                animatedDieTwo = finalDieTwo;
            }
            invalidate();
        });
        diceAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled = false;
            @Override public void onAnimationCancel(Animator animation) { cancelled = true; }
            @Override public void onAnimationEnd(Animator animation) {
                diceRolling = false;
                diceRollProgress = 1f;
                animatedDieOne = finalDieOne;
                animatedDieTwo = finalDieTwo;
                invalidate();
                if (listener != null) listener.onGameChanged();
                if (!cancelled && completion != null) completion.run();
            }
        });
        diceAnimator.start();
    }

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

        for (int p = 1; p <= 24; p++) drawCheckersAtPoint(c, p, adjustedPointValue(p));
        drawBarCheckers(c);
        drawAnimationOverlay(c);
        drawInvalidFeedback(c);
    }

    /** Keeps the physical board closer to real backgammon proportions instead of stretching to 16:9. */
    private void computeGeometry() {
        float w = getWidth(), h = getHeight();
        float pad = Math.max(4f, Math.min(w, h) * 0.008f);
        float availableW = w - pad * 2f;
        float availableH = h - pad * 2f;
        float desiredAspect = 2.38f;
        float boardW = Math.min(availableW, availableH * desiredAspect);
        float boardH = Math.min(availableH, boardW / desiredAspect);

        outerLeft = (w - boardW) / 2f;
        outerRight = outerLeft + boardW;
        outerTop = (h - boardH) / 2f;
        outerBottom = outerTop + boardH;

        frame = Math.max(9f, boardH * 0.032f);
        float offWidth = Math.max(38f, boardW * 0.043f);
        offRight = outerRight - frame * 0.45f;
        offLeft = offRight - offWidth;

        fieldLeft = outerLeft + frame;
        fieldTop = outerTop + frame;
        fieldRight = offLeft - frame * 0.45f;
        fieldBottom = outerBottom - frame;

        float playWidth = fieldRight - fieldLeft;
        float barWidth = Math.max(34f, playWidth * 0.047f);
        colWidth = (playWidth - barWidth) / 12f;
        barLeft = fieldLeft + 6f * colWidth;
        barRight = barLeft + barWidth;
        triangleHeight = (fieldBottom - fieldTop) * 0.445f;
    }

    private float columnX(int visualIndex) {
        return fieldLeft + visualIndex * colWidth + (visualIndex >= 6 ? (barRight - barLeft) : 0);
    }

    private void drawBoardShell(Canvas c) {
        RectF outer = new RectF(outerLeft, outerTop, outerRight, outerBottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(outerLeft, outerTop, outerRight, outerBottom,
                new int[]{0xFF321609, 0xFF75401F, 0xFF4C2511, 0xFF8C4E26, 0xFF2B1208},
                null, Shader.TileMode.CLAMP));
        c.drawRoundRect(outer, 17f, 17f, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.2f, frame * 0.10f));
        paint.setColor(0xFFB78343);
        c.drawRoundRect(new RectF(outerLeft + 3, outerTop + 3, outerRight - 3, outerBottom - 3), 15, 15, paint);

        texturePaint.setStyle(Paint.Style.STROKE);
        texturePaint.setStrokeWidth(1f);
        texturePaint.setColor(0x24431808);
        for (int i = 0; i < 22; i++) {
            float y = outerTop + (outerBottom - outerTop) * (i + 1) / 23f;
            float wobble = (i % 4 - 1.5f) * 1.6f;
            c.drawLine(outerLeft + 8, y, outerRight - 8, y + wobble, texturePaint);
        }
    }

    private void drawPlayingField(Canvas c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(fieldLeft, fieldTop, fieldLeft, fieldBottom,
                0xFF30231B, 0xFF1A120E, Shader.TileMode.CLAMP));
        c.drawRect(fieldLeft, fieldTop, fieldRight, fieldBottom, paint);
        paint.setShader(null);

        texturePaint.setStrokeWidth(1f);
        texturePaint.setColor(0x0DFFFFFF);
        for (int i = 0; i < 28; i++) {
            float y = fieldTop + (fieldBottom - fieldTop) * i / 28f;
            c.drawLine(fieldLeft, y, fieldRight, y + ((i & 1) == 0 ? 1.4f : -1.4f), texturePaint);
        }
    }

    private void drawPoints(Canvas c) {
        for (int i = 0; i < 12; i++) {
            float x0 = columnX(i);
            float x1 = x0 + colWidth;
            boolean even = (i % 2 == 0);
            drawTriangle(c, x0, x1, fieldTop, true, even ? 0xFFE9D8BA : 0xFF8F4A29);
            drawTriangle(c, x0, x1, fieldBottom, false, even ? 0xFF8F4A29 : 0xFFE9D8BA);
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
                down ? lighter : darker, down ? darker : lighter, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        c.drawPath(path, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.05f);
        paint.setColor(baseColor == 0xFFE9D8BA ? 0xFFBDA47D : 0xFF5B2A16);
        c.drawPath(path, paint);
    }

    private void drawCentralBar(Canvas c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(barLeft, 0, barRight, 0,
                new int[]{0xFF3D1C0D, 0xFF91512A, 0xFF32150A}, null, Shader.TileMode.CLAMP));
        c.drawRect(barLeft, fieldTop, barRight, fieldBottom, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.4f);
        paint.setColor(0xFFB98546);
        c.drawLine(barLeft + 2, fieldTop, barLeft + 2, fieldBottom, paint);
        c.drawLine(barRight - 2, fieldTop, barRight - 2, fieldBottom, paint);
        drawCompass(c, (barLeft + barRight) / 2f, (fieldTop + fieldBottom) / 2f,
                Math.min(barRight - barLeft, fieldBottom - fieldTop) * 0.32f);
    }

    private void drawCompass(Canvas c, float cx, float cy, float r) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.2f, r * 0.055f));
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
        RectF tray = new RectF(offLeft + 3, fieldTop, offRight, fieldBottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF140D09);
        c.drawRoundRect(tray, 9, 9, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.1f);
        paint.setColor(0xFF704027);
        c.drawRoundRect(tray, 9, 9, paint);

        float cx = (offLeft + offRight) / 2f;
        float mid = (fieldTop + fieldBottom) / 2f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD));
        paint.setTextSize(Math.max(9f, (offRight - offLeft) * 0.19f));
        paint.setColor(0xFFCDA768);
        c.drawText("OFF", cx, mid + paint.getTextSize() * 0.34f, paint);

        drawOffCount(c, cx, offCenter(true)[1], adjustedOffCount(true), true);
        drawOffCount(c, cx, offCenter(false)[1], adjustedOffCount(false), false);
    }

    private int adjustedOffCount(boolean white) {
        int count = white ? game.getWhiteOff() : game.getBlackOff();
        if (!animating || animationMove == null) return count;
        int player = white ? BackgammonGame.WHITE : BackgammonGame.BLACK;
        boolean moveToOff = (animationMove.to == BackgammonGame.OFF_WHITE || animationMove.to == BackgammonGame.OFF_BLACK)
                && animationPlayer == player;
        if (animationUndo && moveToOff) return Math.max(0, count - 1);
        return count;
    }

    private void drawOffCount(Canvas c, float cx, float cy, int count, boolean white) {
        float r = Math.min((offRight - offLeft) * 0.31f, checkerRadius() * 0.55f);
        if (count > 0) drawChecker(c, cx, cy, r, white, false, 1f);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
        paint.setTextSize(Math.max(10f, r * 0.88f));
        paint.setColor(count == 0 ? 0xFFB8915B : (white ? 0xFF241A13 : 0xFFF2DEC0));
        c.drawText(String.valueOf(count), cx, cy + paint.getTextSize() * 0.34f, paint);
    }

    private int adjustedPointValue(int point) {
        int value = game.getPoint(point);
        if (!animating || animationMove == null) return value;

        if (!animationUndo) {
            if (animationMove.from == point) value -= animationPlayer;
            if (animationHit && animationMove.to == point && animationProgress >= 0.43f) value += animationPlayer;
        } else {
            if (animationMove.to == point) value -= animationPlayer;
        }
        return value;
    }

    private void drawCheckersAtPoint(Canvas c, int point, int value) {
        if (value == 0) return;
        boolean topPoint = point >= 13;
        int visualIndex = topPoint ? point - 13 : 12 - point;
        float cx = columnX(visualIndex) + colWidth / 2f;
        float r = checkerRadius();
        int count = Math.abs(value);
        int visible = Math.min(count, 5);
        float spacing = checkerSpacing(r);

        for (int i = 0; i < visible; i++) {
            float cy = topPoint ? fieldTop + r + i * spacing : fieldBottom - r - i * spacing;
            boolean selected = !animating && selectedFrom == point && i == visible - 1;
            drawChecker(c, cx, cy, r, value > 0, selected, 1f);
        }
        if (count > 5) {
            float cy = topPoint ? fieldTop + r + 4 * spacing : fieldBottom - r - 4 * spacing;
            drawStackBadge(c, cx, cy, r, count, value > 0);
        }
    }

    /** Larger checkers; five visible positions end at approximately the point tip. */
    private float checkerRadius() {
        return Math.min(colWidth * 0.39f, triangleHeight * 0.145f);
    }

    private float checkerSpacing(float r) {
        float fitFiveToTip = Math.max(r * 0.92f, (triangleHeight - 2f * r) / 4f);
        return Math.min(r * 1.72f, fitFiveToTip);
    }

    private void drawChecker(Canvas c, float cx, float cy, float r, boolean white, boolean selected, float alpha) {
        float drawY = selected ? cy - Math.max(2f, r * 0.10f) : cy;
        int a = Math.max(0, Math.min(255, (int)(alpha * 255)));

        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor((Math.min(a, 120) << 24));
        paint.setShadowLayer(r * 0.20f, 0, r * 0.15f, 0x99000000);
        c.drawCircle(cx, drawY, r * 1.02f, paint);
        paint.clearShadowLayer();

        int center = white ? 0xFFFFF7E7 : 0xFF714027;
        int edge = white ? 0xFFD2BE9D : 0xFF241008;
        int hi = lighten(center, 0.15f);
        paint.setShader(new RadialGradient(cx - r * 0.28f, drawY - r * 0.32f, r * 1.25f,
                new int[]{withAlpha(hi, a), withAlpha(center, a), withAlpha(edge, a)},
                new float[]{0f, 0.52f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(cx, drawY, r, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.4f, r * 0.07f));
        paint.setColor(withAlpha(white ? 0xFFE7D6B9 : 0xFFB67B56, a));
        c.drawCircle(cx, drawY, r * 0.82f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(0xFFFFFFFF, Math.min(a, white ? 82 : 64)));
        c.drawCircle(cx - r * 0.28f, drawY - r * 0.30f, r * 0.18f, paint);

        if (selected) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(3f, r * 0.12f));
            paint.setColor(0xFF43AFFF);
            paint.setShadowLayer(r * 0.34f, 0, 0, 0xFF178DFF);
            c.drawCircle(cx, drawY, r * 1.10f, paint);
            paint.clearShadowLayer();
        }
    }

    private void drawStackBadge(Canvas c, float x, float y, float r, int count, boolean white) {
        float badgeR = r * 0.34f;
        float bx = x + r * 0.62f;
        float by = y - r * 0.55f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(white ? 0xFF31231A : 0xFFF4DFC0);
        paint.setShadowLayer(r * 0.12f, 0, r * 0.06f, 0x77000000);
        c.drawCircle(bx, by, badgeR, paint);
        paint.clearShadowLayer();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextSize(badgeR * 1.25f);
        paint.setColor(white ? 0xFFFFF2DA : 0xFF3A2115);
        c.drawText(String.valueOf(count), bx, by + paint.getTextSize() * 0.34f, paint);
    }

    private int adjustedBarCount(boolean white) {
        int count = white ? game.getWhiteBar() : game.getBlackBar();
        if (!animating || animationMove == null) return count;
        int player = white ? BackgammonGame.WHITE : BackgammonGame.BLACK;
        if (!animationUndo && animationMove.from == BackgammonGame.BAR && animationPlayer == player) {
            count = Math.max(0, count - 1);
        }
        if (animationUndo && animationHit && animationPlayer == -player) {
            count = Math.max(0, count - 1);
        }
        return count;
    }

    private void drawBarCheckers(Canvas c) {
        float cx = (barLeft + barRight) / 2f;
        float r = Math.min(checkerRadius() * 0.84f, (barRight - barLeft) * 0.42f);
        int whiteBar = adjustedBarCount(true);
        int blackBar = adjustedBarCount(false);
        if (whiteBar > 0) {
            float cy = barCenter(true)[1];
            drawChecker(c, cx, cy, r, true,
                    !animating && selectedFrom == BackgammonGame.BAR && game.getCurrentPlayer() == BackgammonGame.WHITE, 1f);
            if (whiteBar > 1) drawStackBadge(c, cx, cy, r, whiteBar, true);
        }
        if (blackBar > 0) {
            float cy = barCenter(false)[1];
            drawChecker(c, cx, cy, r, false,
                    !animating && selectedFrom == BackgammonGame.BAR && game.getCurrentPlayer() == BackgammonGame.BLACK, 1f);
            if (blackBar > 1) drawStackBadge(c, cx, cy, r, blackBar, false);
        }
    }

    private void drawDice(Canvas c) {
        if (!game.hasRolled() && !diceRolling && !dicePreviewVisible) return;
        float size = Math.min(checkerRadius() * 1.48f, (fieldBottom - fieldTop) * 0.09f);
        float gap = size * 0.34f;
        float cx = fieldLeft + (fieldRight - fieldLeft) * 0.69f;
        float cy = (fieldTop + fieldBottom) / 2f;
        int d1 = diceRolling || dicePreviewVisible && !game.hasRolled() ? animatedDieOne : game.getDieOne();
        int d2 = diceRolling || dicePreviewVisible && !game.hasRolled() ? animatedDieTwo : game.getDieTwo();

        if (diceRolling) {
            float energy = 1f - diceRollProgress;
            float bounce = (float)Math.sin(diceRollProgress * Math.PI * 8f) * size * 0.14f * energy;
            float angle = 420f * energy;
            drawDieTransformed(c, cx - (size + gap) * 0.50f, cy - bounce, size, d1, angle);
            drawDieTransformed(c, cx + (size + gap) * 0.50f, cy + bounce * 0.65f, size, d2, -angle * 0.88f);
        } else {
            drawDie(c, cx - (size + gap) * 0.50f, cy, size, d1);
            drawDie(c, cx + (size + gap) * 0.50f, cy, size, d2);
        }
    }

    private void drawDieTransformed(Canvas c, float cx, float cy, float size, int value, float angle) {
        c.save();
        c.rotate(angle, cx, cy);
        float settleScale = 0.94f + 0.06f * (1f - diceRollProgress);
        c.scale(settleScale, settleScale, cx, cy);
        drawDie(c, cx, cy, size, value);
        c.restore();
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

    /** No movement lines: blue selected checker, green legal landings, orange capture target. */
    private void drawMoveHints(Canvas c) {
        if (animating || !game.hasRolled() || selectedFrom == NO_SELECTION) return;
        List<BackgammonGame.Move> moves = game.getAllowedFirstMoves();
        List<Integer> destinations = new ArrayList<>();
        for (BackgammonGame.Move m : moves) {
            if (m.from == selectedFrom && !destinations.contains(m.to)) destinations.add(m.to);
        }

        for (int to : destinations) {
            if (to == BackgammonGame.OFF_WHITE || to == BackgammonGame.OFF_BLACK) {
                float[] center = offCenter(game.getCurrentPlayer() == BackgammonGame.WHITE);
                drawValidRing(c, center[0], center[1], Math.min((offRight - offLeft) * 0.30f, checkerRadius() * 0.62f));
                continue;
            }

            int value = game.getPoint(to);
            int player = game.getCurrentPlayer();
            if (value == 0) {
                float[] center = landingCenter(to, 0);
                drawValidRing(c, center[0], center[1], checkerRadius() * 0.56f);
            } else if (Integer.signum(value) == player) {
                drawOwnedPointPreview(c, to, Math.abs(value), player == BackgammonGame.WHITE);
            } else if (Math.abs(value) == 1) {
                float[] center = topCheckerCenter(to, 1);
                drawCaptureHighlight(c, center[0], center[1], checkerRadius());
            }
        }
    }

    private void drawOwnedPointPreview(Canvas c, int point, int count, boolean white) {
        float r = checkerRadius();
        if (count < 5) {
            float[] center = landingCenter(point, count);
            drawChecker(c, center[0], center[1], r, white, false, 0.30f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(3f, r * 0.10f));
            paint.setColor(0xFF67FF89);
            paint.setShadowLayer(r * 0.32f, 0, 0, 0xB832EF70);
            c.drawCircle(center[0], center[1], r * 1.04f, paint);
            paint.clearShadowLayer();
        } else {
            float[] center = topCheckerCenter(point, 5);
            drawValidRing(c, center[0], center[1], r * 0.93f);
            float bx = center[0] + r * 0.78f;
            float by = center[1] - r * 0.72f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF2ADB69);
            c.drawCircle(bx, by, r * 0.34f, paint);
            paint.setColor(0xFF102016);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(r * 0.43f);
            c.drawText("+1", bx, by + paint.getTextSize() * 0.34f, paint);
        }
    }

    private void drawValidRing(Canvas c, float cx, float cy, float r) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x2038EF72);
        paint.setShadowLayer(r * 0.48f, 0, 0, 0xC02DEB6F);
        c.drawCircle(cx, cy, r * 0.80f, paint);
        paint.clearShadowLayer();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2.5f, r * 0.15f));
        paint.setColor(0xFF65FF87);
        c.drawCircle(cx, cy, r, paint);
    }

    private void drawCaptureHighlight(Canvas c, float cx, float cy, float r) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(3f, r * 0.12f));
        paint.setColor(0xFFFF9B4B);
        paint.setShadowLayer(r * 0.30f, 0, 0, 0xD0FF6A2B);
        c.drawCircle(cx, cy, r * 1.10f, paint);
        paint.clearShadowLayer();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFFFB15F);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextSize(r * 0.48f);
        c.drawText("×", cx, cy + paint.getTextSize() * 0.32f, paint);
    }

    private void drawInvalidFeedback(Canvas c) {
        if (invalidDestination == INVALID_NONE) return;
        float cx, cy, r = checkerRadius() * 0.62f;
        if (invalidDestination == INVALID_OFF) {
            float[] center = offCenter(game.getCurrentPlayer() == BackgammonGame.WHITE);
            cx = center[0]; cy = center[1];
        } else {
            int v = game.getPoint(invalidDestination);
            float[] center = v == 0 ? landingCenter(invalidDestination, 0)
                    : topCheckerCenter(invalidDestination, Math.min(Math.abs(v), 5));
            cx = center[0]; cy = center[1];
        }

        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(Math.max(3f, r * 0.16f));
        paint.setColor(0xFFFF5A42);
        paint.setShadowLayer(r * 0.40f, 0, 0, 0xFFFF3B26);
        c.drawCircle(cx, cy, r, paint);
        float d = r * 0.42f;
        c.drawLine(cx - d, cy - d, cx + d, cy + d, paint);
        c.drawLine(cx + d, cy - d, cx - d, cy + d, paint);
        paint.clearShadowLayer();
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || game == null || animating || diceRolling || !inputEnabled
                || !game.hasRolled() || game.getWinner() != 0 || game.canEndTurn()) return true;

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

        BackgammonGame.Move chosen = candidates.get(0);
        for (BackgammonGame.Move m : candidates) if (m.die > chosen.die) chosen = m;
        beginForwardMove(chosen);
        return true;
    }

    private void beginForwardMove(BackgammonGame.Move move) {
        if (animating) return;
        animationMove = move;
        animationPlayer = game.getCurrentPlayer();
        animationUndo = false;
        animationHit = move.to >= 1 && move.to <= 24 && game.getPoint(move.to) == -animationPlayer;
        selectedFrom = NO_SELECTION;
        clearInvalid();
        startMoveAnimator(animationHit ? hitMoveDurationMs : normalMoveDurationMs, () -> {
            game.applyMove(move);
            if (listener != null) listener.onGameChanged();
        });
    }

    /** Used by the AI so it shares exactly the same movement animation as a human move. */
    public void playMoveAnimated(BackgammonGame.Move move, Runnable completion) {
        if (animating || diceRolling || game == null || move == null) return;
        animationMove = move;
        animationPlayer = game.getCurrentPlayer();
        animationUndo = false;
        animationHit = move.to >= 1 && move.to <= 24 && game.getPoint(move.to) == -animationPlayer;
        selectedFrom = NO_SELECTION;
        clearInvalid();
        startMoveAnimator(animationHit ? hitMoveDurationMs : normalMoveDurationMs, () -> {
            game.applyMove(move);
            if (listener != null) listener.onGameChanged();
            if (completion != null) completion.run();
        });
    }

    public void undoLastMoveAnimated(Runnable completion) {
        if (animating || game == null || !game.canUndo()) return;
        BackgammonGame.Move last = game.peekLastMove();
        if (last == null) return;
        animationMove = last;
        animationPlayer = game.getCurrentPlayer();
        animationUndo = true;
        animationHit = game.peekLastMoveWasHit();
        selectedFrom = NO_SELECTION;
        clearInvalid();
        startMoveAnimator(animationHit ? hitMoveDurationMs : normalMoveDurationMs, () -> {
            game.undoLastMove();
            if (completion != null) completion.run();
            if (listener != null) listener.onGameChanged();
        });
    }

    private void startMoveAnimator(long duration, Runnable completion) {
        animating = true;
        animationProgress = 0f;
        animationCompletion = completion;
        if (listener != null) listener.onGameChanged();
        moveAnimator = ValueAnimator.ofFloat(0f, 1f);
        moveAnimator.setDuration(duration);
        moveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        moveAnimator.addUpdateListener(a -> {
            animationProgress = (float)a.getAnimatedValue();
            invalidate();
        });
        moveAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled = false;
            @Override public void onAnimationCancel(Animator animation) { cancelled = true; }
            @Override public void onAnimationEnd(Animator animation) {
                Runnable finish = animationCompletion;
                animating = false;
                animationMove = null;
                animationProgress = 0f;
                animationCompletion = null;
                if (!cancelled && finish != null) finish.run();
                invalidate();
            }
        });
        moveAnimator.start();
    }

    private void drawAnimationOverlay(Canvas c) {
        if (!animating || animationMove == null) return;
        float moveT = animationHit ? Math.min(1f, animationProgress / 0.78f) : animationProgress;
        float easedLift = (float)Math.sin(Math.PI * moveT);

        float[] start;
        float[] end;
        if (!animationUndo) {
            start = locationCenterBeforeMove(animationMove.from, animationPlayer);
            end = landingCenterBeforeMove(animationMove.to, animationPlayer, animationHit);
        } else {
            start = locationCenterAfterMove(animationMove.to, animationPlayer);
            end = sourceLandingCenterForUndo(animationMove.from, animationPlayer);
        }

        float x = lerp(start[0], end[0], moveT);
        float y = lerp(start[1], end[1], moveT) - checkerRadius() * 0.14f * easedLift;
        float r = checkerRadius() * (1f + 0.045f * easedLift);
        drawChecker(c, x, y, r, animationPlayer == BackgammonGame.WHITE, false, 1f);

        if (animationHit) {
            int capturedPlayer = -animationPlayer;
            float hitT;
            if (!animationUndo) hitT = clamp01((animationProgress - 0.43f) / 0.57f);
            else hitT = animationProgress;
            float[] hs = !animationUndo ? topCheckerCenter(animationMove.to, 1) : barCenter(capturedPlayer == BackgammonGame.WHITE);
            float[] he = !animationUndo ? barCenter(capturedPlayer == BackgammonGame.WHITE) : topCheckerCenter(animationMove.to, 1);
            float hx = lerp(hs[0], he[0], hitT);
            float hy = lerp(hs[1], he[1], hitT) - checkerRadius() * 0.09f * (float)Math.sin(Math.PI * hitT);
            if (!animationUndo && animationProgress >= 0.43f || animationUndo) {
                drawChecker(c, hx, hy, checkerRadius() * 0.90f, capturedPlayer == BackgammonGame.WHITE, false, 1f);
            }
        }
    }

    private float[] locationCenterBeforeMove(int location, int player) {
        if (location == BackgammonGame.BAR) return barCenter(player == BackgammonGame.WHITE);
        int count = Math.abs(game.getPoint(location));
        return topCheckerCenter(location, Math.min(count, 5));
    }

    private float[] landingCenterBeforeMove(int location, int player, boolean hit) {
        if (location == BackgammonGame.OFF_WHITE || location == BackgammonGame.OFF_BLACK) {
            return offCenter(player == BackgammonGame.WHITE);
        }
        int count = hit ? 0 : Math.abs(game.getPoint(location));
        return landingCenter(location, count);
    }

    private float[] locationCenterAfterMove(int location, int player) {
        if (location == BackgammonGame.OFF_WHITE || location == BackgammonGame.OFF_BLACK) {
            return offCenter(player == BackgammonGame.WHITE);
        }
        int count = Math.abs(game.getPoint(location));
        return topCheckerCenter(location, Math.min(count, 5));
    }

    private float[] sourceLandingCenterForUndo(int location, int player) {
        if (location == BackgammonGame.BAR) return barCenter(player == BackgammonGame.WHITE);
        int current = Math.abs(game.getPoint(location));
        return landingCenter(location, current);
    }

    private float[] landingCenter(int point, int existingCount) {
        boolean topPoint = point >= 13;
        int idx = topPoint ? point - 13 : 12 - point;
        float cx = columnX(idx) + colWidth / 2f;
        float r = checkerRadius();
        float spacing = checkerSpacing(r);
        int slot = Math.min(existingCount, 4);
        float cy = topPoint ? fieldTop + r + slot * spacing : fieldBottom - r - slot * spacing;
        return new float[]{cx, cy};
    }

    private float[] topCheckerCenter(int point, int visibleCount) {
        boolean topPoint = point >= 13;
        int idx = topPoint ? point - 13 : 12 - point;
        float cx = columnX(idx) + colWidth / 2f;
        float r = checkerRadius();
        float spacing = checkerSpacing(r);
        int slot = Math.max(0, Math.min(visibleCount, 5) - 1);
        float cy = topPoint ? fieldTop + r + slot * spacing : fieldBottom - r - slot * spacing;
        return new float[]{cx, cy};
    }

    private float[] barCenter(boolean white) {
        float cx = (barLeft + barRight) / 2f;
        float cy = white ? fieldTop + (fieldBottom - fieldTop) * 0.37f
                : fieldTop + (fieldBottom - fieldTop) * 0.63f;
        return new float[]{cx, cy};
    }

    private float[] offCenter(boolean white) {
        float cx = (offLeft + offRight) / 2f;
        float cy = white ? fieldTop + (fieldBottom - fieldTop) * 0.21f
                : fieldBottom - (fieldBottom - fieldTop) * 0.18f;
        return new float[]{cx, cy};
    }

    private void showInvalid(int point) {
        invalidDestination = point;
        invalidate();
        postDelayed(() -> {
            if (invalidDestination == point) {
                invalidDestination = INVALID_NONE;
                invalidate();
            }
        }, 520);
    }

    private void clearInvalid() { invalidDestination = INVALID_NONE; }
    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }

    private static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
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
