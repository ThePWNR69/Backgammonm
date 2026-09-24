package com.george.backgammon.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.george.backgammon.cosmetics.BoardTheme;
import com.george.backgammon.cosmetics.CheckerTheme;
import com.george.backgammon.cosmetics.CosmeticCatalog;

/**
 * Shared cosmetic preview. It uses the same BoardMap/BoardGeometry as gameplay, so preview checker
 * stacks cannot drift away from the board triangles.
 */
public final class CosmeticPreviewView extends View {
    public static final int MODE_BOARD = 1;
    public static final int MODE_CHECKERS = 2;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final AssetTextureRepository textures;
    private final StaticBoardRenderer boardRenderer;
    private final BoardGeometry geometry = new BoardGeometry();

    private int mode = MODE_BOARD;
    private BoardTheme boardTheme = CosmeticCatalog.CLASSIC_WALNUT;
    private CheckerTheme checkerTheme = CosmeticCatalog.IVORY_WALNUT;

    public CosmeticPreviewView(Context context) { this(context, null); }
    public CosmeticPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textures = new AssetTextureRepository(context);
        boardRenderer = new StaticBoardRenderer(textures);
        setLayerType(View.LAYER_TYPE_NONE, null);
    }

    public void showBoard(BoardTheme board, CheckerTheme checkers) {
        mode = MODE_BOARD;
        if (board != null) boardTheme = board;
        if (checkers != null) checkerTheme = checkers;
        invalidate();
    }

    public void showCheckers(CheckerTheme checkers) {
        mode = MODE_CHECKERS;
        if (checkers != null) checkerTheme = checkers;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;
        if (mode == MODE_CHECKERS) drawCheckerPair(canvas);
        else drawBoardPreview(canvas);
    }

    private void drawBoardPreview(Canvas canvas) {
        geometry.compute(getWidth(), getHeight());
        boardRenderer.draw(canvas, geometry, boardTheme);

        // One fixed legal sample position for every board. These are standard starting stacks.
        int[] points = {24, 13, 8, 6, 1, 12, 17, 19};
        int[] values = {2, 5, 3, 5, -2, -5, -3, -5};
        for (int i = 0; i < points.length; i++) drawPointStack(canvas, points[i], values[i]);
    }

    private void drawPointStack(Canvas canvas, int point, int value) {
        int count = Math.min(Math.abs(value), 5);
        float radius = geometry.checkerRadius();
        float spacing = geometry.checkerSpacing(radius);
        boolean top = point >= 13;
        int idx = top ? point - 13 : 12 - point;
        float cx = geometry.columnX(idx) + geometry.colWidth * 0.5f;
        for (int slot = 0; slot < count; slot++) {
            float cy = top ? geometry.fieldTop + radius + slot * spacing
                    : geometry.fieldBottom - radius - slot * spacing;
            drawChecker(canvas, cx, cy, radius, value > 0);
        }
    }

    private void drawCheckerPair(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF0C3029);
        canvas.drawRoundRect(new RectF(2, 2, getWidth()-2, getHeight()-2), 18, 18, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, Math.min(getWidth(), getHeight()) * .014f));
        paint.setColor(0xFFCBA65D);
        canvas.drawRoundRect(new RectF(4, 4, getWidth()-4, getHeight()-4), 17, 17, paint);

        float r = Math.min(getHeight() * .30f, getWidth() * .145f);
        float cy = getHeight() * .51f;
        drawChecker(canvas, getWidth() * .34f, cy, r, true);
        drawChecker(canvas, getWidth() * .66f, cy, r, false);
    }

    private void drawChecker(Canvas canvas, float cx, float cy, float radius, boolean light) {
        Bitmap art = textures.get(light ? checkerTheme.lightAsset : checkerTheme.darkAsset);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x68000000);
        canvas.drawCircle(cx + radius * .05f, cy + radius * .10f, radius * 1.04f, paint);
        if (art == null) {
            paint.setColor(light ? checkerTheme.lightBase : checkerTheme.darkBase);
            canvas.drawCircle(cx, cy, radius, paint);
            return;
        }
        Rect src = textures.visibleBounds(art);
        RectF dst = new RectF(cx-radius, cy-radius, cx+radius, cy+radius);
        canvas.drawBitmap(art, src, dst, paint);
    }
}
