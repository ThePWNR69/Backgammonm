package com.george.backgammon.rendering;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

import com.george.backgammon.cosmetics.BoardTheme;

/** Draws only the immutable board skin. The View caches this output into a bitmap. */
final class StaticBoardRenderer {
    private final AssetTextureRepository textures;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint texturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    StaticBoardRenderer(AssetTextureRepository textures) {
        this.textures = textures;
    }

    void draw(Canvas c, BoardGeometry g, BoardTheme theme) {
        Bitmap fullBoard = textures.get(theme.boardAsset);
        if (isValidProductionBoard(fullBoard)) {
            // Board art is a replaceable skin under one permanent invisible BoardMap.
            // Every production board is authored to the same 2048x992 template, so its 24
            // visual points remain pixel-aligned with checker anchors, hitboxes and animations.
            RectF outer = new RectF(g.outerLeft, g.outerTop, g.outerRight, g.outerBottom);
            c.drawBitmap(fullBoard, null, outer, paint);
            return;
        }
        drawBoardShell(c, g, theme);
        drawPlayingField(c, g, theme);
        drawPoints(c, g, theme);
        drawCentralBar(c, g, theme);
        drawSideTrayBackground(c, g.leftTrayLeft, g.leftTrayRight, g, theme, false);
        drawSideTrayBackground(c, g.offLeft, g.offRight, g, theme, true);
        drawThemeDetails(c, g, theme);
    }

