package com.george.backgammon.rendering;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;


import com.george.backgammon.cosmetics.CosmeticCatalog;
import com.george.backgammon.cosmetics.PlayerLoadout;
import com.george.backgammon.game.BackgammonGame;
import com.george.backgammon.game.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * v0.9 rendering/input surface. Game rules, AI, cosmetics and animation definitions now live in
 * separate packages. This class focuses on board interaction and composing the selected visual skin.
 */
public class BackgammonBoardView extends View {
    public interface OnGameChangedListener { void onGameChanged(); }

    private static final int NO_SELECTION = Integer.MIN_VALUE;
    private static final int INVALID_NONE = Integer.MIN_VALUE;
    private static final int INVALID_OFF = Integer.MAX_VALUE;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint texturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spritePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF spriteDst = new RectF();
    private Bitmap staticBoardBitmap;
    private Bitmap whiteCheckerSprite;
    private Bitmap blackCheckerSprite;
    private final Bitmap[] dieSprites = new Bitmap[7];
    private float cachedCheckerRadius = -1f;
    private float cachedDieSize = -1f;
    private boolean showFps = false;
    private boolean showBoardMap = false;
    private boolean swapCheckerColors = false;
    private long fpsWindowStartNs = 0L;
    private int fpsFrameCount = 0;
    private float measuredFps = 0f;
    private BackgammonGame game;
    private OnGameChangedListener listener;
    private int selectedFrom = NO_SELECTION;
    private int invalidDestination = INVALID_NONE;

    private final BoardGeometry geometry = new BoardGeometry();
    private final AssetTextureRepository textures;
    private final StaticBoardRenderer staticRenderer;
    private PlayerLoadout loadout = CosmeticCatalog.defaultLoadout();

    // Local aliases retained for hot-path drawing math; populated from BoardGeometry each layout pass.
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
    private boolean animationUndo = false;
    private boolean animationHit = false;
    private float animationProgress = 0f;
    private int animationPlayer = 0;
    private Move animationMove;
    private Runnable animationCompletion;

