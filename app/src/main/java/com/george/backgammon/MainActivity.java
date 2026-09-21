package com.george.backgammon;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private BackgammonGame game;
    private BackgammonBoardView boardView;
    private TextView statusText;
    private Button rollButton;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        game = new BackgammonGame();
        boardView = findViewById(R.id.boardView);
        statusText = findViewById(R.id.statusText);
        rollButton = findViewById(R.id.rollButton);
        Button newGameButton = findViewById(R.id.newGameButton);

        boardView.setGame(game);
        boardView.setOnGameChangedListener(this::refreshUi);

        rollButton.setOnClickListener(v -> {
            game.rollDice();
            boardView.clearSelection();
            refreshUi();
        });

        newGameButton.setOnClickListener(v -> {
            game.reset();
            boardView.clearSelection();
            refreshUi();
        });

        refreshUi();
    }

    private void refreshUi() {
        boardView.invalidate();
        int winner = game.getWinner();
        if (winner != 0) {
            statusText.setText((winner == BackgammonGame.WHITE ? "White" : "Black") + " wins!");
            rollButton.setEnabled(false);
            return;
        }

        String player = game.getCurrentPlayer() == BackgammonGame.WHITE ? "White" : "Black";
        if (!game.hasRolled()) {
            statusText.setText(player + " to move • Roll the dice");
            rollButton.setEnabled(true);
        } else {
            statusText.setText(player + " to move • Rolled " + game.getDieOne() + " + " + game.getDieTwo()
                    + " • Remaining " + game.getDiceRemaining());
            rollButton.setEnabled(false);
        }
    }
}
