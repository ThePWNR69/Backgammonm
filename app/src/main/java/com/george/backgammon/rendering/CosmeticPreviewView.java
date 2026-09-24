package com.george.backgammon.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.george.backgammon.cosmetics.BoardTheme;
import com.george.backgammon.cosmetics.CheckerTheme;
import com.george.backgammon.cosmetics.CosmeticCatalog;
import com.george.backgammon.game.BackgammonGame;

/**
 * Shared cosmetic preview renderer.
 *
 * Store/Customise previews intentionally use the same permanent BoardMap projection and the same
 * checker sizing rules as gameplay. This prevents preview-only checker drift, incorrect point
 * alignment and theme-dependent piece sizes.
 */
public final class CosmeticPreviewView extends View {
    private final BoardGeometry geometry = new BoardGeometry();
    private final AssetTextureRepository textures;
    private final StaticBoardRenderer boardRenderer;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private BoardTheme boardTheme = CosmeticCatalog.CLASSIC_WALNUT;
    private CheckerTheme checkerTheme = CosmeticCatalog.IVORY_WALNUT;
    private boolean showCheckers = true;

    public CosmeticPreviewView(Context context) { this(context, null); }
    public CosmeticPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textures = new AssetTextureRepository(context);
        boardRenderer = new StaticBoardRenderer(textures);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public void setBoardTheme(BoardTheme theme) {
        if (theme != null) boardTheme = theme;
        invalidate();
    }

    public void setCheckerTheme(CheckerTheme theme) {
        if (theme != null) checkerTheme = theme;
        invalidate();
    }

    public void setShowCheckers(boolean show) {
        showCheckers = show;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        geometry.compute(getWidth(), getHeight());
        boardRenderer.draw(canvas, geometry, boardTheme);
        if (showCheckers) drawStartingPosition(canvas);
    }

    private void drawStartingPosition(Canvas canvas) {
        // Standard backgammon opening layout; exactly the same logical points as gameplay.
        drawStack(canvas, 24, 2, true);
        drawStack(canvas, 13, 5, true);
        drawStack(canvas, 8, 3, true);
        drawStack(canvas, 6, 5, true);
        drawStack(canvas, 1, 2, false);
        drawStack(canvas, 12, 5, false);
        drawStack(canvas, 17, 3, false);
        drawStack(canvas, 19, 5, false);
    }

    private void drawStack(Canvas canvas, int point, int count, boolean light) {
        int visible = Math.min(count, 5);
        for (int i = 0; i < visible; i++) {
            float[] center = geometry.landingCenter(point, i);
            drawChecker(canvas, center[0], center[1], geometry.checkerRadius(), light);
        }
    }

    private void drawChecker(Canvas canvas, float cx, float cy, float radius, boolean light) {
        String asset = light ? checkerTheme.lightAsset : checkerTheme.darkAsset;
        Bitmap bitmap = textures.get(asset);
        if (bitmap != null) {
            Rect src = textures.visibleBounds(bitmap);
            RectF dst = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            canvas.drawBitmap(bitmap, src, dst, paint);
            return;
        }

        // Fallback still obeys the exact same geometry/diameter.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(light ? checkerTheme.lightBase : checkerTheme.darkBase);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, radius * 0.08f));
        paint.setColor(light ? checkerTheme.lightEdge : checkerTheme.darkEdge);
        canvas.drawCircle(cx, cy, radius * 0.9f, paint);
        paint.setStyle(Paint.Style.FILL);
    }
}