    public BackgammonBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textures = new AssetTextureRepository(context);
        staticRenderer = new StaticBoardRenderer(textures);
        // Keep the View on Android's normal GPU-backed rendering path. v0.4 forced
        // software rendering, which made full-board redraws much more likely to miss frames.
        setLayerType(View.LAYER_TYPE_NONE, null);
        setFocusable(true);
    }

    public void setGame(BackgammonGame game) { this.game = game; invalidate(); }
    public void setOnGameChangedListener(OnGameChangedListener l) { listener = l; }
    public boolean isAnimating() { return animating || diceRolling; }
    public boolean isDiceRolling() { return diceRolling; }
    public void setInputEnabled(boolean enabled) { inputEnabled = enabled; }
    public boolean isShowingFps() { return showFps; }
    public boolean isShowingBoardMap() { return showBoardMap; }
    public void setShowBoardMap(boolean show) { showBoardMap = show; invalidate(); }
    public void setSwapCheckerColors(boolean swap) {
        if (swapCheckerColors == swap) return;
        swapCheckerColors = swap;
        cachedCheckerRadius = -1f;
        whiteCheckerSprite = null;
        blackCheckerSprite = null;
        invalidate();
    }
    public void setShowFps(boolean show) {
        showFps = show;
        fpsWindowStartNs = 0L;
        fpsFrameCount = 0;
        measuredFps = 0f;
        invalidate();
    }

    public void setLoadout(PlayerLoadout loadout) {
        if (loadout == null) return;
        this.loadout = loadout;
        clearRenderCaches();
        invalidate();
    }

    public PlayerLoadout getLoadout() { return loadout; }

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
        requestHighRefresh(false);
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
        requestHighRefresh(true);
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
            postInvalidateOnAnimation();
        });
        diceAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled = false;
            @Override public void onAnimationCancel(Animator animation) { cancelled = true; }
            @Override public void onAnimationEnd(Animator animation) {
                diceRolling = false;
                diceRollProgress = 1f;
                animatedDieOne = finalDieOne;
                animatedDieTwo = finalDieTwo;
                requestHighRefresh(false);
                postInvalidateOnAnimation();
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
        ensureRenderCaches();

        // The expensive wood/leather/point geometry is rasterized only when the View size changes.
        // During animation a frame is therefore mostly bitmap blits plus one moving checker.
        if (staticBoardBitmap != null) c.drawBitmap(staticBoardBitmap, 0f, 0f, null);
        else staticRenderer.draw(c, geometry, loadout.board);

        drawOffCounts(c);
        drawDice(c);
        drawMoveHints(c);
        for (int p = 1; p <= 24; p++) drawCheckersAtPoint(c, p, adjustedPointValue(p));
        drawBarCheckers(c);
        drawAnimationOverlay(c);
        drawInvalidFeedback(c);
        if (showBoardMap) drawBoardMapOverlay(c);
        if (showFps) drawFpsOverlay(c);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clearRenderCaches();
        if (w > 0 && h > 0) {
            computeGeometry();
            ensureRenderCaches();
        }
    }

    private void requestHighRefresh(boolean high) {
        if (Build.VERSION.SDK_INT >= 35) {
            setRequestedFrameRate(high ? View.REQUESTED_FRAME_RATE_CATEGORY_HIGH
                    : View.REQUESTED_FRAME_RATE_CATEGORY_DEFAULT);
        }
    }

    private void clearRenderCaches() {
        staticBoardBitmap = null;
        whiteCheckerSprite = null;
        blackCheckerSprite = null;
        for (int i = 0; i < dieSprites.length; i++) dieSprites[i] = null;
        cachedCheckerRadius = -1f;
        cachedDieSize = -1f;
    }

    private void ensureRenderCaches() {
        float currentR = checkerRadius();
        if (staticBoardBitmap == null || staticBoardBitmap.getWidth() != getWidth()
                || staticBoardBitmap.getHeight() != getHeight()) {
            staticBoardBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas cacheCanvas = new Canvas(staticBoardBitmap);
            staticRenderer.draw(cacheCanvas, geometry, loadout.board);
        }
        if (whiteCheckerSprite == null || Math.abs(cachedCheckerRadius - currentR) > 0.5f) {
            buildCheckerSprites(currentR);
        }
        float dieSize = currentDieSize();
        if (dieSprites[1] == null || Math.abs(cachedDieSize - dieSize) > 0.5f) {
            buildDieSprites(dieSize);
        }
    }

    /** Keeps the physical board close to physical backgammon proportions instead of stretching to 16:9. */
    private void computeGeometry() {
        geometry.compute(getWidth(), getHeight());
        outerLeft = geometry.outerLeft; outerTop = geometry.outerTop;
        outerRight = geometry.outerRight; outerBottom = geometry.outerBottom;
        fieldLeft = geometry.fieldLeft; fieldTop = geometry.fieldTop;
        fieldRight = geometry.fieldRight; fieldBottom = geometry.fieldBottom;
        offLeft = geometry.offLeft; offRight = geometry.offRight;
        barLeft = geometry.barLeft; barRight = geometry.barRight;
        colWidth = geometry.colWidth; triangleHeight = geometry.triangleHeight;
        frame = geometry.frame;
    }

    private float columnX(int visualIndex) { return geometry.columnX(visualIndex); }

    private void drawOffCounts(Canvas c) {
        float cx = (offLeft + offRight) / 2f;
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
        float r = checkerRadius();
        int count = Math.abs(value);
        int visible = Math.min(count, 5);

        // v1.4.4: do not use one fixed column X.  Every checker slot asks BoardGeometry
        // for the midpoint between the actual painted triangle edges at that slot's Y.
        // This keeps a stack visually centred even when the triangle leans a few pixels.
        for (int i = 0; i < visible; i++) {
            float[] center = landingCenter(point, i);
            boolean selected = !animating && selectedFrom == point && i == visible - 1;
            drawChecker(c, center[0], center[1], r, value > 0, selected, 1f);
        }
        if (count > 5) {
            float[] center = landingCenter(point, 4);
            drawStackBadge(c, center[0], center[1], r, count, value > 0);
        }
    }

    /** One renderer-controlled checker size for every cosmetic set. */
    private float checkerRadius() { return geometry.checkerRadius(); }

    private float checkerSpacing(float r) { return geometry.checkerSpacing(r); }

    private void buildCheckerSprites(float r) {
        cachedCheckerRadius = r;
        int padding = Math.max(4, (int)Math.ceil(r * 0.42f));
        int size = Math.max(8, (int)Math.ceil(r * 2f) + padding * 2);
        whiteCheckerSprite = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        blackCheckerSprite = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        drawCheckerSprite(new Canvas(whiteCheckerSprite), size, r, true);
        drawCheckerSprite(new Canvas(blackCheckerSprite), size, r, false);
    }

    private void drawCheckerSprite(Canvas canvas, int size, float r, boolean white) {
        boolean lightVisual = white ^ swapCheckerColors;
        Bitmap artwork = textures.get(lightVisual ? loadout.checkers.lightAsset : loadout.checkers.darkAsset);
        if (artwork == null) {
            drawCheckerPrimitive(canvas, size / 2f, size / 2f, r, white);
            return;
        }
        float cx = size / 2f, cy = size / 2f;
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x70000000);
        paint.setShadowLayer(r * 0.18f, 0, r * 0.12f, 0x88000000);
        canvas.drawCircle(cx, cy, r * 1.01f, paint);
        paint.clearShadowLayer();
        Rect src = textures.visibleBounds(artwork);
        RectF dst = new RectF(cx - r, cy - r, cx + r, cy + r);
        canvas.drawBitmap(artwork, src, dst, spritePaint);
    }

    /** Rendered once into a software Bitmap; live animation only blits the resulting sprite. */
    private void drawCheckerPrimitive(Canvas c, float cx, float cy, float r, boolean white) {
        boolean lightVisual = white ^ swapCheckerColors;
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x78000000);
        paint.setShadowLayer(r * 0.20f, 0, r * 0.15f, 0x99000000);
        c.drawCircle(cx, cy, r * 1.02f, paint);
        paint.clearShadowLayer();

        int center = lightVisual ? loadout.checkers.lightBase : loadout.checkers.darkBase;
        int edge = lightVisual ? loadout.checkers.lightEdge : loadout.checkers.darkEdge;
        int hi = lighten(center, 0.15f);
        paint.setShader(new RadialGradient(cx - r * 0.28f, cy - r * 0.32f, r * 1.25f,
                new int[]{hi, center, edge}, new float[]{0f, 0.52f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(cx, cy, r, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.4f, r * 0.07f));
        paint.setColor(lighten(lightVisual ? loadout.checkers.lightEdge : loadout.checkers.darkEdge, 0.25f));
        c.drawCircle(cx, cy, r * 0.82f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(lightVisual ? 0x52FFFFFF : 0x40FFFFFF);
        c.drawCircle(cx - r * 0.28f, cy - r * 0.30f, r * 0.18f, paint);

        drawCheckerThemeDetail(c, cx, cy, r, lightVisual);
    }


    private void drawCheckerThemeDetail(Canvas c, float cx, float cy, float r, boolean white) {
        String id = loadout.checkers.id;
        paint.setShader(null);
        if ("checkers_marble_bronze".equals(id)) {
            // Fine marble veins plus a warm metallic edge make the set read differently even at phone size.
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(Math.max(1f, r * .035f));
            paint.setColor(white ? 0x40908B82 : 0x45F0C994);
            Path vein = new Path();
            vein.moveTo(cx - r * .62f, cy - r * .18f);
            vein.cubicTo(cx - r * .18f, cy - r * .58f, cx + r * .12f, cy + r * .42f, cx + r * .62f, cy + r * .08f);
            c.drawPath(vein, paint);
            vein.reset();
            vein.moveTo(cx - r * .48f, cy + r * .44f);
            vein.cubicTo(cx - r * .12f, cy + r * .16f, cx + r * .18f, cy + r * .55f, cx + r * .54f, cy + r * .34f);
            c.drawPath(vein, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeWidth(Math.max(1.4f, r * .075f));
            paint.setColor(white ? 0x99C6A05B : 0xAADAA765);
            c.drawCircle(cx, cy, r * .73f, paint);
        } else if ("checkers_obsidian_gold".equals(id)) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(2f, r * .10f));
            paint.setColor(0xFFD7AD58);
            c.drawCircle(cx, cy, r * .74f, paint);
            paint.setStrokeWidth(Math.max(1f, r * .035f));
            paint.setColor(0x88F3D999);
            c.drawCircle(cx, cy, r * .55f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xBBDDB760);
            c.drawCircle(cx, cy, r * .075f, paint);
        } else {
            // Fine concentric turnings suggest polished timber / carved ivory.
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(.8f, r * .028f));
            paint.setColor(white ? 0x36A98D63 : 0x488C5A39);
            c.drawCircle(cx, cy, r * .60f, paint);
            c.drawCircle(cx, cy, r * .46f, paint);
        }
    }

    private void drawChecker(Canvas c, float cx, float cy, float r, boolean white, boolean selected, float alpha) {
        float drawY = selected ? cy - Math.max(2f, r * 0.10f) : cy;
        int a = Math.max(0, Math.min(255, (int)(alpha * 255)));
        Bitmap sprite = white ? whiteCheckerSprite : blackCheckerSprite;
        if (sprite == null || cachedCheckerRadius <= 0f) {
            drawCheckerPrimitive(c, cx, drawY, r, white);
        } else {
            float scale = r / cachedCheckerRadius;
            float halfW = sprite.getWidth() * scale * 0.5f;
            float halfH = sprite.getHeight() * scale * 0.5f;
            spriteDst.set(cx - halfW, drawY - halfH, cx + halfW, drawY + halfH);
            spritePaint.setAlpha(a);
            c.drawBitmap(sprite, null, spriteDst, spritePaint);
            spritePaint.setAlpha(255);
        }

        if (selected) drawSelectionGlow(c, cx, drawY, r);
    }

    private void drawSelectionGlow(Canvas c, float cx, float cy, float r) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(8f, r * 0.28f));
        paint.setColor(withAlpha(loadout.highlights.selectedColor, 48));
        c.drawCircle(cx, cy, r * 1.12f, paint);
        paint.setStrokeWidth(Math.max(5f, r * 0.19f));
        paint.setColor(withAlpha(loadout.highlights.selectedColor, 96));
        c.drawCircle(cx, cy, r * 1.11f, paint);
        paint.setStrokeWidth(Math.max(3f, r * 0.11f));
        paint.setColor(lighten(loadout.highlights.selectedColor, 0.12f));
        c.drawCircle(cx, cy, r * 1.10f, paint);
    }

    private void drawStackBadge(Canvas c, float x, float y, float r, int count, boolean white) {
        boolean lightVisual = white ^ swapCheckerColors;
        float badgeR = r * 0.34f;
        float bx = x + r * 0.62f;
        float by = y - r * 0.55f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x55000000);
        c.drawCircle(bx, by + r * 0.06f, badgeR * 1.05f, paint);
        paint.setColor(lightVisual ? 0xFF31231A : 0xFFF4DFC0);
        c.drawCircle(bx, by, badgeR, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextSize(badgeR * 1.25f);
        paint.setColor(lightVisual ? 0xFFFFF2DA : 0xFF3A2115);
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

    private float currentDieSize() {
        return Math.min(checkerRadius() * 1.48f, (fieldBottom - fieldTop) * 0.09f);
    }

    private void buildDieSprites(float size) {
        cachedDieSize = size;
        int padding = Math.max(4, (int)Math.ceil(size * 0.24f));
        int bitmapSize = Math.max(8, (int)Math.ceil(size) + padding * 2);
        for (int value = 1; value <= 6; value++) {
            Bitmap b = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            String pattern = loadout.dice.faceAssetPattern;
            Bitmap artwork = pattern == null ? null : textures.get(String.format(java.util.Locale.US, pattern, value));
            if (artwork == null) {
                drawDiePrimitive(canvas, bitmapSize / 2f, bitmapSize / 2f, size, value);
            } else {
                float cx = bitmapSize / 2f, cy = bitmapSize / 2f;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0x70000000);
                paint.setShadowLayer(size * 0.14f, 0, size * 0.10f, 0x99000000);
                canvas.drawRoundRect(new RectF(cx - size * .48f, cy - size * .43f,
                        cx + size * .52f, cy + size * .57f), size * .17f, size * .17f, paint);
                paint.clearShadowLayer();
                canvas.drawBitmap(artwork, null, new RectF(cx-size/2f, cy-size/2f, cx+size/2f, cy+size/2f), spritePaint);
            }
            dieSprites[value] = b;
        }
    }

    private void drawDice(Canvas c) {
        if (!game.hasRolled() && !diceRolling && !dicePreviewVisible) return;
        float size = currentDieSize();
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
        Bitmap sprite = value >= 1 && value <= 6 ? dieSprites[value] : null;
        if (sprite == null || cachedDieSize <= 0f) {
            drawDiePrimitive(c, cx, cy, size, value);
            return;
        }
        float scale = size / cachedDieSize;
        float halfW = sprite.getWidth() * scale * 0.5f;
        float halfH = sprite.getHeight() * scale * 0.5f;
        spriteDst.set(cx - halfW, cy - halfH, cx + halfW, cy + halfH);
        c.drawBitmap(sprite, null, spriteDst, spritePaint);
    }

    private void drawDiePrimitive(Canvas c, float cx, float cy, float size, int value) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x75000000);
        paint.setShadowLayer(size * 0.16f, 0, size * 0.11f, 0xBB000000);
        c.drawRoundRect(new RectF(cx - size * 0.48f, cy - size * 0.43f,
                cx + size * 0.52f, cy + size * 0.57f), size * 0.17f, size * 0.17f, paint);
        paint.clearShadowLayer();

        RectF r = new RectF(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);
        paint.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom,
                lighten(loadout.dice.bodyColor, 0.16f), darken(loadout.dice.bodyColor, 0.07f), Shader.TileMode.CLAMP));
        c.drawRoundRect(r, size * 0.16f, size * 0.16f, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, size * 0.035f));
        paint.setColor(loadout.dice.edgeColor);
        c.drawRoundRect(r, size * 0.16f, size * 0.16f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(loadout.dice.pipColor);
        float pr = size * 0.072f;
        float dx = size * 0.22f, dy = size * 0.22f;
        if (value % 2 == 1) c.drawCircle(cx, cy, pr, paint);
        if (value >= 2) { c.drawCircle(cx - dx, cy - dy, pr, paint); c.drawCircle(cx + dx, cy + dy, pr, paint); }
        if (value >= 4) { c.drawCircle(cx + dx, cy - dy, pr, paint); c.drawCircle(cx - dx, cy + dy, pr, paint); }
        if (value == 6) { c.drawCircle(cx - dx, cy, pr, paint); c.drawCircle(cx + dx, cy, pr, paint); }
    }

    /**
     * Move language: blue = selected checker, green = one-die landing, gold = the same checker can
     * legally continue with a second die to reach that final point, orange = capture. No route lines.
     */
    private void drawMoveHints(Canvas c) {
        if (animating || !game.hasRolled() || selectedFrom == NO_SELECTION) return;
        List<Move> moves = game.getAllowedFirstMoves();
        List<Integer> destinations = new ArrayList<>();
        for (Move m : moves) {
            if (m.from == selectedFrom && !destinations.contains(m.to)) destinations.add(m.to);
        }

        for (int to : destinations) drawSingleDieDestination(c, to);

        // Show final positions reachable by using two dice consecutively with this checker. These
        // come directly from RulesEngine playable sequences, so a blocked intermediate point can
        // never produce a misleading combined marker.
        List<Integer> combined = combinedDestinationsFromSelection();
        for (int to : combined) {
            if (!destinations.contains(to)) drawCombinedDestination(c, to);
        }
    }

    private void drawSingleDieDestination(Canvas c, int to) {
        if (to == BackgammonGame.OFF_WHITE || to == BackgammonGame.OFF_BLACK) {
            float[] center = offCenter(game.getCurrentPlayer() == BackgammonGame.WHITE);
            drawValidRing(c, center[0], center[1], Math.min((offRight - offLeft) * 0.30f, checkerRadius() * 0.62f));
            return;
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

    private List<Integer> combinedDestinationsFromSelection() {
        List<Integer> result = new ArrayList<>();
        if (game == null || selectedFrom == NO_SELECTION) return result;
        for (List<Move> seq : game.getPlayableSequencesSnapshot()) {
            if (seq.size() < 2) continue;
            Move first = seq.get(0);
            Move second = seq.get(1);
            if (first.from != selectedFrom || second.from != first.to) continue;
            if (!result.contains(second.to)) result.add(second.to);
        }
        return result;
    }

    private List<Move> combinedSequenceTo(int destination) {
        if (game == null || selectedFrom == NO_SELECTION) return null;
        for (List<Move> seq : game.getPlayableSequencesSnapshot()) {
            if (seq.size() < 2) continue;
            Move first = seq.get(0);
            Move second = seq.get(1);
            if (first.from == selectedFrom && second.from == first.to && second.to == destination) {
                List<Move> pair = new ArrayList<>();
                pair.add(first);
                pair.add(second);
                return pair;
            }
        }
        return null;
    }

    private void drawCombinedDestination(Canvas c, int to) {
        float r = checkerRadius();
        float cx, cy;
        if (to == BackgammonGame.OFF_WHITE || to == BackgammonGame.OFF_BLACK) {
            float[] center = offCenter(game.getCurrentPlayer() == BackgammonGame.WHITE);
            cx = center[0]; cy = center[1]; r = Math.min((offRight - offLeft) * 0.30f, r * 0.62f);
        } else {
            int value = game.getPoint(to);
            if (value == 0) {
                float[] center = landingCenter(to, 0);
                cx = center[0]; cy = center[1]; r *= 0.58f;
            } else if (Integer.signum(value) == game.getCurrentPlayer()) {
                int count = Math.abs(value);
                float[] center = count < 5 ? landingCenter(to, count) : topCheckerCenter(to, 5);
                cx = center[0]; cy = center[1];
                if (count < 5) drawChecker(c, cx, cy, checkerRadius(), game.getCurrentPlayer() == BackgammonGame.WHITE, false, 0.22f);
            } else {
                float[] center = topCheckerCenter(to, 1);
                cx = center[0]; cy = center[1];
            }
        }
        drawCombinedRing(c, cx, cy, r);
        drawTwoDiceBadge(c, cx, cy, r);
    }

    private void drawCombinedRing(Canvas c, float cx, float cy, float r) {
        int color = loadout.highlights.combinedColor;
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(color, 28));
        c.drawCircle(cx, cy, r * 0.80f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(7f, r * 0.27f));
        paint.setColor(withAlpha(color, 44));
        c.drawCircle(cx, cy, r, paint);
        paint.setStrokeWidth(Math.max(3.5f, r * 0.15f));
        paint.setColor(withAlpha(color, 130));
        c.drawCircle(cx, cy, r, paint);
        paint.setStrokeWidth(Math.max(2.2f, r * 0.09f));
        paint.setColor(lighten(color, 0.16f));
        c.drawCircle(cx, cy, r, paint);
    }

    private void drawTwoDiceBadge(Canvas c, float cx, float cy, float r) {
        float br = Math.max(8f, r * 0.34f);
        float bx = cx + r * 0.82f;
        float by = cy - r * 0.78f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xE61C1710);
        c.drawCircle(bx, by, br * 1.12f, paint);
        paint.setColor(loadout.highlights.combinedColor);
        c.drawCircle(bx, by, br, paint);
        paint.setColor(0xFF241B0D);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(br * 1.02f);
        c.drawText("2", bx, by + paint.getTextSize() * 0.34f, paint);
    }

    private void drawOwnedPointPreview(Canvas c, int point, int count, boolean white) {
        float r = checkerRadius();
        if (count < 5) {
            float[] center = landingCenter(point, count);
            drawChecker(c, center[0], center[1], r, white, false, 0.30f);
            drawGreenGlowRing(c, center[0], center[1], r * 1.04f);
        } else {
            float[] center = topCheckerCenter(point, 5);
            drawValidRing(c, center[0], center[1], r * 0.93f);
            float bx = center[0] + r * 0.78f;
            float by = center[1] - r * 0.72f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(loadout.highlights.validColor);
            c.drawCircle(bx, by, r * 0.34f, paint);
            paint.setColor(0xFF102016);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(r * 0.43f);
            c.drawText("+1", bx, by + paint.getTextSize() * 0.34f, paint);
        }
    }

    private void drawGreenGlowRing(Canvas c, float cx, float cy, float r) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(7f, r * 0.28f));
        paint.setColor(withAlpha(loadout.highlights.validColor, 38));
        c.drawCircle(cx, cy, r, paint);
        paint.setStrokeWidth(Math.max(4f, r * 0.19f));
        paint.setColor(withAlpha(loadout.highlights.validColor, 96));
        c.drawCircle(cx, cy, r, paint);
        paint.setStrokeWidth(Math.max(2.5f, r * 0.12f));
        paint.setColor(lighten(loadout.highlights.validColor, 0.18f));
        c.drawCircle(cx, cy, r, paint);
    }

    private void drawValidRing(Canvas c, float cx, float cy, float r) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(loadout.highlights.validColor, 32));
        c.drawCircle(cx, cy, r * 0.80f, paint);
        drawGreenGlowRing(c, cx, cy, r);
    }

    private void drawCaptureHighlight(Canvas c, float cx, float cy, float r) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(7f, r * 0.24f));
        paint.setColor(withAlpha(loadout.highlights.captureColor, 48));
        c.drawCircle(cx, cy, r * 1.10f, paint);
        paint.setStrokeWidth(Math.max(3f, r * 0.12f));
        paint.setColor(loadout.highlights.captureColor);
        c.drawCircle(cx, cy, r * 1.10f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(lighten(loadout.highlights.captureColor, 0.18f));
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
        paint.setStrokeWidth(Math.max(7f, r * 0.28f));
        paint.setColor(withAlpha(loadout.highlights.invalidColor, 53));
        c.drawCircle(cx, cy, r, paint);
        paint.setStrokeWidth(Math.max(3f, r * 0.16f));
        paint.setColor(loadout.highlights.invalidColor);
        c.drawCircle(cx, cy, r, paint);
        float d = r * 0.42f;
        c.drawLine(cx - d, cy - d, cx + d, cy + d, paint);
        c.drawLine(cx + d, cy - d, cx - d, cy + d, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || game == null || animating || diceRolling || !inputEnabled
                || !game.hasRolled() || game.getWinner() != 0 || game.canEndTurn()) return true;

        float x = event.getX(), y = event.getY();
        clearInvalid();

        if (geometry.isInOffTray(x, y)) {
            if (selectedFrom != NO_SELECTION) {
                int off = game.getCurrentPlayer() == BackgammonGame.WHITE
                        ? BackgammonGame.OFF_WHITE : BackgammonGame.OFF_BLACK;
                if (!tryDestination(off)) showInvalid(INVALID_OFF);
            }
            return true;
        }

        if (geometry.isInBar(x, y)) {
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

    private int hitNearestPointBesideBar(float x, float y) { return geometry.nearestPointBesideBar(x, y); }

    private int hitPoint(float x, float y) { return geometry.hitPoint(x, y); }

    private boolean hasMoveFrom(int from) {
        for (Move m : game.getAllowedFirstMoves()) if (m.from == from) return true;
        return false;
    }

    private boolean tryDestination(int to) {
        List<Move> candidates = new ArrayList<>();
        for (Move m : game.getAllowedFirstMoves()) {
            if (m.from == selectedFrom && m.to == to) candidates.add(m);
        }
        if (!candidates.isEmpty()) {
            Move chosen = candidates.get(0);
            for (Move m : candidates) if (m.die > chosen.die) chosen = m;
            beginForwardMove(chosen);
            return true;
        }

        // Gold two-dice destinations are interactive: tapping one performs the two legal moves
        // sequentially, with the normal animation applied to each leg.
        List<Move> pair = combinedSequenceTo(to);
        if (pair != null) {
            beginForwardSequence(pair);
            return true;
        }
        return false;
    }

    private void beginForwardSequence(List<Move> moves) {
        if (moves == null || moves.isEmpty() || animating) return;
        selectedFrom = NO_SELECTION;
        clearInvalid();
        playSequenceStep(new ArrayList<>(moves), 0);
    }

    private void playSequenceStep(List<Move> moves, int index) {
        if (index >= moves.size()) return;
        Move move = moves.get(index);
        animationMove = move;
        animationPlayer = game.getCurrentPlayer();
        animationUndo = false;
        animationHit = move.to >= 1 && move.to <= 24 && game.getPoint(move.to) == -animationPlayer;
        startMoveAnimator(animationHit ? loadout.moveAnimation.hitDurationMs() : loadout.moveAnimation.normalDurationMs(), () -> {
            boolean applied = game.applyMove(move);
            if (listener != null) listener.onGameChanged();
            if (applied && index + 1 < moves.size()) {
                playSequenceStep(moves, index + 1);
            }
        });
    }

    private void beginForwardMove(Move move) {
        if (animating) return;
        animationMove = move;
        animationPlayer = game.getCurrentPlayer();
        animationUndo = false;
        animationHit = move.to >= 1 && move.to <= 24 && game.getPoint(move.to) == -animationPlayer;
        selectedFrom = NO_SELECTION;
        clearInvalid();
        startMoveAnimator(animationHit ? loadout.moveAnimation.hitDurationMs() : loadout.moveAnimation.normalDurationMs(), () -> {
            game.applyMove(move);
            if (listener != null) listener.onGameChanged();
        });
    }

    /** Used by the AI so it shares exactly the same movement animation as a human move. */
    public void playMoveAnimated(Move move, Runnable completion) {
        if (animating || diceRolling || game == null || move == null) return;
        animationMove = move;
        animationPlayer = game.getCurrentPlayer();
        animationUndo = false;
        animationHit = move.to >= 1 && move.to <= 24 && game.getPoint(move.to) == -animationPlayer;
        selectedFrom = NO_SELECTION;
        clearInvalid();
        startMoveAnimator(animationHit ? loadout.moveAnimation.hitDurationMs() : loadout.moveAnimation.normalDurationMs(), () -> {
            game.applyMove(move);
            if (listener != null) listener.onGameChanged();
            if (completion != null) completion.run();
        });
    }

    public void undoLastMoveAnimated(Runnable completion) {
        if (animating || game == null || !game.canUndo()) return;
        Move last = game.peekLastMove();
        if (last == null) return;
        animationMove = last;
        animationPlayer = game.getCurrentPlayer();
        animationUndo = true;
        animationHit = game.peekLastMoveWasHit();
        selectedFrom = NO_SELECTION;
        clearInvalid();
        startMoveAnimator(animationHit ? loadout.moveAnimation.hitDurationMs() : loadout.moveAnimation.normalDurationMs(), () -> {
            game.undoLastMove();
            if (completion != null) completion.run();
            if (listener != null) listener.onGameChanged();
        });
    }

    private void startMoveAnimator(long duration, Runnable completion) {
        animating = true;
        requestHighRefresh(true);
        animationProgress = 0f;
        animationCompletion = completion;
        if (listener != null) listener.onGameChanged();
        moveAnimator = ValueAnimator.ofFloat(0f, 1f);
        moveAnimator.setDuration(duration);
        moveAnimator.setInterpolator(input -> loadout.moveAnimation.interpolation(input));
        moveAnimator.addUpdateListener(a -> {
            animationProgress = (float)a.getAnimatedValue();
            postInvalidateOnAnimation();
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
                requestHighRefresh(false);
                if (!cancelled && finish != null) finish.run();
                postInvalidateOnAnimation();
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
        float y = lerp(start[1], end[1], moveT) - checkerRadius() * loadout.moveAnimation.arcHeightFactor() * easedLift;
        float r = checkerRadius() * (1f + loadout.moveAnimation.scaleLiftFactor() * easedLift);
        drawMovingChecker(c, x, y, r, animationPlayer == BackgammonGame.WHITE, moveT);

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


    private void drawMovingChecker(Canvas c, float x, float y, float r, boolean white, float t) {
        if (!"roll_and_fall".equals(loadout.moveAnimation.id())) {
            drawChecker(c, x, y, r, white, false, 1f);
            return;
        }

        // Premium preview: the disk rises, turns toward its edge, rolls, then falls flat at the landing.
        // It stays a 2D top-down illusion so it remains fast on Android while visibly differing from Slide/Snap.
        float edgePhase = (float)Math.sin(Math.PI * clamp01(t));
        float widthScale = 1f - edgePhase * .62f;
        float roll = 900f * t;
        c.save();
        c.rotate(roll, x, y);
        c.scale(widthScale, 1f + edgePhase * .08f, x, y);
        drawChecker(c, x, y, r, white, false, 1f);
        c.restore();

        if (t > .82f) {
            float settle = (t - .82f) / .18f;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1.4f, r * .06f));
            paint.setColor(withAlpha(loadout.board.metalAccent, (int)(90 * (1f - settle))));
            c.drawCircle(x, y, r * (1f + settle * .18f), paint);
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

    private float[] landingCenter(int point, int existingCount) { return geometry.landingCenter(point, existingCount); }

    private float[] topCheckerCenter(int point, int visibleCount) { return geometry.topCheckerCenter(point, visibleCount); }

    private float[] barCenter(boolean white) { return geometry.barCenter(white); }

    private float[] offCenter(boolean white) { return geometry.offCenter(white); }

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

    /** Developer alignment overlay for validating future board skins against the permanent map. */
    private void drawBoardMapOverlay(Canvas c) {
        float r = checkerRadius();
        paint.setShader(null);
        paint.setStrokeWidth(Math.max(1.5f, r * 0.045f));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextSize(Math.max(9f, r * 0.40f));

        for (int point = 1; point <= 24; point++) {
            float[] baseL = geometry.pointBaseLeft(point);
            float[] baseR = geometry.pointBaseRight(point);
            float[] baseC = geometry.pointBaseCenter(point);
            float[] apex = geometry.pointApex(point);
            float[] center = geometry.pointVisualCenter(point);

            // Cyan centreline is the exact midpoint path from triangle base to apex.
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1.8f, r * 0.05f));
            paint.setColor(0xDC43E8FF);
            c.drawLine(baseC[0], baseC[1], apex[0], apex[1], paint);

            // Faint triangle-edge guides make it obvious that checker centres are halfway
            // between the two edges rather than based on an unrelated column grid.
            paint.setStrokeWidth(Math.max(1f, r * 0.026f));
            paint.setColor(0x775AC8FF);
            c.drawLine(baseL[0], baseL[1], apex[0], apex[1], paint);
            c.drawLine(baseR[0], baseR[1], apex[0], apex[1], paint);

            paint.setStrokeWidth(Math.max(1.5f, r * 0.042f));
            paint.setColor(0xB85AC8FF);
            c.drawCircle(center[0], center[1], r * 0.72f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xEFFFFFFF);
            c.drawText(String.valueOf(point), center[0], center[1] + paint.getTextSize() * 0.34f, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xB8FFD56A);
        paint.setStrokeWidth(Math.max(2f, r * 0.055f));
        c.drawRect(barLeft, fieldTop, barRight, fieldBottom, paint);
        c.drawRect(offLeft, fieldTop, offRight, fieldBottom, paint);
    }

    private void drawFpsOverlay(Canvas c) {
        long now = System.nanoTime();
        if (fpsWindowStartNs == 0L) fpsWindowStartNs = now;
        fpsFrameCount++;
        long elapsed = now - fpsWindowStartNs;
        if (elapsed >= 500_000_000L) {
            measuredFps = fpsFrameCount * 1_000_000_000f / elapsed;
            fpsWindowStartNs = now;
            fpsFrameCount = 0;
        }

        float textSize = Math.max(12f, Math.min(getWidth(), getHeight()) * 0.025f);
        String label = String.format(java.util.Locale.US, "%.0f FPS%s", measuredFps,
                isHardwareAccelerated() ? " • GPU" : " • CPU");
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(textSize);
        float x = outerLeft + frame * 0.6f;
        float y = outerTop + frame * 0.9f;
        float width = paint.measureText(label);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xB8000000);
        c.drawRoundRect(new RectF(x - 6f, y - textSize, x + width + 6f, y + 6f), 7f, 7f, paint);
        paint.setColor(0xFFFFFFFF);
        c.drawText(label, x, y, paint);
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
