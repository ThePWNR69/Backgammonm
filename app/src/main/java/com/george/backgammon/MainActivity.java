package com.george.backgammon;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

public class MainActivity extends Activity {
    private BackgammonGame game;
    private BackgammonBoardView boardView;
    private TextView statusTitle;
    private TextView playerOneSub;
    private TextView playerTwoSub;
    private View playerOnePanel;
    private View playerTwoPanel;
    private Button mainActionButton;
    private Button undoButton;
    private Button menuButton;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        setContentView(R.layout.activity_main);

        game = new BackgammonGame();
        boardView = findViewById(R.id.boardView);
        statusTitle = findViewById(R.id.statusTitle);
        playerOneSub = findViewById(R.id.playerOneSub);
        playerTwoSub = findViewById(R.id.playerTwoSub);
        playerOnePanel = findViewById(R.id.playerOnePanel);
        playerTwoPanel = findViewById(R.id.playerTwoPanel);
        mainActionButton = findViewById(R.id.mainActionButton);
        undoButton = findViewById(R.id.undoButton);
        menuButton = findViewById(R.id.menuButton);

        boardView.setGame(game);
        boardView.setOnGameChangedListener(this::refreshUi);

        mainActionButton.setOnClickListener(v -> {
            if (boardView.isAnimating()) return;
            if (!game.hasRolled()) {
                game.rollDice();
                boardView.clearSelection();
                refreshUi();
            } else if (game.canEndTurn() && game.endTurn()) {
                boardView.clearSelection();
                refreshUi();
            }
        });

        undoButton.setOnClickListener(v -> {
            if (!boardView.isAnimating() && game.canUndo()) {
                boardView.undoLastMoveAnimated(this::refreshUi);
            }
        });

        menuButton.setOnClickListener(this::showGameMenu);
        refreshUi();
    }

    private void showGameMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("New Game");
        popup.getMenu().add("About v0.3");
        popup.setOnMenuItemClickListener((MenuItem item) -> {
            String title = String.valueOf(item.getTitle());
            if (title.equals("New Game")) {
                game.reset();
                boardView.cancelAnimationsAndReset();
                refreshUi();
                return true;
            }
            if (title.equals("About v0.3")) {
                statusTitle.setText("Backgammon v0.3");
                boardView.postDelayed(this::refreshUi, 1200);
                return true;
            }
            return false;
        });
        popup.show();
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

    private void refreshUi() {
        boardView.invalidate();

        int winner = game.getWinner();
        if (winner != 0) {
            String name = winner == BackgammonGame.WHITE ? "Player 1" : "Player 2";
            statusTitle.setText(name + " Wins!");
            mainActionButton.setText("Game Over");
            mainActionButton.setEnabled(false);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        boolean whiteTurn = game.getCurrentPlayer() == BackgammonGame.WHITE;
        String currentName = whiteTurn ? "Player 1" : "Player 2";

        if (!game.hasRolled()) {
            statusTitle.setText(currentName + " • Roll");
            mainActionButton.setText("⚄  Roll Dice");
            mainActionButton.setBackgroundResource(R.drawable.button_green_selector);
            mainActionButton.setEnabled(!boardView.isAnimating());
        } else {
            if (game.canEndTurn()) {
                statusTitle.setText(game.getMovesMadeThisTurn() > 0 ? "Review Move" : "No Legal Move");
            } else {
                statusTitle.setText(currentName + " • " + diceText());
            }
            mainActionButton.setText("✓  End Turn");
            mainActionButton.setBackgroundResource(R.drawable.button_gold_selector);
            mainActionButton.setEnabled(game.canEndTurn() && !boardView.isAnimating());
        }

        undoButton.setEnabled(game.canUndo() && !boardView.isAnimating());
        updatePlayerPanels();
    }

    private String diceText() {
        if (!game.hasRolled()) return "";
        if (game.getDiceRemaining().isEmpty()) return "Done";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < game.getDiceRemaining().size(); i++) {
            if (i > 0) sb.append(" · ");
            sb.append(game.getDiceRemaining().get(i));
        }
        return sb.toString();
    }

    private void updatePlayerPanels() {
        boolean whiteActive = game.getWinner() == 0 && game.getCurrentPlayer() == BackgammonGame.WHITE;
        playerOnePanel.setBackgroundResource(whiteActive ? R.drawable.bg_panel_active : R.drawable.bg_panel);
        playerTwoPanel.setBackgroundResource(!whiteActive && game.getWinner() == 0
                ? R.drawable.bg_panel_active : R.drawable.bg_panel);

        playerOneSub.setText("IVORY  •  OFF " + game.getWhiteOff() + "  •  BAR " + game.getWhiteBar());
        playerTwoSub.setText("WALNUT  •  OFF " + game.getBlackOff() + "  •  BAR " + game.getBlackBar());
    }
}
