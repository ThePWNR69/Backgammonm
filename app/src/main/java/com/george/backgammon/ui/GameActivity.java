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
import com.george.backgammon.cosmetics.UiTheme;
import com.george.backgammon.cosmetics.UiThemeCatalog;
import com.george.backgammon.animation.MoveAnimationStyle;
import com.george.backgammon.game.BackgammonGame;
import com.george.backgammon.game.Move;
import com.george.backgammon.game.GameVariant;
import com.george.backgammon.progression.MatchReward;
import com.george.backgammon.progression.RewardCalculator;
import com.george.backgammon.progression.ProgressStore;
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
    public static final String EXTRA_GAME_VARIANT = "game_variant";
    public static final String EXTRA_HINTS_ENABLED = "hints_enabled";

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
    private View bottomControlBar;
    private UiTheme uiTheme = UiThemeCatalog.defaultTheme();
    private FrameLayout gameplayCanvas;
    private View playerOneDivider;
    private View playerTwoDivider;
    private TextView statusLeftOrnament;
    private TextView statusRightOrnament;
    private Button mainActionButton;
    private Button undoButton;
    private Button menuButton;
    private Button settingsButton;
    private Button hintButton;
    private ImageView dieOneView;
    private ImageView dieTwoView;
    private int difficultyLevel = 1;
    private GameVariant gameVariant = GameVariant.BACKGAMMON;
    private boolean hintsEnabled = true;
    private int hintsUsedThisGame = 0;
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
    private boolean openingResultVisible = false;
    private String openingResultText = "";
    private float gameplayUiScale = 1f;

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
        difficultyLevel = difficulty;
        int variantIndex = getIntent().getIntExtra(EXTRA_GAME_VARIANT, 0);
        gameVariant = variantIndex == 1 ? GameVariant.MAHBOUSEH : (variantIndex == 2 ? GameVariant.TAWLA_31 : GameVariant.BACKGAMMON);
        hintsEnabled = getIntent().getBooleanExtra(EXTRA_HINTS_ENABLED, true);
        double aiNoise = difficulty <= 0 ? 12.0 : (difficulty >= 2 ? 0.0 : 0.35);
        aiStrategy = new PositionalAi(aiNoise);

        game = new BackgammonGame(gameVariant);
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
        bottomControlBar = findViewById(R.id.bottomControlBar);
        gameplayCanvas = findViewById(R.id.gameplayCanvas);
        playerOneDivider = findViewById(R.id.playerOneDivider);
        playerTwoDivider = findViewById(R.id.playerTwoDivider);
        statusLeftOrnament = findViewById(R.id.statusLeftOrnament);
        statusRightOrnament = findViewById(R.id.statusRightOrnament);
        mainActionButton = findViewById(R.id.mainActionButton);
        undoButton = findViewById(R.id.undoButton);
        menuButton = findViewById(R.id.menuButton);
        settingsButton = findViewById(R.id.settingsButton);
        hintButton = findViewById(R.id.hintButton);
        dieOneView = findViewById(R.id.dieOneView);
        dieTwoView = findViewById(R.id.dieTwoView);
        applyUiTheme();
        if (playerOneCheckerIcon != null) playerOneCheckerIcon.setVisibility(View.INVISIBLE);
        if (playerTwoCheckerIcon != null) playerTwoCheckerIcon.setVisibility(View.INVISIBLE);
        if (statusLeftOrnament != null) statusLeftOrnament.setVisibility(View.GONE);
        if (statusRightOrnament != null) statusRightOrnament.setVisibility(View.GONE);

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
        settingsButton.setOnClickListener(this::showGameMenu);
        hintButton.setOnClickListener(v -> {
            if (boardView.isAnimating() || isAiTurn() || !game.hasRolled()) return;
            if (!hintsEnabled) return;
            List<Move> moves = game.getAllowedFirstMoves();
            if (!moves.isEmpty()) {
                hintsUsedThisGame++;
                boardView.showHint(moves.get(0));
            }
        });
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
        openingResultVisible = false;
        openingResultText = "";
        boardView.setOpeningPresentationActive(true);
        boardView.clearSelection();
        boardView.animateDiceRoll(d1, d2, () -> {
            openingResultText = buildOpeningResultText();
            openingResultVisible = true;
            refreshUi();
            handler.postDelayed(() -> {
                openingResultVisible = false;
                boardView.setOpeningPresentationActive(false);
                refreshUi();
                if (game.isOpeningResolved()) scheduleAiIfNeeded();
            }, 1050L);
        });
        refreshUi();
    }

    private String buildOpeningResultText() {
        int p1Die = playerOneSide == BackgammonGame.WHITE ? game.getDieOne() : game.getDieTwo();
        int p2Die = playerOneSide == BackgammonGame.WHITE ? game.getDieTwo() : game.getDieOne();
        String p1 = versusAi ? "You" : "P1";
        String p2 = versusAi ? "AI" : "P2";
        if (!game.isOpeningResolved()) return p1 + " " + p1Die + " • " + p2Die + " " + p2 + " • Tie";
        return p1 + " " + p1Die + " • " + p2Die + " " + p2 + " • " + displayName(game.getCurrentPlayer()) + " First";
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
                statusTitle.setText("Backgammon Legacy v1.10.0 • Approved Premium Layout");
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
        // The approved HUD uses identity icons, while checker colours stay on the board itself.
        if (playerOneCheckerIcon != null) {
            playerOneCheckerIcon.setImageResource(R.drawable.ic_profile);
            playerOneCheckerIcon.setColorFilter(0xFFFFD47A);
        }
        if (playerTwoCheckerIcon != null) {
            playerTwoCheckerIcon.setImageResource(versusAi ? R.drawable.ic_robot : R.drawable.ic_profile);
            playerTwoCheckerIcon.setColorFilter(versusAi ? 0xFF5CB8FF : 0xFFFFD47A);
        }
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
        openingResultVisible = false;
        openingResultText = "";
        boardView.setOpeningPresentationActive(false);
        game.reset();
        hintsUsedThisGame = 0;
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
     * v1.16.0 layered-image composition.
     *
     * The approved 1672 x 941 design is the master coordinate system. Every visual layer is
     * placed in that coordinate system and the entire canvas scales uniformly. Individual
     * board/HUD/button assets are never independently repositioned to "fit" a device.
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
        float scale = Math.min(availableW / REF_W, availableH / REF_H);
        int canvasW = Math.max(1, Math.round(REF_W * scale));
        int canvasH = Math.max(1, Math.round(REF_H * scale));
        int canvasX = safeLeft + (availableW - canvasW) / 2;
        int canvasY = safeTop + (availableH - canvasH) / 2;

        FrameLayout.LayoutParams canvasLp = (FrameLayout.LayoutParams) gameplayCanvas.getLayoutParams();
        canvasLp.width = canvasW;
        canvasLp.height = canvasH;
        canvasLp.leftMargin = canvasX;
        canvasLp.topMargin = canvasY;
        canvasLp.gravity = 0;
        gameplayCanvas.setLayoutParams(canvasLp);

        // Top HUD — exact approved reference placement.
        setFrameRef(menuButton,       108f, 22f,  88f,  86f, scale);
        setFrameRef(playerOnePanel,   210f, 22f, 450f,  86f, scale);
        setFrameRef(statusPanel,      670f, 18f, 342f,  92f, scale);
        setFrameRef(playerTwoPanel,  1022f, 22f, 432f,  86f, scale);
        setFrameRef(settingsButton,  1464f, 22f,  88f,  86f, scale);

        // Board skin. Dynamic checkers/highlights are rendered by BackgammonBoardView on top.
        setFrameRef(boardView,        104f, 118f, 1442f, 615f, scale);

        // Bottom deck — background, then live dice and independently stateful buttons.
        setFrameRef(bottomControlBar, 108f, 752f, 1435f, 126f, scale);
        setFrameRef(dieOneView,       223f, 786f,   80f,  78f, scale);
        setFrameRef(dieTwoView,       331f, 786f,   81f,  78f, scale);
        setFrameRef(mainActionButton, 571f, 780f,  451f,  87f, scale);
        setFrameRef(undoButton,      1090f, 782f,  197f,  82f, scale);
        setFrameRef(hintButton,      1309f, 782f,  195f,  82f, scale);

        gameplayUiScale = scale;

        // Player plaque internals: avatar/robot art is part of the image asset, so invisible
        // spacer views reserve those exact areas while all text remains live Android text.
        int icon = Math.max(1, Math.round(82f * scale));
        int score = Math.max(1, Math.round(92f * scale));
        int sidePad = Math.max(1, Math.round(5f * scale));
        int iconGap = Math.max(1, Math.round(6f * scale));
        int dividerH = Math.max(1, Math.round(50f * scale));
        int dividerGap = Math.max(1, Math.round(4f * scale));
        applyHorizontalPanelMetrics((LinearLayout) playerOnePanel, true, icon, score,
                sidePad, iconGap, dividerH, dividerGap);
        applyHorizontalPanelMetrics((LinearLayout) playerTwoPanel, false, icon, score,
                sidePad, iconGap, dividerH, dividerGap);

        setTextPx(playerOneName, 30f * scale);
        setTextPx(playerTwoName, 28f * scale);
        setTextPx(playerOneScoreText, 32f * scale);
        setTextPx(playerTwoScoreText, 32f * scale);
        setStatusTextSizeForCurrentMessage(scale);

        setTextPx(mainActionButton, 31f * scale);
        setTextPx(undoButton, 22f * scale);
        setTextPx(hintButton, 22f * scale);

        gameplayCanvas.requestLayout();
        boardView.requestLayout();
    }

    private void setFrameRef(View view, float x, float y, float w, float h, float scale) {
        setFramePx(view, Math.round(x * scale), Math.round(y * scale),
                Math.round(w * scale), Math.round(h * scale));
    }

    private void setFramePx(View view, int x, int y, int w, int h) {
        if (view == null) return;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.width = Math.max(1, w);
        lp.height = Math.max(1, h);
        lp.leftMargin = x;
        lp.topMargin = y;
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
        float designPx = 27f;
        if (text.length() > 20) designPx = 21f;
        else if (text.length() > 16) designPx = 24f;
        setTextPx(statusTitle, designPx * scale);
        statusTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
    }

    private void applyUiTheme() {
        if (uiTheme == null) uiTheme = UiThemeCatalog.defaultTheme();
        if (playerOnePanel != null) playerOnePanel.setBackgroundResource(uiTheme.playerOnePanelDrawable);
        if (playerTwoPanel != null) playerTwoPanel.setBackgroundResource(uiTheme.playerTwoPanelDrawable);
        if (statusPanel != null) statusPanel.setBackgroundResource(uiTheme.statusBarDrawable);
        if (bottomControlBar != null) bottomControlBar.setBackgroundResource(uiTheme.bottomBarDrawable);
        if (mainActionButton != null) mainActionButton.setBackgroundResource(uiTheme.primaryButtonDrawable);
        if (undoButton != null) undoButton.setBackgroundResource(uiTheme.secondaryButtonDrawable);
        if (hintButton != null) hintButton.setBackgroundResource(uiTheme.hintButtonDrawable);
        if (menuButton != null) menuButton.setBackgroundResource(uiTheme.menuButtonDrawable);
        if (settingsButton != null) settingsButton.setBackgroundResource(uiTheme.settingsButtonDrawable);
    }

    private void refreshUi() {
        boardView.invalidate();
        updateDiceControls();
        if (hintButton != null) hintButton.setEnabled(false);
        boolean aiTurn = isAiTurn();
        boardView.setInputEnabled(!aiTurn && !aiBusy && game.isOpeningResolved());

        playerOneName.setText("Player 1");
        playerTwoName.setText(versusAi ? "AI - " + difficultyName() : "Player 2");

        int winner = game.getWinner();
        if (winner != 0) {
            if (!winnerScored) {
                int roundPoints = pointsForRound(winner);
                if (winner == playerOneSide) playerOneScore += roundPoints; else playerTwoScore += roundPoints;
                awardProgression(winner);
                winnerScored = true;
            }
            boolean matchOver = playerOneScore >= matchTarget || playerTwoScore >= matchTarget;
            String name = displayName(winner);
            statusTitle.setText(matchOver
                    ? name + " Wins Match"
                    : name + " Wins Game");
            mainActionButton.setText(matchOver ? "MATCH OVER" : "NEXT GAME");
            mainActionButton.setBackgroundResource(uiTheme.primaryButtonDrawable);
            mainActionButton.setEnabled(!matchOver);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        if (boardView.isDiceRolling()) {
            statusTitle.setText(game.isOpeningResolved() ? "ROLLING…" : "OPENING ROLL…");
            mainActionButton.setText("ROLLING…");
            mainActionButton.setEnabled(false);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        if (openingResultVisible) {
            statusTitle.setText(openingResultText);
            mainActionButton.setText(game.isOpeningResolved() ? "FIRST PLAYER SET" : "Tie");
            mainActionButton.setBackgroundResource(uiTheme.primaryButtonDrawable);
            mainActionButton.setEnabled(false);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        if (!game.isOpeningResolved()) {
            if (game.wasOpeningTie()) {
                statusTitle.setText("TIE • ROLL AGAIN");
                mainActionButton.setText("⚄  ROLL AGAIN");
            } else {
                statusTitle.setText("ROLL TO DECIDE FIRST");
                mainActionButton.setText("⚄  ROLL");
            }
            mainActionButton.setBackgroundResource(uiTheme.primaryButtonDrawable);
            mainActionButton.setEnabled(!aiBusy);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        String currentName = displayName(game.getCurrentPlayer());
        if (aiTurn || aiBusy) {
            statusTitle.setText(game.hasRolled() ? "AI THINKING…" : "AI TURN");
            mainActionButton.setText("AI TURN");
            mainActionButton.setBackgroundResource(uiTheme.primaryButtonDrawable);
            mainActionButton.setEnabled(false);
            undoButton.setEnabled(false);
            updatePlayerPanels();
            return;
        }

        if (!game.hasRolled()) {
            statusTitle.setText(versusAi && game.getCurrentPlayer() == playerOneSide ? "YOUR TURN" : currentName + " Turn");
            mainActionButton.setText("⚄  ROLL");
            mainActionButton.setBackgroundResource(uiTheme.primaryButtonDrawable);
            mainActionButton.setEnabled(!boardView.isAnimating());
        } else {
            if (game.canEndTurn()) {
                statusTitle.setText(game.getMovesMadeThisTurn() > 0 ? "REVIEW MOVE" : "NO LEGAL MOVE");
            } else if (game.getMovesMadeThisTurn() == 0) {
                statusTitle.setText(currentName + " Starts • " + diceText());
            } else {
                statusTitle.setText(currentName + " • " + diceText());
            }
            mainActionButton.setText("⚄  END TURN");
            mainActionButton.setBackgroundResource(uiTheme.primaryButtonDrawable);
            mainActionButton.setEnabled(game.canEndTurn() && !boardView.isAnimating());
        }

        undoButton.setEnabled(game.canUndo() && !boardView.isAnimating());
        hintButton.setVisibility(hintsEnabled ? View.VISIBLE : View.INVISIBLE);
        hintButton.setEnabled(hintsEnabled && game.hasRolled() && !isAiTurn() && !boardView.isAnimating() && !game.getAllowedFirstMoves().isEmpty());
        updateDiceControls();
        updatePlayerPanels();
    }

    private int pointsForRound(int winner) {
        if (gameVariant != GameVariant.TAWLA_31) return 1;
        int loser = -winner;
        int loserOff = loser == BackgammonGame.WHITE ? game.getWhiteOff() : game.getBlackOff();
        return Math.max(1, 15 - loserOff);
    }

    private void awardProgression(int winner) {
        boolean playerOneWon = winner == playerOneSide;
        int borneOff = playerOneSide == BackgammonGame.WHITE ? game.getWhiteOff() : game.getBlackOff();
        MatchReward reward = RewardCalculator.calculate(playerOneWon, borneOff, difficultyLevel, hintsUsedThisGame, versusAi, gameVariant);
        ProgressStore.add(this, reward);
        int level = ProgressStore.getLevel(this);
        int gold = ProgressStore.getGold(this);
        String hintLine = hintsUsedThisGame > 0 ? "\nHint modifier: " + reward.hintPercent + "%" : "\nNo hints used";
        new AlertDialog.Builder(this)
                .setTitle(playerOneWon ? "Rewards Earned" : "Progress Earned")
                .setMessage("+" + reward.xp + " XP\n+" + reward.gold + " Gold" + hintLine +
                        "\n\nLevel " + level + "  •  " + gold + " Gold")
                .setPositiveButton("Continue", null)
                .show();
    }

    private String difficultyName() {
        if (difficultyLevel <= 0) return "Beginner";
        if (difficultyLevel >= 2) return "Hard";
        return "Normal";
    }

    private void updateDiceControls() {
        if (dieOneView == null || dieTwoView == null) return;
        boolean liveDice = game.hasRolled() || boardView.isDiceRolling();
        int[] faces = {0, R.drawable.die_1, R.drawable.die_2, R.drawable.die_3, R.drawable.die_4, R.drawable.die_5, R.drawable.die_6};
        if (!liveDice) {
            // Keep the two dice bays present like the approved control deck without implying a roll.
            dieOneView.setVisibility(View.VISIBLE);
            dieTwoView.setVisibility(View.VISIBLE);
            dieOneView.setImageResource(R.drawable.die_1);
            dieTwoView.setImageResource(R.drawable.die_1);
            dieOneView.setAlpha(0.28f);
            dieTwoView.setAlpha(0.28f);
            return;
        }
        dieOneView.setVisibility(View.VISIBLE);
        dieTwoView.setVisibility(View.VISIBLE);
        dieOneView.setAlpha(1f);
        dieTwoView.setAlpha(1f);
        int d1 = boardView.isDiceRolling() ? boardView.getAnimatedDieOne() : game.getDieOne();
        int d2 = boardView.isDiceRolling() ? boardView.getAnimatedDieTwo() : game.getDieTwo();
        d1 = Math.max(1, Math.min(6, d1));
        d2 = Math.max(1, Math.min(6, d2));
        dieOneView.setImageResource(faces[d1]);
        dieTwoView.setImageResource(faces[d2]);
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
            setStatusTextSizeForCurrentMessage(gameplayUiScale);
        }
        boolean opening = !game.isOpeningResolved();
        boolean p1Active = !opening && game.getWinner() == 0 && game.getCurrentPlayer() == playerOneSide;
        boolean p2Active = !opening && game.getWinner() == 0 && game.getCurrentPlayer() == -playerOneSide;
        playerOnePanel.setBackgroundResource(uiTheme.playerOnePanelDrawable);
        playerTwoPanel.setBackgroundResource(uiTheme.playerTwoPanelDrawable);

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
