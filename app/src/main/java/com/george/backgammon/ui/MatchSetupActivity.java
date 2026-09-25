package com.george.backgammon.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.george.backgammon.R;
import com.george.backgammon.game.BackgammonGame;

/** Premium pre-match setup shared by bot and local two-player matches. */
public class MatchSetupActivity extends Activity {
    public static final String EXTRA_MODE = "setup_mode";

    private static final int CREAM = Color.rgb(248, 229, 181);
    private static final int GOLD = Color.rgb(229, 194, 120);
    private static final int MUTED = Color.rgb(207, 195, 166);

    private String mode;
    private Spinner versionSpinner;
    private Spinner difficultySpinner;
    private Spinner directionSpinner;
    private Spinner colourSpinner;
    private Spinner matchSpinner;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = GameActivity.MODE_AI;
        setContentView(buildUi());
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(8), dp(14), dp(10));
        root.setBackgroundResource(R.drawable.tabletop);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(48)));

        ImageButton back = new ImageButton(this);
        back.setImageResource(R.drawable.ic_back);
        back.setPadding(dp(9), dp(9), dp(9), dp(9));
        back.setBackgroundResource(R.drawable.premium_small_button);
        back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(46), dp(44)));

        TextView title = text(GameActivity.MODE_TWO_PLAYER.equals(mode) ? "2-PLAYER SETUP" : "VS BOT SETUP", 25, true);
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.04f);
        header.addView(title, new LinearLayout.LayoutParams(0, -1, 1f));
        header.addView(new View(this), new LinearLayout.LayoutParams(dp(46), 1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(-1, 0, 1f);
        contentLp.topMargin = dp(6);
        root.addView(content, contentLp);

        LinearLayout left = panel();
        LinearLayout right = panel();
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(0, -1, 1.12f);
        leftLp.setMargins(dp(4), 0, dp(7), 0);
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(0, -1, 0.88f);
        rightLp.setMargins(dp(7), 0, dp(4), 0);
        content.addView(left, leftLp);
        content.addView(right, rightLp);

        versionSpinner = addChoice(left, "⚄", "Game version", new String[]{
                "Backgammon / Sheish Beish",
                "Mahbouseh — coming soon",
                "Tawle 31 — coming soon"
        });

        if (!GameActivity.MODE_TWO_PLAYER.equals(mode)) {
            difficultySpinner = addChoice(left, "▥", "Difficulty", new String[]{"Easy", "Normal", "Hard"});
            difficultySpinner.setSelection(1);
        }

        directionSpinner = addChoice(left, "↺", "Player 1 direction", new String[]{
                "Counter-clockwise", "Clockwise"
        });
        colourSpinner = addChoice(left, "◐", "Player 1 colour", new String[]{"Light", "Dark"});
        matchSpinner = addChoice(left, "♛", "Match length", new String[]{
                "1 Match", "First to 3", "First to 5", "First to 7"
        });

        TextView rulesTitle = text("MATCH RULES", 18, true);
        rulesTitle.setGravity(Gravity.CENTER);
        rulesTitle.setLetterSpacing(0.04f);
        right.addView(rulesTitle, new LinearLayout.LayoutParams(-1, dp(36)));
        addDivider(right);

        addRule(right, "↔", "Player 2 automatically travels in the opposite direction.");
        addRule(right, "◐", "Checker colour is visual only and does not change movement rules.");
        addRule(right, "⚄", "Opening roll: each side rolls one die; ties reroll; the higher die starts using both opening dice.");
        addRule(right, "▲", "Mahbouseh and Tawle 31 are prepared in setup, but this build enables standard Backgammon / Sheish Beish only.");

        View flex = new View(this);
        right.addView(flex, new LinearLayout.LayoutParams(-1, 0, 1f));

        Button start = new Button(this);
        start.setText("START MATCH");
        start.setAllCaps(false);
        start.setTextColor(Color.rgb(255, 240, 207));
        start.setTextSize(17);
        start.setTypeface(Typeface.SERIF, Typeface.BOLD);
        start.setBackgroundResource(R.drawable.button_green);
        start.setStateListAnimator(null);
        start.setOnClickListener(v -> startMatch());
        LinearLayout.LayoutParams startLp = new LinearLayout.LayoutParams(-1, dp(48));
        startLp.topMargin = dp(6);
        right.addView(start, startLp);

        return root;
    }

    private Spinner addChoice(LinearLayout parent, String icon, String label, String[] choices) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(2), dp(8), dp(2));

        TextView badge = text(icon, 21, true);
        badge.setGravity(Gravity.CENTER);
        badge.setTextColor(GOLD);
        badge.setBackgroundResource(R.drawable.profile_avatar_bg);
        row.addView(badge, new LinearLayout.LayoutParams(dp(36), dp(36)));

        LinearLayout middle = new LinearLayout(this);
        middle.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams middleLp = new LinearLayout.LayoutParams(0, -2, 1f);
        middleLp.leftMargin = dp(12);
        row.addView(middle, middleLp);

        TextView l = text(label, 11, true);
        l.setTextColor(CREAM);
        middle.addView(l, new LinearLayout.LayoutParams(-1, dp(17)));

        Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN);
        spinner.setAdapter(new PremiumSpinnerAdapter(choices));
        spinner.setBackgroundResource(R.drawable.premium_tab_selected);
        spinner.setPadding(dp(12), 0, dp(8), 0);
        middle.addView(spinner, new LinearLayout.LayoutParams(-1, dp(31)));

        parent.addView(row, new LinearLayout.LayoutParams(-1, dp(51)));
        addDivider(parent);
        return spinner;
    }

    private void addRule(LinearLayout parent, String icon, String rule) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(7), dp(2), dp(7), dp(2));

        TextView badge = text(icon, 19, true);
        badge.setTextColor(GOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(R.drawable.profile_avatar_bg);
        row.addView(badge, new LinearLayout.LayoutParams(dp(36), dp(36)));

        TextView body = text(rule, 10, false);
        body.setTextColor(MUTED);
        body.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1f);
        bp.leftMargin = dp(11);
        row.addView(body, bp);
        parent.addView(row, new LinearLayout.LayoutParams(-1, dp(44)));
        addDivider(parent);
    }

    private void addDivider(LinearLayout parent) {
        View line = new View(this);
        line.setBackgroundColor(Color.argb(90, 196, 153, 79));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(1));
        lp.leftMargin = dp(6);
        lp.rightMargin = dp(6);
        parent.addView(line, lp);
    }

    private void startMatch() {
        if (versionSpinner.getSelectedItemPosition() != 0) {
            Toast.makeText(this, "That ruleset is not playable yet.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_MODE, mode);
        int direction = directionSpinner.getSelectedItemPosition();
        int p1Side = direction == 0 ? BackgammonGame.WHITE : BackgammonGame.BLACK;
        intent.putExtra(GameActivity.EXTRA_PLAYER_ONE_SIDE, p1Side);
        intent.putExtra(GameActivity.EXTRA_PLAYER_ONE_LIGHT, colourSpinner.getSelectedItemPosition() == 0);
        intent.putExtra(GameActivity.EXTRA_MATCH_TARGET, matchTarget(matchSpinner.getSelectedItemPosition()));
        if (difficultySpinner != null) {
            intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficultySpinner.getSelectedItemPosition());
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
        panel.setPadding(dp(12), dp(9), dp(12), dp(10));
        panel.setBackgroundResource(R.drawable.premium_card);
        return panel;
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(CREAM);
        v.setTextSize(sp);
        v.setTypeface(Typeface.SERIF, bold ? Typeface.BOLD : Typeface.NORMAL);
        return v;
    }

    private class PremiumSpinnerAdapter extends ArrayAdapter<String> {
        PremiumSpinnerAdapter(String[] values) {
            super(MatchSetupActivity.this, android.R.layout.simple_spinner_item, values);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        private TextView style(TextView view, boolean dropdown) {
            view.setTextColor(dropdown ? Color.rgb(35, 22, 14) : Color.rgb(255, 239, 205));
            view.setTextSize(dropdown ? 15 : 14);
            view.setTypeface(Typeface.SERIF, Typeface.NORMAL);
            view.setGravity(Gravity.CENTER_VERTICAL);
            if (!dropdown) view.setPadding(dp(2), 0, dp(4), 0);
            return view;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            return style((TextView) super.getView(position, convertView, parent), false);
        }

        @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return style((TextView) super.getDropDownView(position, convertView, parent), true);
        }
    }

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
