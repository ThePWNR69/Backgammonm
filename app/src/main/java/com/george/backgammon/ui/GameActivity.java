package com.george.backgammon.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;


import com.george.backgammon.R;
import com.george.backgammon.ai.AiStrategy;
import com.george.backgammon.ai.PositionalAi;
import com.george.backgammon.cosmetics.CosmeticCatalog;
import com.george.backgammon.cosmetics.BoardTheme;
import com.george.backgammon.cosmetics.CheckerTheme;
import com.george.backgammon.cosmetics.PlayerLoadout;
import com.george.backgammon.animation.MoveAnimationStyle;
import com.george.backgammon.game.BackgammonGame;
import com.george.backgammon.game.Move;
import com.george.backgammon.rendering.BackgammonBoardView;

import java.util.List;

public class GameActivity extends Activity {
    public static final String EXTRA_MODE = "game_mode";
    public static final String MODE_AI = "vs_ai";
    public static final String MODE_TWO_PLAYER = "two_player";
    private static final int AI_PLAYER = BackgammonGame.BLACK;

    private BackgammonGame game;
    private final AiStrategy aiStrategy = new PositionalAi();
    private BackgammonBoardView boardView;
    private TextView statusTitle;
    private TextView playerOneName;
    private TextView playerTwoName;
    private TextView playerOneSub;
    private TextView playerTwoSub;
    private View playerOnePanel;
    private View playerTwoPanel;
    private Button mainActionButton;
    private Button undoButton;
    private Button menuButton;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean versusAi = true;
    private boolean aiBusy = false;
    private PlayerLoadout loadout;
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        setContentView(R.layout.activity_main);

        String requestedMode = getIntent().getStringExtra(EXTRA_MODE);
        versusAi = !MODE_TWO_PLAYER.equals(requestedMode);

        game = new BackgammonGame();
        boardView = findViewById(R.id.boardView);
        statusTitle = findViewById(R.id.statusTitle);
        playerOneName = findViewById(R.id.playerOneName);
        playerTwoName = findViewById(R.id.playerTwoName);
        playerOneSub = findViewById(R.id.playerOneSub);
        playerTwoSub = findViewById(R.id.playerTwoSub);
        playerOnePanel = findViewById(R.id.playerOnePanel);
        playerTwoPanel = findViewById(R.id.playerTwoPanel);
        mainActionButton = findViewById(R.id.mainActionButton);
        undoButton = findViewById(R.id.undoButton);
        menuButton = findViewById(R.id.menuButton);

        boardView.setGame(game);
        prefs = getSharedPreferences("backgammon_visuals", MODE_PRIVATE);
        loadout = CosmeticCatalog.defaultLoadout();
        restoreVisualPreferences();
        boardView.setLoadout(loadout);
        boardView.setOnGameChangedListener(this::refreshUi);

        mainActionButton.setOnClickListener(v -> onMainAction());

        undoButton.setOnClickListener(v -> {
            if (!boardView.isAnimating() && !isAiTurn() && game.canUndo()) {
                boardView.undoLastMoveAnimated(this::refreshUi);
            }
        });

