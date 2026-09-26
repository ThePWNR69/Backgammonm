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
import android.view.ViewGroup;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import com.george.backgammon.rendering.BoardMap;

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
    private TextView playerOneScoreText;
    private TextView playerTwoScoreText;
    private ImageView playerOneCheckerIcon;
    private ImageView playerTwoCheckerIcon;
    private View playerOnePanel;
    private View playerTwoPanel;
    private View statusPanel;
    private View gameplayRoot;
    private FrameLayout gameplayCanvas;
    private View playerOneDivider;
    private View playerTwoDivider;
    private TextView statusLeftOrnament;
    private TextView statusRightOrnament;
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
        playerOneScoreText = findViewById(R.id.playerOneScore);
        playerTwoScoreText = findViewById(R.id.playerTwoScore);
        playerOneCheckerIcon = findViewById(R.id.playerOneCheckerIcon);
        playerTwoCheckerIcon = findViewById(R.id.playerTwoCheckerIcon);
        playerOnePanel = findViewById(R.id.playerOnePanel);
        playerTwoPanel = findViewById(R.id.playerTwoPanel);
        statusPanel = findViewById(R.id.statusPanel);
        gameplayRoot = findViewById(R.id.gameplayRoot);
        gameplayCanvas = findViewById(R.id.gameplayCanvas);
        playerOneDivider = findViewById(R.id.playerOneDivider);
        playerTwoDivider = findViewById(R.id.playerTwoDivider);
        statusLeftOrnament = findViewById(R.id.statusLeftOrnament);
        statusRightOrnament = findViewById(R.id.statusRightOrnament);
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
        gameplayRoot.post(this::applyReferenceComposition);
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
                statusTitle.setText("Backgammon Legacy v1.6.3 • Wide-Screen Reference Match");
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
        if (gameplayRoot != null) gameplayRoot.post(this::applyReferenceComposition);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
            if (gameplayRoot != null) gameplayRoot.post(this::applyReferenceComposition);
        }
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

    /**
     * Project the approved 1672 x 941 gameplay reference into the actual safe landscape
     * window. Phones are commonly much wider than the reference artwork, so horizontal
     * and vertical coordinates intentionally use independent scales. This keeps the same
     * reference composition on-screen instead of shrinking the whole game into a narrow
     * centred 16:9 island. The BoardView receives the final displayed rectangle and its
     * BoardMap/BoardGeometry continue to scale from that rectangle.
     */
    private void applyReferenceComposition() {
        if (gameplayRoot == null || gameplayCanvas == null || boardView == null) return;

        final float REF_W = 1672f;
        final float REF_H = 941f;

        int rootW = gameplayRoot.getWidth();
        int rootH = gameplayRoot.getHeight();
        if (rootW <= 0 || rootH <= 0) return;

        int safeLeft = 0, safeTop = 0, safeRight = 0, safeBottom = 0;
        if (Build.VERSION.SDK_INT >= 28 && gameplayRoot.getRootWindowInsets() != null
                && gameplayRoot.getRootWindowInsets().getDisplayCutout() != null) {
            android.view.DisplayCutout cutout = gameplayRoot.getRootWindowInsets().getDisplayCutout();
            safeLeft = cutout.getSafeInsetLeft();
            safeTop = cutout.getSafeInsetTop();
            safeRight = cutout.getSafeInsetRight();
            safeBottom = cutout.getSafeInsetBottom();
        }

        int availableW = Math.max(1, rootW - safeLeft - safeRight);
        int availableH = Math.max(1, rootH - safeTop - safeBottom);
        float scaleX = availableW / REF_W;
        float scaleY = availableH / REF_H;
        float uiScale = Math.min(scaleX, scaleY);

        // The canvas now owns the complete safe app window. Reference coordinates are
        // projected into it instead of fitting a narrower 16:9 canvas inside the phone.
        FrameLayout.LayoutParams canvasLp = (FrameLayout.LayoutParams) gameplayCanvas.getLayoutParams();
        canvasLp.width = availableW;
        canvasLp.height = availableH;
        canvasLp.leftMargin = safeLeft;
        canvasLp.topMargin = safeTop;
        canvasLp.gravity = 0;
        gameplayCanvas.setLayoutParams(canvasLp);

        // Exact major rectangles measured from the approved reference. X/W follow the
        // safe screen width; Y/H follow the safe screen height.
        setFrame(playerOnePanel, 101, 14, 493, 80, scaleX, scaleY);
        setFrame(statusPanel,    613, 14, 424, 80, scaleX, scaleY);
        setFrame(playerTwoPanel,1053, 14, 495, 80, scaleX, scaleY);
        setFrame(boardView,      100,104,1448,691, scaleX, scaleY);

        setFrame(undoButton,       445,811,151,80, scaleX, scaleY);
        setFrame(mainActionButton, 611,811,432,80, scaleX, scaleY);
        setFrame(menuButton,      1060,811,151,80, scaleX, scaleY);

        // Keep circular/detail elements visually round. Their size follows the smaller
        // axis while horizontal spacing may use the extra width available on wide phones.
        int icon = Math.max(1, Math.round(57f * uiScale));
        int score = Math.max(1, Math.round(53f * uiScale));
        int sidePad = Math.max(2, Math.round(14f * scaleX));
        int iconGap = Math.max(2, Math.round(14f * scaleX));
        int dividerH = Math.max(1, Math.round(43f * scaleY));
        int dividerGap = Math.max(2, Math.round(14f * scaleX));

        applyHorizontalPanelMetrics((LinearLayout) playerOnePanel, true, icon, score,
                sidePad, iconGap, dividerH, dividerGap);
        applyHorizontalPanelMetrics((LinearLayout) playerTwoPanel, false, icon, score,
                sidePad, iconGap, dividerH, dividerGap);

        setTextPx(playerOneName, 35f * uiScale);
        setTextPx(playerTwoName, 35f * uiScale);
        setTextPx(playerOneScoreText, 30f * uiScale);
        setTextPx(playerTwoScoreText, 30f * uiScale);

        int ornamentW = Math.max(1, Math.round(34f * uiScale));
        setLinearWidth(statusLeftOrnament, ornamentW);
        setLinearWidth(statusRightOrnament, ornamentW);
        setTextPx(statusLeftOrnament, 16f * uiScale);
        setTextPx(statusRightOrnament, 16f * uiScale);
        setStatusTextSizeForCurrentMessage(uiScale);

        setTextPx(undoButton, 29f * uiScale);
        setTextPx(mainActionButton, 36f * uiScale);
        setTextPx(menuButton, 42f * uiScale);

        gameplayCanvas.requestLayout();
        boardView.requestLayout();
    }

    private void setFrame(View view, float x, float y, float w, float h,
                          float scaleX, float scaleY) {
        if (view == null) return;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.width = Math.max(1, Math.round(w * scaleX));
        lp.height = Math.max(1, Math.round(h * scaleY));
        lp.leftMargin = Math.round(x * scaleX);
        lp.topMargin = Math.round(y * scaleY);
        lp.gravity = 0;
        view.setLayoutParams(lp);
    }

    private void applyHorizontalPanelMetrics(LinearLayout panel, boolean playerOne,
                                             int iconPx, int scorePx, int sidePad,
                                             int iconGap, int dividerH, int dividerGap) {
        if (panel == null) return;
        panel.setPadding(sidePad, 0, sidePad, 0);

        ImageView iconView = playerOne ? playerOneCheckerIcon : playerTwoCheckerIcon;
        TextView scoreView = playerOne ? playerOneScoreText : playerTwoScoreText;
        View divider = playerOne ? playerOneDivider : playerTwoDivider;

        LinearLayout.LayoutParams iconLp = (LinearLayout.LayoutParams) iconView.getLayoutParams();
        iconLp.width = iconPx;
        iconLp.height = iconPx;
        if (playerOne) {
            iconLp.leftMargin = 0;
            iconLp.rightMargin = iconGap;
        } else {
            iconLp.leftMargin = iconGap;
            iconLp.rightMargin = 0;
        }
        iconView.setLayoutParams(iconLp);

        LinearLayout.LayoutParams scoreLp = (LinearLayout.LayoutParams) scoreView.getLayoutParams();
        scoreLp.width = scorePx;
        scoreLp.height = scorePx;
        scoreView.setLayoutParams(scoreLp);

        LinearLayout.LayoutParams dividerLp = (LinearLayout.LayoutParams) divider.getLayoutParams();
        dividerLp.width = Math.max(1, Math.round(1.5f * getResources().getDisplayMetrics().density));
        dividerLp.height = dividerH;
        dividerLp.leftMargin = dividerGap;
        dividerLp.rightMargin = dividerGap;
        divider.setLayoutParams(dividerLp);
    }

    private void setLinearWidth(View view, int widthPx) {
        if (view == null) return;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.width = widthPx;
        view.setLayoutParams(lp);
    }

    private void setTextPx(TextView view, float px) {
        if (view != null) view.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(1f, px));
    }

    private void setStatusTextSizeForCurrentMessage(float scale) {
        if (statusTitle == null) return;
        String text = String.valueOf(statusTitle.getText());
        float designPx = 35f;
        if (text.length() > 20) designPx = 29f;
        else if (text.length() > 17) designPx = 32f;
        setTextPx(statusTitle, designPx * scale);
        statusTitle.setEllipsize(null);
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
                    ? name + " Wins Match"
                    : name + " Wins Game");
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
        if (gameplayCanvas != null && gameplayCanvas.getHeight() > 0) {
            setStatusTextSizeForCurrentMessage(gameplayCanvas.getHeight() / 941f);
        }
        boolean opening = !game.isOpeningResolved();
        boolean p1Active = !opening && game.getWinner() == 0 && game.getCurrentPlayer() == playerOneSide;
        boolean p2Active = !opening && game.getWinner() == 0 && game.getCurrentPlayer() == -playerOneSide;
        playerOnePanel.setBackgroundResource(p1Active ? R.drawable.bg_panel_active : R.drawable.bg_panel);
        playerTwoPanel.setBackgroundResource(p2Active ? R.drawable.bg_panel_active : R.drawable.bg_panel);

        playerOneScoreText.setText(String.valueOf(playerOneScore));
        playerTwoScoreText.setText(String.valueOf(playerTwoScore));
    }

    private int offFor(int player) {
        return player == BackgammonGame.WHITE ? game.getWhiteOff() : game.getBlackOff();
    }

    private int barFor(int player) {
        return player == BackgammonGame.WHITE ? game.getWhiteBar() : game.getBlackBar();
    }
}
