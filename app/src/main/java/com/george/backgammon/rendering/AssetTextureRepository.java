package com.george.backgammon.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

    void clearGeneratedState() {
        // Source asset bitmaps are immutable and reusable across loadout changes; keep them cached.
    }
}
