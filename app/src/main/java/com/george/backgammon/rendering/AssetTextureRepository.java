package com.george.backgammon.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Small asset cache used by skins. Missing files intentionally return null so a theme can use
 * procedural fallbacks while art is still being produced.
 */
final class AssetTextureRepository {
    private final Context context;
    private final Map<String, Bitmap> cache = new HashMap<>();
    private final Map<String, Boolean> missing = new HashMap<>();
    private final Map<Bitmap, Rect> alphaBounds = new HashMap<>();

    AssetTextureRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    Bitmap get(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        if (cache.containsKey(path)) return cache.get(path);
        if (Boolean.TRUE.equals(missing.get(path))) return null;
        try (InputStream in = context.getAssets().open("cosmetics/" + path)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap != null) {
                cache.put(path, bitmap);
                return bitmap;
            }
        } catch (IOException ignored) {
            // Theme artwork is optional during development; renderer falls back gracefully.
        }
        missing.put(path, true);
        return null;
    }

    /**
     * Visible-content bounds for cosmetic sprites. Checker rendering uses this rather than the
     * source canvas edges, so transparent padding can never make one checker set look smaller.
     */
    Rect visibleBounds(Bitmap bitmap) {
        Rect cached = alphaBounds.get(bitmap);
        if (cached != null) return new Rect(cached);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        int left = w, top = h, right = -1, bottom = -1;
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                if (((pixels[row + x] >>> 24) & 0xFF) > 12) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }
        Rect result;
        if (right >= left && bottom >= top) {
            int contentW = right - left + 1;
            int contentH = bottom - top + 1;
            int side = Math.max(contentW, contentH);
            int cx = (left + right + 1) / 2;
            int cy = (top + bottom + 1) / 2;
            int l = cx - side / 2;
            int t = cy - side / 2;
            l = Math.max(0, Math.min(w - side, l));
            t = Math.max(0, Math.min(h - side, t));
            result = new Rect(l, t, Math.min(w, l + side), Math.min(h, t + side));
        } else {
            result = new Rect(0, 0, w, h);
        }
        alphaBounds.put(bitmap, new Rect(result));
        return result;
    }

    void clearGeneratedState() {
        // Source asset bitmaps are immutable and reusable across loadout changes; keep them cached.
    }
}
