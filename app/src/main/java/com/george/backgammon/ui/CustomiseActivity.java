package com.george.backgammon.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.george.backgammon.R;
import com.george.backgammon.cosmetics.CosmeticCatalog;
import com.george.backgammon.cosmetics.PlayerLoadout;
import com.george.backgammon.rendering.CosmeticPreviewView;

/** Premium visual customisation gallery. */
public class CustomiseActivity extends Activity {
    private SharedPreferences prefs;
    private PlayerLoadout loadout;

    private TextView tabBoard;
    private TextView tabCheckers;
    private TextView tabAnimation;
    private TextView sectionDescription;
    private View boardContent;
    private View checkerContent;
    private View animationContent;

    private final View[] boardCards = new View[3];
    private final View[] checkerCards = new View[3];
    private final View[] animationCards = new View[3];
    private final TextView[] boardStates = new TextView[3];
    private final TextView[] checkerStates = new TextView[3];
    private final TextView[] animationStates = new TextView[3];

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        setContentView(R.layout.activity_customise);

        prefs = getSharedPreferences("backgammon_visuals", MODE_PRIVATE);
        loadout = CosmeticCatalog.defaultLoadout();
        restore();

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        tabBoard = findViewById(R.id.tabBoard);
        tabCheckers = findViewById(R.id.tabCheckers);
        tabAnimation = findViewById(R.id.tabAnimation);
        sectionDescription = findViewById(R.id.sectionDescription);
        boardContent = findViewById(R.id.boardContent);
        checkerContent = findViewById(R.id.checkerContent);
        animationContent = findViewById(R.id.animationContent);

        boardCards[0] = findViewById(R.id.boardCard0);
        boardCards[1] = findViewById(R.id.boardCard1);
        boardCards[2] = findViewById(R.id.boardCard2);
        checkerCards[0] = findViewById(R.id.checkerCard0);
        checkerCards[1] = findViewById(R.id.checkerCard1);
        checkerCards[2] = findViewById(R.id.checkerCard2);
        animationCards[0] = findViewById(R.id.animationCard0);
        animationCards[1] = findViewById(R.id.animationCard1);
        animationCards[2] = findViewById(R.id.animationCard2);

        boardStates[0] = findViewById(R.id.boardState0);
        boardStates[1] = findViewById(R.id.boardState1);
        boardStates[2] = findViewById(R.id.boardState2);
        checkerStates[0] = findViewById(R.id.checkerState0);
        checkerStates[1] = findViewById(R.id.checkerState1);
        checkerStates[2] = findViewById(R.id.checkerState2);
        animationStates[0] = findViewById(R.id.animationState0);
        animationStates[1] = findViewById(R.id.animationState1);
        animationStates[2] = findViewById(R.id.animationState2);

        // Previews use the exact same BoardMap and checker-size rules as gameplay.
        CosmeticPreviewView boardPreview0 = findViewById(R.id.boardPreview0);
        CosmeticPreviewView boardPreview1 = findViewById(R.id.boardPreview1);
        CosmeticPreviewView boardPreview2 = findViewById(R.id.boardPreview2);
        boardPreview0.setBoardTheme(CosmeticCatalog.BOARD_THEMES.get(0));
        boardPreview1.setBoardTheme(CosmeticCatalog.BOARD_THEMES.get(1));
        boardPreview2.setBoardTheme(CosmeticCatalog.BOARD_THEMES.get(2));
        boardPreview0.setCheckerTheme(loadout.checkers);
        boardPreview1.setCheckerTheme(loadout.checkers);
        boardPreview2.setCheckerTheme(loadout.checkers);

        CosmeticPreviewView checkerPreview0 = findViewById(R.id.checkerPreview0);
        CosmeticPreviewView checkerPreview1 = findViewById(R.id.checkerPreview1);
        CosmeticPreviewView checkerPreview2 = findViewById(R.id.checkerPreview2);
        checkerPreview0.setBoardTheme(loadout.board);
        checkerPreview1.setBoardTheme(loadout.board);
        checkerPreview2.setBoardTheme(loadout.board);
        checkerPreview0.setCheckerTheme(CosmeticCatalog.CHECKER_THEMES.get(0));
        checkerPreview1.setCheckerTheme(CosmeticCatalog.CHECKER_THEMES.get(1));
        checkerPreview2.setCheckerTheme(CosmeticCatalog.CHECKER_THEMES.get(2));

