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
        drawBoardShell(c, g, theme);
        drawPlayingField(c, g, theme);
        drawPoints(c, g, theme);
        drawCentralBar(c, g, theme);
        drawOffTrayBackground(c, g, theme);
    }

    private void drawBoardShell(Canvas c, BoardGeometry g, BoardTheme theme) {
        RectF outer = new RectF(g.outerLeft, g.outerTop, g.outerRight, g.outerBottom);
        Bitmap asset = textures.get(theme.frameAsset);
        if (asset != null) {
            c.drawBitmap(asset, null, outer, paint);
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

    private void drawOffTrayBackground(Canvas c, BoardGeometry g, BoardTheme theme) {
        RectF tray = new RectF(g.offLeft + 3, g.fieldTop, g.offRight, g.fieldBottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(darken(theme.leather, .38f));
        c.drawRoundRect(tray, 9, 9, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.1f);
        paint.setColor(darken(theme.trim, .26f));
        c.drawRoundRect(tray, 9, 9, paint);
        float cx = (g.offLeft + g.offRight) / 2f;
        float mid = (g.fieldTop + g.fieldBottom) / 2f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD));
        paint.setTextSize(Math.max(9f, (g.offRight - g.offLeft) * .19f));
        paint.setColor(theme.metalAccent);
        c.drawText("OFF", cx, mid + paint.getTextSize() * .34f, paint);
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
