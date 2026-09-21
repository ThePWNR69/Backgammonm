package com.george.backgammon;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private BackgammonGame game;
    private BackgammonBoardView boardView;
    private TextView statusTitle;
    private TextView statusSubtitle;
    private TextView playerOneSub;
    private TextView playerTwoSub;
    private View playerOnePanel;
    private View playerTwoPanel;
    private Button rollButton;
    private Button undoButton;
    private Button endTurnButton;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        setContentView(R.layout.activity_main);

        game = new BackgammonGame();
        boardView = findViewById(R.id.boardView);
        statusTitle = findViewById(R.id.statusTitle);
        statusSubtitle = findViewById(R.id.statusSubtitle);
        playerOneSub = findViewById(R.id.playerOneSub);
        playerTwoSub = findViewById(R.id.playerTwoSub);
        playerOnePanel = findViewById(R.id.playerOnePanel);
        playerTwoPanel = findViewById(R.id.playerTwoPanel);
        rollButton = findViewById(R.id.rollButton);
        undoButton = findViewById(R.id.undoButton);
        endTurnButton = findViewById(R.id.endTurnButton);
        Button newGameButton = findViewById(R.id.newGameButton);

        boardView.setGame(game);
        boardView.setOnGameChangedListener(this::refreshUi);

        rollButton.setOnClickListener(v -> {
            game.rollDice();
            boardView.clearSelection();
            refreshUi();
        });

        undoButton.setOnClickListener(v -> {
            if (game.undoLastMove()) {
                boardView.clearSelection();
                refreshUi();
            }
        });

        endTurnButton.setOnClickListener(v -> {
            if (game.endTurn()) {
                boardView.clearSelection();
                refreshUi();
            }
        });

        newGameButton.setOnClickListener(v -> {
            game.reset();
            boardView.clearSelection();
            refreshUi();
        });

        refreshUi();
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
            statusSubtitle.setText("Start a new game to play again");
            rollButton.setEnabled(false);
            undoButton.setEnabled(false);
            endTurnButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        boolean whiteTurn = game.getCurrentPlayer() == BackgammonGame.WHITE;
        String currentName = whiteTurn ? "Player 1" : "Player 2";

        if (!game.hasRolled()) {
            statusTitle.setText(currentName + "'s Turn");
            statusSubtitle.setText("Roll the dice");
        } else if (game.canEndTurn()) {
            statusTitle.setText(game.getMovesMadeThisTurn() > 0 ? "Review Your Move" : "No Legal Move");
            statusSubtitle.setText(game.getMovesMadeThisTurn() > 0
                    ? "Undo to change it, or End Turn to confirm"
                    : "End Turn to pass play");
        } else {
            statusTitle.setText(currentName + "'s Turn");
            String remaining = game.getDiceRemaining().toString();
            statusSubtitle.setText("Select a checker • Remaining dice " + remaining);
        }

        rollButton.setEnabled(!game.hasRolled());
        undoButton.setEnabled(game.canUndo());
        endTurnButton.setEnabled(game.canEndTurn());
        updatePlayerPanels();
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
