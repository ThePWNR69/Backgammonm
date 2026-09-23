package com.george.backgammon.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.george.backgammon.R;
import com.george.backgammon.cosmetics.CosmeticCatalog;
import com.george.backgammon.cosmetics.PlayerLoadout;

/** Launcher/home screen. Gameplay is hosted by GameActivity. */
public class MainActivity extends Activity {
    private TextView equippedSummary;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        setContentView(R.layout.activity_menu);

        equippedSummary = findViewById(R.id.equippedSummary);
        View vsBot = findViewById(R.id.vsBotButton);
        View twoPlayer = findViewById(R.id.twoPlayerButton);
        View store = findViewById(R.id.storeButton);
        View customise = findViewById(R.id.customiseButton);
        View profile = findViewById(R.id.profileButton);
        View settings = findViewById(R.id.settingsButton);
        View coinChip = findViewById(R.id.coinChip);

        vsBot.setOnClickListener(v -> launchGame(GameActivity.MODE_AI));
        twoPlayer.setOnClickListener(v -> launchGame(GameActivity.MODE_TWO_PLAYER));
        store.setOnClickListener(v -> startActivity(new Intent(this, StoreActivity.class)));
        customise.setOnClickListener(v -> startActivity(new Intent(this, CustomiseActivity.class)));
        profile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        settings.setOnClickListener(v -> startActivity(new Intent(this, CustomiseActivity.class)));
        coinChip.setOnClickListener(v -> startActivity(new Intent(this, StoreActivity.class)));

        refreshEquippedSummary();
    }

    private void launchGame(String mode) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }

    @Override protected void onResume() {
        super.onResume();
        refreshEquippedSummary();
        enterImmersiveMode();
    }

    private void refreshEquippedSummary() {
        if (equippedSummary == null) return;
        SharedPreferences prefs = getSharedPreferences("backgammon_visuals", MODE_PRIVATE);
        PlayerLoadout loadout = CosmeticCatalog.defaultLoadout();
        loadout.board = CosmeticCatalog.boardById(prefs.getString("board", loadout.board.id));
        loadout.checkers = CosmeticCatalog.checkersById(prefs.getString("checkers", loadout.checkers.id));
        loadout.moveAnimation = CosmeticCatalog.animationById(
                prefs.getString("animation", loadout.moveAnimation.id()));
        equippedSummary.setText(loadout.board.displayName + "  •  "
                + loadout.checkers.displayName + "  •  " + loadout.moveAnimation.displayName());
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterImmersiveMode();
    }

    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