        menuButton.setOnClickListener(this::showGameMenu);
        refreshUi();
    }

    private void onMainAction() {
        if (boardView.isAnimating() || aiBusy || game.getWinner() != 0) return;

        if (!game.isOpeningResolved()) {
            beginOpeningRoll();
            return;
        }

        if (isAiTurn()) return;

        if (!game.hasRolled()) {
            beginHumanDiceRoll();
        } else if (game.canEndTurn() && game.endTurn()) {
            boardView.clearSelection();
            boardView.clearDicePreview();
            refreshUi();
            scheduleAiIfNeeded();
        }
    }

    private void beginOpeningRoll() {
        game.rollOpeningDice();
        int d1 = game.getDieOne();
        int d2 = game.getDieTwo();
        boardView.clearSelection();
        boardView.animateDiceRoll(d1, d2, () -> {
            refreshUi();
            if (game.isOpeningResolved()) scheduleAiIfNeeded();
        });
        refreshUi();
    }

    private void beginHumanDiceRoll() {
        game.rollDice();
        boardView.clearSelection();
        boardView.animateDiceRoll(game.getDieOne(), game.getDieTwo(), this::refreshUi);
        refreshUi();
    }

    private boolean isAiTurn() {
        return versusAi && game.isOpeningResolved() && game.getWinner() == 0
                && game.getCurrentPlayer() == AI_PLAYER;
    }

    private void scheduleAiIfNeeded() {
        if (!isAiTurn() || aiBusy || boardView.isAnimating()) return;
        aiBusy = true;
        refreshUi();
        handler.postDelayed(() -> {
            if (!isAiTurn()) {
                aiBusy = false;
                refreshUi();
                return;
            }
            if (!game.hasRolled()) beginAiDiceRoll();
            else playNextAiMove();
        }, 520L);
    }

    private void beginAiDiceRoll() {
        if (!isAiTurn()) {
            aiBusy = false;
            refreshUi();
            return;
        }
        game.rollDice();
        boardView.animateDiceRoll(game.getDieOne(), game.getDieTwo(), () -> {
            refreshUi();
            handler.postDelayed(this::playNextAiMove, 360L);
        });
        refreshUi();
    }

    private void playNextAiMove() {
        if (!isAiTurn()) {
            aiBusy = false;
            refreshUi();
            return;
        }
        if (boardView.isAnimating()) {
            handler.postDelayed(this::playNextAiMove, 120L);
            return;
        }

        if (game.canEndTurn()) {
            handler.postDelayed(() -> {
                if (isAiTurn() && game.canEndTurn()) {
                    game.endTurn();
                    boardView.clearDicePreview();
                }
                aiBusy = false;
                refreshUi();
            }, 420L);
            return;
        }

        List<Move> sequence = aiStrategy.chooseTurn(game);
        if (sequence.isEmpty()) {
            if (game.canEndTurn()) {
                game.endTurn();
                boardView.clearDicePreview();
            }
            aiBusy = false;
            refreshUi();
            return;
        }

        Move move = sequence.get(0);
        boardView.playMoveAnimated(move, () -> handler.postDelayed(this::playNextAiMove, 300L));
        refreshUi();
    }

    private void showGameMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Main Menu");
        popup.getMenu().add("Customise");
        popup.getMenu().add("New Game");
        popup.getMenu().add(boardView.isShowingFps() ? "Hide FPS" : "Show FPS");
        popup.getMenu().add("Move Guide");
        popup.getMenu().add("About Backgammon Legacy");
        popup.setOnMenuItemClickListener((MenuItem item) -> {
            String title = String.valueOf(item.getTitle());
            if (title.equals("Main Menu")) {
                finish();
                return true;
            }
            if (title.equals("Customise")) {
                startActivity(new Intent(this, CustomiseActivity.class));
                return true;
            }
            if (title.equals("New Game")) {
                resetGame();
                return true;
            }
            if (title.equals("Show FPS") || title.equals("Hide FPS")) {
                boardView.setShowFps(!boardView.isShowingFps());
                return true;
            }
            if (title.equals("Move Guide")) {
                new AlertDialog.Builder(this)
                        .setTitle("Move Colours")
                        .setMessage("Green = legal move with one die\nGold 2 = the same checker can use two dice in sequence\nOrange = capture\nBlue = selected checker")
                        .setPositiveButton("OK", null)
                        .show();
                return true;
            }
            if (title.equals("About Backgammon Legacy")) {
                statusTitle.setText("Backgammon Legacy v1.2 • Main Menu");
                boardView.postDelayed(this::refreshUi, 1400);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void restoreVisualPreferences() {
        String boardId = prefs.getString("board", loadout.board.id);
        for (BoardTheme option : CosmeticCatalog.BOARD_THEMES) {
            if (option.id.equals(boardId)) { loadout.board = option; break; }
        }
        String checkerId = prefs.getString("checkers", loadout.checkers.id);
        for (CheckerTheme option : CosmeticCatalog.CHECKER_THEMES) {
            if (option.id.equals(checkerId)) { loadout.checkers = option; break; }
        }
        String animationId = prefs.getString("animation", loadout.moveAnimation.id());
        for (MoveAnimationStyle option : CosmeticCatalog.MOVE_ANIMATIONS) {
            if (option.id().equals(animationId)) { loadout.moveAnimation = option; break; }
        }
    }

    private void resetGame() {
        handler.removeCallbacksAndMessages(null);
        aiBusy = false;
        game.reset();
        boardView.cancelAnimationsAndReset();
        refreshUi();
    }

    @Override protected void onResume() {
        super.onResume();
        if (prefs != null && loadout != null && boardView != null) {
            restoreVisualPreferences();
            boardView.setLoadout(loadout);
            refreshUi();
        }
        enterImmersiveMode();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
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
        boolean aiTurn = isAiTurn();
        boardView.setInputEnabled(!aiTurn && !aiBusy && game.isOpeningResolved());

        playerOneName.setText(versusAi ? "You" : "Player 1");
        playerTwoName.setText(versusAi ? "AI" : "Player 2");

        int winner = game.getWinner();
        if (winner != 0) {
            String name = displayName(winner);
            statusTitle.setText(name + " Wins!");
            mainActionButton.setText("Game Over");
            mainActionButton.setEnabled(false);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        if (boardView.isDiceRolling()) {
            statusTitle.setText(game.isOpeningResolved() ? "Rolling…" : "Opening Roll…");
            mainActionButton.setText("Rolling…");
            mainActionButton.setEnabled(false);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        if (!game.isOpeningResolved()) {
            if (game.wasOpeningTie()) {
                statusTitle.setText("Tie • Roll Again");
                mainActionButton.setText("⚄  Roll Again");
            } else {
                statusTitle.setText("Roll to Decide First");
                mainActionButton.setText("⚄  Opening Roll");
            }
            mainActionButton.setBackgroundResource(R.drawable.button_green_selector);
            mainActionButton.setEnabled(!aiBusy);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        String currentName = displayName(game.getCurrentPlayer());
        if (aiTurn || aiBusy) {
            statusTitle.setText(game.hasRolled() ? "AI Thinking…" : "AI Turn");
            mainActionButton.setText("AI Turn");
            mainActionButton.setBackgroundResource(R.drawable.button_dark_selector);
            mainActionButton.setEnabled(false);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        if (!game.hasRolled()) {
            statusTitle.setText(versusAi && game.getCurrentPlayer() == BackgammonGame.WHITE ? "Your Turn" : currentName + " Turn");
            mainActionButton.setText("⚄  Roll Dice");
            mainActionButton.setBackgroundResource(R.drawable.button_green_selector);
            mainActionButton.setEnabled(!boardView.isAnimating());
        } else {
            if (game.canEndTurn()) {
                statusTitle.setText(game.getMovesMadeThisTurn() > 0 ? "Review Move" : "No Legal Move");
            } else if (game.getMovesMadeThisTurn() == 0) {
                statusTitle.setText(currentName + " Starts • " + diceText());
            } else {
                statusTitle.setText(currentName + " • " + diceText());
            }
            mainActionButton.setText("✓  End Turn");
            mainActionButton.setBackgroundResource(R.drawable.button_green_selector);
            mainActionButton.setEnabled(game.canEndTurn() && !boardView.isAnimating());
        }

        undoButton.setEnabled(game.canUndo() && !boardView.isAnimating());
        updatePlayerPanels();
    }

    private String displayName(int player) {
        if (!versusAi) return player == BackgammonGame.WHITE ? "Player 1" : "Player 2";
        return player == BackgammonGame.WHITE ? "You" : "AI";
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
        boolean opening = !game.isOpeningResolved();
        boolean whiteActive = !opening && game.getWinner() == 0 && game.getCurrentPlayer() == BackgammonGame.WHITE;
        boolean blackActive = !opening && game.getWinner() == 0 && game.getCurrentPlayer() == BackgammonGame.BLACK;
        playerOnePanel.setBackgroundResource(whiteActive ? R.drawable.bg_panel_active : R.drawable.bg_panel);
        playerTwoPanel.setBackgroundResource(blackActive ? R.drawable.bg_panel_active : R.drawable.bg_panel);

        playerOneSub.setText("LIGHT  •  OFF " + game.getWhiteOff() + "  •  BAR " + game.getWhiteBar());
        playerTwoSub.setText("DARK  •  OFF " + game.getBlackOff() + "  •  BAR " + game.getBlackBar());
    }
}
