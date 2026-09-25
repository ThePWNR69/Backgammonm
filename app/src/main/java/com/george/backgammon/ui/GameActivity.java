package com.george.backgammon.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ImageView;
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
    public static final String EXTRA_PLAYER_ONE_SIDE = "player_one_side";
    public static final String EXTRA_PLAYER_ONE_LIGHT = "player_one_light";
    public static final String EXTRA_MATCH_TARGET = "match_target";
    public static final String EXTRA_DIFFICULTY = "difficulty";

    private BackgammonGame game;
    private AiStrategy aiStrategy;
    private BackgammonBoardView boardView;
    private TextView statusTitle;
    private TextView playerOneName;
    private TextView playerTwoName;
    private TextView playerOneSub;
    private TextView playerTwoSub;
    private ImageView playerOneCheckerIcon;
    private ImageView playerTwoCheckerIcon;
    private View playerOnePanel;
    private View playerTwoPanel;
    private Button mainActionButton;
    private Button undoButton;
    private Button menuButton;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean versusAi = true;
    private boolean aiBusy = false;
    private int playerOneSide = BackgammonGame.WHITE;
    private int aiPlayer = BackgammonGame.BLACK;
    private boolean playerOneLight = true;
    private int matchTarget = 1;
    private int playerOneScore = 0;
    private int playerTwoScore = 0;
    private boolean winnerScored = false;
    private PlayerLoadout loadout;
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        setContentView(R.layout.activity_main);

        String requestedMode = getIntent().getStringExtra(EXTRA_MODE);
        versusAi = !MODE_TWO_PLAYER.equals(requestedMode);
        playerOneSide = getIntent().getIntExtra(EXTRA_PLAYER_ONE_SIDE, BackgammonGame.WHITE);
        if (playerOneSide != BackgammonGame.BLACK) playerOneSide = BackgammonGame.WHITE;
        aiPlayer = -playerOneSide;
        playerOneLight = getIntent().getBooleanExtra(EXTRA_PLAYER_ONE_LIGHT, true);
        matchTarget = Math.max(1, getIntent().getIntExtra(EXTRA_MATCH_TARGET, 1));
        int difficulty = getIntent().getIntExtra(EXTRA_DIFFICULTY, 1);
        double aiNoise = difficulty <= 0 ? 12.0 : (difficulty >= 2 ? 0.0 : 0.35);
        aiStrategy = new PositionalAi(aiNoise);

        game = new BackgammonGame();
        boardView = findViewById(R.id.boardView);
        statusTitle = findViewById(R.id.statusTitle);
        playerOneName = findViewById(R.id.playerOneName);
        playerTwoName = findViewById(R.id.playerTwoName);
        playerOneSub = findViewById(R.id.playerOneSub);
        playerTwoSub = findViewById(R.id.playerTwoSub);
        playerOneCheckerIcon = findViewById(R.id.playerOneCheckerIcon);
        playerTwoCheckerIcon = findViewById(R.id.playerTwoCheckerIcon);
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
        updatePlayerCheckerIcons();
        boardView.setSwapCheckerColors((playerOneSide == BackgammonGame.WHITE) ^ playerOneLight);
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
        if (boardView.isAnimating() || aiBusy) return;

        if (game.getWinner() != 0) {
            if (playerOneScore < matchTarget && playerTwoScore < matchTarget) resetRound();
            return;
        }

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
                && game.getCurrentPlayer() == aiPlayer;
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
        popup.getMenu().add(boardView.isShowingBoardMap() ? "Hide Board Map" : "Show Board Map");
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
            if (title.equals("Show Board Map") || title.equals("Hide Board Map")) {
                boardView.setShowBoardMap(!boardView.isShowingBoardMap());
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
                statusTitle.setText("Backgammon Legacy v1.4.1 • Premium Rebuild");
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


    private void updatePlayerCheckerIcons() {
        if (loadout == null || loadout.checkers == null) return;
        String p1 = playerOneLight ? loadout.checkers.lightAsset : loadout.checkers.darkAsset;
        String p2 = playerOneLight ? loadout.checkers.darkAsset : loadout.checkers.lightAsset;
        setAssetImage(playerOneCheckerIcon, p1);
        setAssetImage(playerTwoCheckerIcon, p2);
    }

    private void setAssetImage(ImageView view, String relativeAssetPath) {
        if (view == null || relativeAssetPath == null) return;
        try (java.io.InputStream in = getAssets().open("cosmetics/" + relativeAssetPath)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            view.setImageBitmap(bitmap);
        } catch (Exception ignored) {
            view.setImageDrawable(null);
        }
    }

    private void resetGame() {
        playerOneScore = 0;
        playerTwoScore = 0;
        resetRound();
    }

    private void resetRound() {
        handler.removeCallbacksAndMessages(null);
        aiBusy = false;
        winnerScored = false;
        game.reset();
        boardView.cancelAnimationsAndReset();
        refreshUi();
    }

    @Override protected void onResume() {
        super.onResume();
        if (prefs != null && loadout != null && boardView != null) {
            restoreVisualPreferences();
            boardView.setLoadout(loadout);
            updatePlayerCheckerIcons();
            boardView.setSwapCheckerColors((playerOneSide == BackgammonGame.WHITE) ^ playerOneLight);
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
            if (!winnerScored) {
                if (winner == playerOneSide) playerOneScore++; else playerTwoScore++;
                winnerScored = true;
            }
            boolean matchOver = playerOneScore >= matchTarget || playerTwoScore >= matchTarget;
            String name = displayName(winner);
            statusTitle.setText(matchOver
                    ? name + " Wins Match • " + playerOneScore + "–" + playerTwoScore
                    : name + " Wins Game • " + playerOneScore + "–" + playerTwoScore);
            mainActionButton.setText(matchOver ? "Match Over" : "Next Game");
            mainActionButton.setBackgroundResource(matchOver ? R.drawable.button_dark : R.drawable.button_green);
            mainActionButton.setEnabled(!matchOver);
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
            mainActionButton.setBackgroundResource(R.drawable.button_green);
            mainActionButton.setEnabled(!aiBusy);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        String currentName = displayName(game.getCurrentPlayer());
        if (aiTurn || aiBusy) {
            statusTitle.setText(game.hasRolled() ? "AI Thinking…" : "AI Turn");
            mainActionButton.setText("AI Turn");
            mainActionButton.setBackgroundResource(R.drawable.button_dark);
            mainActionButton.setEnabled(false);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        if (!game.hasRolled()) {
            statusTitle.setText(versusAi && game.getCurrentPlayer() == playerOneSide ? "Your Turn" : currentName + " Turn");
            mainActionButton.setText("⚄  Roll Dice");
            mainActionButton.setBackgroundResource(R.drawable.button_green);
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
            mainActionButton.setBackgroundResource(R.drawable.button_green);
            mainActionButton.setEnabled(game.canEndTurn() && !boardView.isAnimating());
        }

        undoButton.setEnabled(game.canUndo() && !boardView.isAnimating());
        updatePlayerPanels();
    }

    private String displayName(int player) {
        boolean isPlayerOne = player == playerOneSide;
        if (!versusAi) return isPlayerOne ? "Player 1" : "Player 2";
        return isPlayerOne ? "You" : "AI";
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
        boolean p1Active = !opening && game.getWinner() == 0 && game.getCurrentPlayer() == playerOneSide;
        boolean p2Active = !opening && game.getWinner() == 0 && game.getCurrentPlayer() == -playerOneSide;
        playerOnePanel.setBackgroundResource(p1Active ? R.drawable.bg_panel_active : R.drawable.bg_panel);
        playerTwoPanel.setBackgroundResource(p2Active ? R.drawable.bg_panel_active : R.drawable.bg_panel);

        String p1Colour = playerOneLight ? "LIGHT" : "DARK";
        String p2Colour = playerOneLight ? "DARK" : "LIGHT";
        String p1Dir = playerOneSide == BackgammonGame.WHITE ? "↺" : "↻";
        String p2Dir = playerOneSide == BackgammonGame.WHITE ? "↻" : "↺";
        String score1 = matchTarget > 1 ? "  •  SCORE " + playerOneScore : "";
        String score2 = matchTarget > 1 ? "  •  SCORE " + playerTwoScore : "";
        playerOneSub.setText(p1Colour + " " + p1Dir + score1 + "  •  OFF " + offFor(playerOneSide) + "  •  BAR " + barFor(playerOneSide));
        playerTwoSub.setText(p2Colour + " " + p2Dir + score2 + "  •  OFF " + offFor(-playerOneSide) + "  •  BAR " + barFor(-playerOneSide));
    }

    private int offFor(int player) {
        return player == BackgammonGame.WHITE ? game.getWhiteOff() : game.getBlackOff();
    }

    private int barFor(int player) {
        return player == BackgammonGame.WHITE ? game.getWhiteBar() : game.getBlackBar();
    }
}