        tabBoard.setOnClickListener(v -> showTab(0));
        tabCheckers.setOnClickListener(v -> showTab(1));
        tabAnimation.setOnClickListener(v -> showTab(2));

        for (int i = 0; i < 3; i++) {
            final int index = i;
            boardCards[i].setOnClickListener(v -> equipBoard(index));
            checkerCards[i].setOnClickListener(v -> equipCheckers(index));
            animationCards[i].setOnClickListener(v -> equipAnimation(index));
        }

        showTab(0);
        refreshSelections();
    }

    private void showTab(int tab) {
        boardContent.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        checkerContent.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        animationContent.setVisibility(tab == 2 ? View.VISIBLE : View.GONE);

        tabBoard.setBackgroundResource(tab == 0 ? R.drawable.premium_tab_selected : R.drawable.premium_tab);
        tabCheckers.setBackgroundResource(tab == 1 ? R.drawable.premium_tab_selected : R.drawable.premium_tab);
        tabAnimation.setBackgroundResource(tab == 2 ? R.drawable.premium_tab_selected : R.drawable.premium_tab);
        tabBoard.setTextColor(tab == 0 ? 0xFFF8E5B5 : 0xFFCDBA92);
        tabCheckers.setTextColor(tab == 1 ? 0xFFF8E5B5 : 0xFFCDBA92);
        tabAnimation.setTextColor(tab == 2 ? 0xFFF8E5B5 : 0xFFCDBA92);

        if (tab == 0) sectionDescription.setText("Choose the board that sets the tone of your table");
        else if (tab == 1) sectionDescription.setText("Choose the checker set you want to play with");
        else sectionDescription.setText("Choose how your checkers travel across the board");
    }

    private void equipBoard(int index) {
        loadout.board = CosmeticCatalog.BOARD_THEMES.get(index);
        prefs.edit().putString("board", loadout.board.id).apply();
        refreshSelections();
        Toast.makeText(this, loadout.board.displayName + " equipped", Toast.LENGTH_SHORT).show();
    }

    private void equipCheckers(int index) {
        loadout.checkers = CosmeticCatalog.CHECKER_THEMES.get(index);
        prefs.edit().putString("checkers", loadout.checkers.id).apply();
        refreshSelections();
        Toast.makeText(this, loadout.checkers.displayName + " equipped", Toast.LENGTH_SHORT).show();
    }

    private void equipAnimation(int index) {
        loadout.moveAnimation = CosmeticCatalog.MOVE_ANIMATIONS.get(index);
        prefs.edit().putString("animation", loadout.moveAnimation.id()).apply();
        refreshSelections();
        Toast.makeText(this, loadout.moveAnimation.displayName() + " equipped", Toast.LENGTH_SHORT).show();
    }

    private void refreshSelections() {
        for (int i = 0; i < 3; i++) {
            boolean selectedBoard = CosmeticCatalog.BOARD_THEMES.get(i).id.equals(loadout.board.id);
            boardCards[i].setBackgroundResource(selectedBoard ? R.drawable.premium_card_selected : R.drawable.premium_card);
            boardStates[i].setText(selectedBoard ? "✓  EQUIPPED" : "TAP TO EQUIP");

            boolean selectedChecker = CosmeticCatalog.CHECKER_THEMES.get(i).id.equals(loadout.checkers.id);
            checkerCards[i].setBackgroundResource(selectedChecker ? R.drawable.premium_card_selected : R.drawable.premium_card);
            checkerStates[i].setText(selectedChecker ? "✓  EQUIPPED" : "TAP TO EQUIP");

            boolean selectedAnimation = CosmeticCatalog.MOVE_ANIMATIONS.get(i).id().equals(loadout.moveAnimation.id());
            animationCards[i].setBackgroundResource(selectedAnimation ? R.drawable.premium_card_selected : R.drawable.premium_card);
            animationStates[i].setText(selectedAnimation ? "✓  EQUIPPED" : "TAP TO EQUIP");
        }
    }

    private void restore() {
        loadout.board = CosmeticCatalog.boardById(prefs.getString("board", loadout.board.id));
        loadout.checkers = CosmeticCatalog.checkersById(prefs.getString("checkers", loadout.checkers.id));
        loadout.moveAnimation = CosmeticCatalog.animationById(
                prefs.getString("animation", loadout.moveAnimation.id()));
    }

    @Override protected void onResume() {
        super.onResume();
        restore();
        if (boardCards[0] != null) refreshSelections();
        enterImmersiveMode();
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