    private void drawBoardShell(Canvas c, BoardGeometry g, BoardTheme theme) {
        RectF outer = new RectF(g.outerLeft, g.outerTop, g.outerRight, g.outerBottom);
        Bitmap asset = textures.get(theme.frameAsset);
        if (asset != null) {
            c.drawBitmap(asset, null, outer, paint);
            // A soft bevel over the texture gives the frame the depth of a physical case.
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(5f, g.frame * 0.30f));
            paint.setColor(0x46000000);
            c.drawRoundRect(new RectF(g.outerLeft + 4, g.outerTop + 4, g.outerRight - 4, g.outerBottom - 4), 16, 16, paint);
            paint.setStrokeWidth(Math.max(1.5f, g.frame * 0.10f));
            paint.setColor(withAlpha(theme.trim, 185));
            c.drawRoundRect(new RectF(g.outerLeft + 3, g.outerTop + 3, g.outerRight - 3, g.outerBottom - 3), 15, 15, paint);
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(g.outerLeft, g.outerTop, g.outerRight, g.outerBottom,
                new int[]{darken(theme.outerWood, .42f), theme.outerWood, darken(theme.outerWood, .18f),
                        lighten(theme.outerWood, .14f), darken(theme.outerWood, .48f)},
                null, Shader.TileMode.CLAMP));
        c.drawRoundRect(outer, 17f, 17f, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.2f, g.frame * 0.10f));
        paint.setColor(theme.trim);
        c.drawRoundRect(new RectF(g.outerLeft + 3, g.outerTop + 3, g.outerRight - 3, g.outerBottom - 3), 15, 15, paint);

        texturePaint.setStyle(Paint.Style.STROKE);
        texturePaint.setStrokeWidth(1f);
        texturePaint.setColor(withAlpha(darken(theme.outerWood, .55f), 42));
        for (int i = 0; i < 22; i++) {
            float y = g.outerTop + (g.outerBottom - g.outerTop) * (i + 1) / 23f;
            float wobble = (i % 4 - 1.5f) * 1.6f;
            c.drawLine(g.outerLeft + 8, y, g.outerRight - 8, y + wobble, texturePaint);
        }
    }

    private void drawPlayingField(Canvas c, BoardGeometry g, BoardTheme theme) {
        Bitmap asset = textures.get(theme.fieldAsset);
        RectF field = new RectF(g.fieldLeft, g.fieldTop, g.fieldRight, g.fieldBottom);
        if (asset != null) {
            c.drawBitmap(asset, null, field, paint);
            // Inset shading keeps the field from looking like a flat wallpaper texture.
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(5f, g.frame * .32f));
            paint.setColor(0x4C000000);
            c.drawRect(g.fieldLeft + 2, g.fieldTop + 2, g.fieldRight - 2, g.fieldBottom - 2, paint);
            paint.setStrokeWidth(1.2f);
            paint.setColor(withAlpha(lighten(theme.trim, .12f), 115));
            c.drawRect(g.fieldLeft + 4, g.fieldTop + 4, g.fieldRight - 4, g.fieldBottom - 4, paint);
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(g.fieldLeft, g.fieldTop, g.fieldLeft, g.fieldBottom,
                lighten(theme.leather, .08f), darken(theme.leather, .32f), Shader.TileMode.CLAMP));
        c.drawRect(g.fieldLeft, g.fieldTop, g.fieldRight, g.fieldBottom, paint);
        paint.setShader(null);
        texturePaint.setStrokeWidth(1f);
        texturePaint.setColor(0x0DFFFFFF);
        for (int i = 0; i < 28; i++) {
            float y = g.fieldTop + (g.fieldBottom - g.fieldTop) * i / 28f;
            c.drawLine(g.fieldLeft, y, g.fieldRight, y + ((i & 1) == 0 ? 1.4f : -1.4f), texturePaint);
        }
    }

    private void drawPoints(Canvas c, BoardGeometry g, BoardTheme theme) {
        Bitmap asset = textures.get(theme.pointsAsset);
        if (asset != null) {
            c.drawBitmap(asset, null, new RectF(g.fieldLeft, g.fieldTop, g.fieldRight, g.fieldBottom), paint);
            return;
        }
        for (int i = 0; i < 12; i++) {
            float x0 = g.columnX(i);
            float x1 = x0 + g.colWidth;
            boolean even = (i % 2 == 0);
            drawTriangle(c, g, x0, x1, g.fieldTop, true, even ? theme.lightPoint : theme.darkPoint, theme);
            drawTriangle(c, g, x0, x1, g.fieldBottom, false, even ? theme.darkPoint : theme.lightPoint, theme);
        }
    }

    private void drawTriangle(Canvas c, BoardGeometry g, float x0, float x1, float edgeY,
                              boolean down, int baseColor, BoardTheme theme) {
        float tipY = edgeY + (down ? g.triangleHeight : -g.triangleHeight);
        Path path = new Path();
        path.moveTo(x0 + 1f, edgeY);
        path.lineTo(x1 - 1f, edgeY);
        path.lineTo((x0 + x1) / 2f, tipY);
        path.close();
        int lighter = lighten(baseColor, .13f);
        int darker = darken(baseColor, .18f);
        paint.setShader(new LinearGradient(0, edgeY, 0, tipY,
                down ? lighter : darker, down ? darker : lighter, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        c.drawPath(path, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.05f);
        paint.setColor(darken(baseColor, .23f));
        c.drawPath(path, paint);
    }

    private void drawCentralBar(Canvas c, BoardGeometry g, BoardTheme theme) {
        Bitmap asset = textures.get(theme.barAsset);
        if (asset != null) {
            c.drawBitmap(asset, null, new RectF(g.barLeft, g.fieldTop, g.barRight, g.fieldBottom), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.4f);
            paint.setColor(withAlpha(theme.trim, 190));
            c.drawLine(g.barLeft + 2, g.fieldTop, g.barLeft + 2, g.fieldBottom, paint);
            c.drawLine(g.barRight - 2, g.fieldTop, g.barRight - 2, g.fieldBottom, paint);
            drawCompass(c, (g.barLeft + g.barRight) / 2f, (g.fieldTop + g.fieldBottom) / 2f,
                    Math.min(g.barRight - g.barLeft, g.fieldBottom - g.fieldTop) * .31f, theme.metalAccent);
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(g.barLeft, 0, g.barRight, 0,
                new int[]{darken(theme.innerWood, .48f), lighten(theme.innerWood, .14f), darken(theme.innerWood, .52f)},
                null, Shader.TileMode.CLAMP));
        c.drawRect(g.barLeft, g.fieldTop, g.barRight, g.fieldBottom, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.4f);
        paint.setColor(theme.trim);
        c.drawLine(g.barLeft + 2, g.fieldTop, g.barLeft + 2, g.fieldBottom, paint);
        c.drawLine(g.barRight - 2, g.fieldTop, g.barRight - 2, g.fieldBottom, paint);
        drawCompass(c, (g.barLeft + g.barRight) / 2f, (g.fieldTop + g.fieldBottom) / 2f,
                Math.min(g.barRight - g.barLeft, g.fieldBottom - g.fieldTop) * .32f, theme.metalAccent);
    }

    private void drawCompass(Canvas c, float cx, float cy, float r, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.2f, r * .055f));
        paint.setColor(color);
        Path star = new Path();
        for (int i = 0; i < 16; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 8;
            float rr = (i % 2 == 0) ? r : r * .34f;
            float x = cx + (float)Math.cos(a) * rr;
            float y = cy + (float)Math.sin(a) * rr;
            if (i == 0) star.moveTo(x, y); else star.lineTo(x, y);
        }
        star.close();
        c.drawPath(star, paint);
        c.drawCircle(cx, cy, r * .14f, paint);
    }

    private void drawSideTrayBackground(Canvas c, float trayLeft, float trayRight,
                                        BoardGeometry g, BoardTheme theme, boolean labelOff) {
        RectF tray = new RectF(trayLeft + 3, g.fieldTop, trayRight, g.fieldBottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(darken(theme.leather, .38f));
        c.drawRoundRect(tray, 9, 9, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.1f);
        paint.setColor(darken(theme.trim, .26f));
        c.drawRoundRect(tray, 9, 9, paint);
        if (!labelOff) return;
        float cx = (trayLeft + trayRight) / 2f;
        float mid = (g.fieldTop + g.fieldBottom) / 2f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD));
        paint.setTextSize(Math.max(9f, (trayRight - trayLeft) * .19f));
        paint.setColor(theme.metalAccent);
        c.drawText("OFF", cx, mid + paint.getTextSize() * .34f, paint);
    }


    /** Extra procedural material detail so the three showcase boards read as genuinely different skins. */
    private void drawThemeDetails(Canvas c, BoardGeometry g, BoardTheme theme) {
        if ("board_greek_marble".equals(theme.id)) {
            drawMarbleVeins(c, g);
            drawGreekKeyTrim(c, g, theme);
        } else if ("board_modern_teal".equals(theme.id)) {
            drawModernStitching(c, g, theme);
            drawModernPanelLines(c, g, theme);
        } else {
            drawWalnutInlays(c, g, theme);
        }
    }

    private void drawWalnutInlays(Canvas c, BoardGeometry g, BoardTheme theme) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, g.frame * .07f));
        paint.setColor(withAlpha(lighten(theme.trim, .18f), 125));
        float inset = Math.max(5f, g.frame * .24f);
        c.drawRoundRect(new RectF(g.outerLeft + inset, g.outerTop + inset,
                g.outerRight - inset, g.outerBottom - inset), 11f, 11f, paint);

        // A few darker grain strokes in the rails, deliberately irregular rather than UI-flat.
        texturePaint.setStyle(Paint.Style.STROKE);
        texturePaint.setStrokeWidth(1.2f);
        texturePaint.setColor(withAlpha(darken(theme.outerWood, .56f), 52));
        for (int i = 0; i < 9; i++) {
            float x = g.outerLeft + 12 + i * (g.outerRight - g.outerLeft - 24) / 8f;
            float wobble = (i % 3 - 1) * 2.2f;
            c.drawLine(x, g.outerTop + 5, x + wobble, g.fieldTop - 3, texturePaint);
            c.drawLine(x - wobble, g.fieldBottom + 3, x, g.outerBottom - 5, texturePaint);
        }
    }

    private void drawMarbleVeins(Canvas c, BoardGeometry g) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        float w = g.outerRight - g.outerLeft;
        float h = g.outerBottom - g.outerTop;
        int[] alphas = {35, 24, 31, 20, 28};
        for (int i = 0; i < 5; i++) {
            Path vein = new Path();
            float sy = g.outerTop + h * (.10f + i * .18f);
            vein.moveTo(g.outerLeft + w * .02f, sy);
            vein.cubicTo(g.outerLeft + w * .28f, sy + (i % 2 == 0 ? 14 : -18),
                    g.outerLeft + w * .55f, sy + (i % 2 == 0 ? -11 : 15),
                    g.outerRight - w * .03f, sy + (i - 2) * 3f);
            paint.setStrokeWidth(i % 2 == 0 ? 1.4f : .85f);
            paint.setColor(withAlpha(0xFF6E7782, alphas[i]));
            c.drawPath(vein, paint);
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawGreekKeyTrim(Canvas c, BoardGeometry g, BoardTheme theme) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.4f, g.frame * .075f));
        paint.setColor(withAlpha(theme.trim, 185));
        float yTop = g.outerTop + Math.max(6f, g.frame * .32f);
        float yBottom = g.outerBottom - Math.max(6f, g.frame * .32f);
        float step = Math.max(12f, g.frame * .75f);
        for (float x = g.outerLeft + 8; x < g.outerRight - 8 - step; x += step) {
            Path key = new Path();
            key.moveTo(x, yTop); key.lineTo(x + step * .72f, yTop);
            key.lineTo(x + step * .72f, yTop + step * .22f);
            key.lineTo(x + step * .28f, yTop + step * .22f);
            key.lineTo(x + step * .28f, yTop + step * .42f);
            key.lineTo(x + step, yTop + step * .42f);
            c.drawPath(key, paint);

            key.reset();
            key.moveTo(x, yBottom); key.lineTo(x + step * .72f, yBottom);
            key.lineTo(x + step * .72f, yBottom - step * .22f);
            key.lineTo(x + step * .28f, yBottom - step * .22f);
            key.lineTo(x + step * .28f, yBottom - step * .42f);
            key.lineTo(x + step, yBottom - step * .42f);
            c.drawPath(key, paint);
        }
    }

    private void drawModernStitching(Canvas c, BoardGeometry g, BoardTheme theme) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.1f);
        paint.setColor(withAlpha(lighten(theme.darkPoint, .22f), 95));
        float dash = 7f;
        for (float x = g.fieldLeft + 7; x < g.fieldRight - 7; x += dash * 2f) {
            c.drawLine(x, g.fieldTop + 5, Math.min(x + dash, g.fieldRight - 7), g.fieldTop + 5, paint);
            c.drawLine(x, g.fieldBottom - 5, Math.min(x + dash, g.fieldRight - 7), g.fieldBottom - 5, paint);
        }
    }

    private void drawModernPanelLines(Canvas c, BoardGeometry g, BoardTheme theme) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(withAlpha(theme.trim, 42));
        float mid = (g.fieldTop + g.fieldBottom) * .5f;
        c.drawLine(g.fieldLeft + 4, mid, g.barLeft - 4, mid, paint);
        c.drawLine(g.barRight + 4, mid, g.fieldRight - 4, mid, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(theme.trim, 100));
        float r = Math.max(1.7f, g.frame * .08f);
        for (int i = 0; i < 6; i++) {
            float y = g.fieldTop + (g.fieldBottom - g.fieldTop) * (i + 1) / 7f;
            c.drawCircle(g.barLeft + 4f, y, r, paint);
            c.drawCircle(g.barRight - 4f, y, r, paint);
        }
    }

    private boolean isValidProductionBoard(Bitmap bitmap) {
        return bitmap != null
                && bitmap.getWidth() == BoardMap.MASTER_WIDTH_PX
                && bitmap.getHeight() == BoardMap.MASTER_HEIGHT_PX;
    }

    private static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }
    private static int lighten(int color, float amount) {
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        r += (int)((255 - r) * amount); g += (int)((255 - g) * amount); b += (int)((255 - b) * amount);
        return Color.rgb(Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }
    private static int darken(int color, float amount) {
        return Color.rgb(Math.max(0, (int)(Color.red(color) * (1f - amount))),
                Math.max(0, (int)(Color.green(color) * (1f - amount))),
                Math.max(0, (int)(Color.blue(color) * (1f - amount))));
    }
}
