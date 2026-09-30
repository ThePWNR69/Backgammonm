package com.george.backgammon.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.george.backgammon.R;
import com.george.backgammon.cosmetics.CheckerTheme;
import com.george.backgammon.game.BackgammonGame;

/**
 * v1.19 premium pre-match setup.
 *
 * Dropdowns were deliberately removed. Every setting uses a compact left/right sliding selector,
 * which keeps the screen readable in landscape and makes all choices visible without pop-up menus.
 */
public class MatchSetupActivity extends Activity {
    public static final String EXTRA_MODE = "setup_mode";

    private static final int CREAM = Color.rgb(255, 237, 194);
    private static final int GOLD = Color.rgb(244, 202, 111);
    private static final int MUTED = Color.rgb(229, 214, 181);

    private String mode;
    private SlidingChoice versionChoice;
    private SlidingChoice difficultyChoice;
    private SlidingChoice directionChoice;
    private SlidingChoice opponentColourChoice;
    private SlidingChoice matchChoice;
    private SlidingChoice hintsChoice;
    private LinearLayout rulesContainer;
    private TextView opponentColourLabel;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = GameActivity.MODE_AI;
        setContentView(buildUi());
        updateRules();
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(7), dp(12), dp(9));
        root.setBackgroundResource(R.drawable.tabletop);

        // Compact header: gives the option cards more breathing room than the previous screen.
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(44)));

        ImageButton back = new ImageButton(this);
        back.setImageResource(R.drawable.ic_back);
        back.setPadding(dp(8), dp(8), dp(8), dp(8));
        back.setBackgroundResource(R.drawable.setup_back_button_selector);
        back.setColorFilter(CREAM);
        back.setElevation(dp(5));
        back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(42), dp(40)));

        TextView title = text(GameActivity.MODE_TWO_PLAYER.equals(mode) ? "2-PLAYER SETUP" : "VS BOT SETUP", 25, true);
        title.setShadowLayer(dp(2), 0f, dp(2), Color.argb(170, 0, 0, 0));
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.045f);
        header.addView(title, new LinearLayout.LayoutParams(0, -1, 1f));
        header.addView(new View(this), new LinearLayout.LayoutParams(dp(42), 1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(-1, 0, 1f);
        contentLp.topMargin = dp(5);
        root.addView(content, contentLp);

        LinearLayout left = panel();
        LinearLayout right = panel();
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(0, -1, 1.10f);
        leftLp.setMargins(dp(3), 0, dp(6), 0);
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(0, -1, 0.90f);
        rightLp.setMargins(dp(6), 0, dp(3), 0);
        content.addView(left, leftLp);
        content.addView(right, rightLp);

        versionChoice = addChoice(left, "⚄", "Game version", new String[]{
                "Backgammon / Sheish Beish", "Mahbouseh", "Tawla 31"
        });
        versionChoice.setOnChangedListener(index -> {
            updateMatchLengthForVariant(index);
            updateRules();
        });

        if (!GameActivity.MODE_TWO_PLAYER.equals(mode)) {
            difficultyChoice = addChoice(left, "▥", "Difficulty", new String[]{"Easy", "Normal", "Hard"});
            difficultyChoice.setIndex(1, false);
        }

        directionChoice = addChoice(left, "↺",
                GameActivity.MODE_TWO_PLAYER.equals(mode) ? "Player 1 direction" : "Your direction",
                new String[]{"Counter-clockwise", "Clockwise"});

        opponentColourChoice = addChoice(left, "◐",
                GameActivity.MODE_TWO_PLAYER.equals(mode) ? "Player 2 colour" : "Bot colour",
                new String[]{"Auto", "Light", "Dark"});
        opponentColourLabel = opponentColourChoice.labelView;

        matchChoice = addChoice(left, "♛", "Match length", new String[]{
                "1 Match", "First to 3", "First to 5", "First to 7"
        });

        hintsChoice = addChoice(left, "?", "Hints", new String[]{"On", "Off"});

        TextView rulesTitle = text("MATCH RULES", 17, true);
        rulesTitle.setGravity(Gravity.CENTER);
        rulesTitle.setLetterSpacing(0.045f);
        rulesTitle.setShadowLayer(dp(2), 0f, dp(1), Color.argb(150, 0, 0, 0));
        right.addView(rulesTitle, new LinearLayout.LayoutParams(-1, dp(33)));
        addDivider(right);

        rulesContainer = new LinearLayout(this);
        rulesContainer.setOrientation(LinearLayout.VERTICAL);
        right.addView(rulesContainer, new LinearLayout.LayoutParams(-1, 0, 1f));

        Button start = new Button(this);
        start.setText("START MATCH");
        start.setAllCaps(false);
        start.setTextColor(Color.rgb(255, 240, 207));
        start.setTextSize(18);
        start.setTypeface(Typeface.SERIF, Typeface.BOLD);
        start.setBackgroundResource(R.drawable.setup_start_button_selector);
        start.setShadowLayer(dp(2), 0f, dp(1), Color.argb(150, 0, 0, 0));
        start.setElevation(dp(5));
        start.setStateListAnimator(null);
        start.setOnClickListener(v -> startMatch());
        LinearLayout.LayoutParams startLp = new LinearLayout.LayoutParams(-1, dp(45));
        startLp.topMargin = dp(5);
        right.addView(start, startLp);

        return root;
    }

    /** One clean row: icon, label, then an animated left/right selector. */
    private SlidingChoice addChoice(LinearLayout parent, String icon, String label, String[] choices) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(7), dp(2), dp(7), dp(2));

        TextView badge = text(icon, 19, true);
        badge.setGravity(Gravity.CENTER);
        badge.setTextColor(GOLD);
        badge.setBackgroundResource(R.drawable.setup_icon_badge);
        badge.setShadowLayer(dp(1), 0f, dp(1), Color.argb(140, 0, 0, 0));
        row.addView(badge, new LinearLayout.LayoutParams(dp(36), dp(36)));

        TextView l = text(label, 11, true);
        l.setTextColor(CREAM);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setShadowLayer(dp(1), 0f, dp(1), Color.argb(125, 0, 0, 0));
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(dp(118), -1);
        labelLp.leftMargin = dp(10);
        row.addView(l, labelLp);

        SlidingChoice selector = new SlidingChoice(choices, l);
        LinearLayout.LayoutParams selectorLp = new LinearLayout.LayoutParams(0, dp(36), 1f);
        selectorLp.leftMargin = dp(6);
        row.addView(selector, selectorLp);

        parent.addView(row, new LinearLayout.LayoutParams(-1, dp(46)));
        addDivider(parent);
        return selector;
    }

    private void updateMatchLengthForVariant(int variantIndex) {
        if (matchChoice == null) return;
        if (variantIndex == 2) {
            matchChoice.setValues(new String[]{"31 Points"});
        } else {
            int old = matchChoice.getIndex();
            matchChoice.setValues(new String[]{"1 Match", "First to 3", "First to 5", "First to 7"});
            matchChoice.setIndex(Math.min(old, 3), false);
        }
    }

    /** Rules text is variant-specific rather than displaying all variants at once. */
    private void updateRules() {
        if (rulesContainer == null || versionChoice == null) return;
        rulesContainer.removeAllViews();
        int v = versionChoice.getIndex();

        addRule(rulesContainer, "↔", "The two sides always travel in opposite directions.");
        if (v == 0) {
            addRule(rulesContainer, "⚄", "Standard opening roll: each side rolls one die; ties reroll and the higher die starts.");
            addRule(rulesContainer, "●", "A lone opposing checker can be hit and sent to the bar. Bar checkers must re-enter first.");
            addRule(rulesContainer, "⌂", "Bear off once all of your remaining checkers are in your home board.");
        } else if (v == 1) {
            addRule(rulesContainer, "●", "All 15 checkers begin together on the starting point.");
            addRule(rulesContainer, "⊙", "Landing on one lone opposing checker pins it instead of sending it to the bar.");
            addRule(rulesContainer, "⌂", "There is no standard bar-hit cycle; race, block, pin, then bear off to win.");
        } else {
            addRule(rulesContainer, "●", "All 15 checkers begin together and opposing occupied points are blocked.");
            addRule(rulesContainer, "↝", "No hitting: the opening runner must be released before the rest of that side can advance.");
            addRule(rulesContainer, "31", "The match target is fixed at 31 points for Tawla 31.");
        }
        addRule(rulesContainer, "★", "XP and Gold are earned after every completed game. Hints reduce bonus rewards, not the protected participation reward.");
    }

    private void addRule(LinearLayout parent, String icon, String rule) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(2), dp(6), dp(2));

        TextView badge = text(icon, icon.length() > 1 ? 11 : 17, true);
        badge.setTextColor(GOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(R.drawable.setup_icon_badge);
        badge.setShadowLayer(dp(1), 0f, dp(1), Color.argb(140, 0, 0, 0));
        row.addView(badge, new LinearLayout.LayoutParams(dp(33), dp(33)));

        TextView body = text(rule, 9, false);
        body.setTextColor(MUTED);
        body.setGravity(Gravity.CENTER_VERTICAL);
        body.setLineSpacing(0f, 1.04f);
        body.setShadowLayer(dp(1), 0f, dp(1), Color.argb(105, 0, 0, 0));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1f);
        bp.leftMargin = dp(9);
        row.addView(body, bp);
        parent.addView(row, new LinearLayout.LayoutParams(-1, 0, 1f));
        addDivider(parent);
    }

    private void addDivider(LinearLayout parent) {
        View line = new View(this);
        line.setBackgroundResource(R.drawable.setup_divider);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(1));
        lp.leftMargin = dp(5);
        lp.rightMargin = dp(5);
        parent.addView(line, lp);
    }

    private void startMatch() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_MODE, mode);

        int direction = directionChoice.getIndex();
        int p1Side = direction == 0 ? BackgammonGame.WHITE : BackgammonGame.BLACK;
        intent.putExtra(GameActivity.EXTRA_PLAYER_ONE_SIDE, p1Side);

        // The user's checker side is a Customisation preference. Match Setup only decides the
        // opponent side. Auto always chooses the opposite of the equipped player side.
        SharedPreferences visuals = getSharedPreferences("backgammon_visuals", MODE_PRIVATE);
        String equippedSide = visuals.getString("checker_side", CheckerTheme.SIDE_LIGHT);
        boolean userIsLight = !CheckerTheme.SIDE_DARK.equals(equippedSide);
        int opponentChoice = opponentColourChoice.getIndex(); // 0 auto, 1 light, 2 dark
        if (opponentChoice == 1) userIsLight = false;          // opponent explicitly light
        else if (opponentChoice == 2) userIsLight = true;      // opponent explicitly dark
        intent.putExtra(GameActivity.EXTRA_PLAYER_ONE_LIGHT, userIsLight);

        int variantIndex = versionChoice.getIndex();
        intent.putExtra(GameActivity.EXTRA_MATCH_TARGET,
                variantIndex == 2 ? 31 : matchTarget(matchChoice.getIndex()));
        intent.putExtra(GameActivity.EXTRA_GAME_VARIANT, variantIndex);
        intent.putExtra(GameActivity.EXTRA_HINTS_ENABLED, hintsChoice.getIndex() == 0);
        if (difficultyChoice != null) {
            intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficultyChoice.getIndex());
        }
        startActivity(intent);
    }

    private static int matchTarget(int index) {
        if (index == 1) return 3;
        if (index == 2) return 5;
        if (index == 3) return 7;
        return 1;
    }

    private LinearLayout panel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(8), dp(10), dp(9));
        panel.setBackgroundResource(R.drawable.setup_panel);
        panel.setElevation(dp(6));
        return panel;
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(CREAM);
        v.setTextSize(sp);
        v.setTypeface(Typeface.SERIF, bold ? Typeface.BOLD : Typeface.NORMAL);
        v.setIncludeFontPadding(false);
        return v;
    }

    /**
     * Compact carousel selector. The selected value slides horizontally when a chevron is pressed,
     * replacing the old Android Spinner/dropdown completely.
     */
    private final class SlidingChoice extends LinearLayout {
        private String[] values;
        private int index = 0;
        private final TextView valueView;
        private final TextView labelView;
        private OnChangedListener changedListener;

        SlidingChoice(String[] initialValues, TextView labelView) {
            super(MatchSetupActivity.this);
            this.values = initialValues;
            this.labelView = labelView;
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setBackgroundResource(R.drawable.setup_selector);
            setElevation(dp(3));
            setPadding(dp(2), 0, dp(2), 0);
            setClipChildren(true);

            TextView left = arrow("‹");
            valueView = text(initialValues[0], 12, false);
            valueView.setGravity(Gravity.CENTER);
            valueView.setTextColor(CREAM);
            valueView.setShadowLayer(dp(1), 0f, dp(1), Color.argb(140, 0, 0, 0));
            valueView.setSingleLine(true);
            valueView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            TextView right = arrow("›");

            addView(left, new LayoutParams(dp(36), -1));
            View leftDivider = new View(MatchSetupActivity.this);
            leftDivider.setBackgroundResource(R.drawable.setup_separator_vertical);
            addView(leftDivider, new LayoutParams(dp(1), dp(25)));
            addView(valueView, new LayoutParams(0, -1, 1f));
            View rightDivider = new View(MatchSetupActivity.this);
            rightDivider.setBackgroundResource(R.drawable.setup_separator_vertical);
            addView(rightDivider, new LayoutParams(dp(1), dp(25)));
            addView(right, new LayoutParams(dp(36), -1));

            left.setOnClickListener(v -> cycle(-1));
            right.setOnClickListener(v -> cycle(1));
            valueView.setOnClickListener(v -> cycle(1));
        }

        private TextView arrow(String glyph) {
            TextView v = text(glyph, 23, true);
            v.setGravity(Gravity.CENTER);
            v.setTextColor(GOLD);
            v.setClickable(true);
            v.setBackgroundResource(R.drawable.setup_selector_arrow);
            v.setShadowLayer(dp(1), 0f, dp(1), Color.argb(140, 0, 0, 0));
            return v;
        }

        void cycle(int direction) {
            if (values.length <= 1) return;
            final float distance = dp(24) * (direction > 0 ? -1f : 1f);
            valueView.animate().cancel();
            setBackgroundResource(R.drawable.setup_selector_pressed);
            valueView.animate()
                    .translationX(distance)
                    .alpha(0f)
                    .setDuration(85)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        index = (index + direction + values.length) % values.length;
                        valueView.setText(values[index]);
                        valueView.setTranslationX(-distance);
                        valueView.animate()
                                .translationX(0f)
                                .alpha(1f)
                                .setDuration(125)
                                .setInterpolator(new DecelerateInterpolator())
                                .withEndAction(() -> setBackgroundResource(R.drawable.setup_selector))
                                .start();
                        if (changedListener != null) changedListener.onChanged(index);
                    }).start();
        }

        int getIndex() { return index; }

        void setIndex(int newIndex, boolean notify) {
            if (values.length == 0) return;
            index = Math.max(0, Math.min(newIndex, values.length - 1));
            valueView.setText(values[index]);
            if (notify && changedListener != null) changedListener.onChanged(index);
        }

        void setValues(String[] newValues) {
            values = newValues;
            index = 0;
            valueView.setText(values.length == 0 ? "" : values[0]);
        }

        void setOnChangedListener(OnChangedListener listener) { changedListener = listener; }
    }

    private interface OnChangedListener { void onChanged(int index); }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onResume() { super.onResume(); enterImmersiveMode(); }
    @Override public void onWindowFocusChanged(boolean hasFocus) { super.onWindowFocusChanged(hasFocus); if (hasFocus) enterImmersiveMode(); }
    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
