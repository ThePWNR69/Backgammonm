package com.george.backgammon.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.george.backgammon.R;
import com.george.backgammon.cosmetics.CosmeticCatalog;
import com.george.backgammon.rendering.CosmeticPreviewView;

public class StoreActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        setContentView(R.layout.activity_store);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        CosmeticPreviewView p0 = findViewById(R.id.storePreview0);
        CosmeticPreviewView p1 = findViewById(R.id.storePreview1);
        CosmeticPreviewView p2 = findViewById(R.id.storePreview2);
        p0.setBoardTheme(CosmeticCatalog.MODERN_TEAL);
        p1.setBoardTheme(CosmeticCatalog.CLASSIC_WALNUT);
        p2.setBoardTheme(CosmeticCatalog.GREEK_MARBLE);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterImmersiveMode();
    }

    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
