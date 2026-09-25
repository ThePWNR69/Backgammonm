package com.george.backgammon.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.george.backgammon.R;
import com.george.backgammon.game.BackgammonGame;

/** Lightweight pre-match setup. Core options are functional; regional rulesets are surfaced but
 * remain disabled until their dedicated rules engines are added. */
public class MatchSetupActivity extends Activity {
    public static final String EXTRA_MODE = "setup_mode";

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
        int pad = dp(14);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, dp(10), pad, dp(10));
        root.setBackgroundResource(R.drawable.screen_green_background);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(44)));

        ImageButton back = new ImageButton(this);
        back.setImageResource(R.drawable.ic_back);
        back.setPadding(dp(9), dp(9), dp(9), dp(9));
        back.setBackgroundResource(R.drawable.premium_small_button);
        back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(42), dp(40)));

        TextView title = text(GameActivity.MODE_TWO_PLAYER.equals(mode) ? "2-PLAYER SETUP" : "VS BOT SETUP", 23, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, -1, 1f);
        header.addView(title, titleLp);
        View spacer = new View(this);
        header.addView(spacer, new LinearLayout.LayoutParams(dp(42), 1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(-1, 0, 1f);
        contentLp.topMargin = dp(8);
        root.addView(content, contentLp);

        LinearLayout left = panel();
        LinearLayout right = panel();
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, -1, 1f);
        half.setMargins(dp(6), 0, dp(6), 0);
        content.addView(left, half);
        content.addView(right, half);

        versionSpinner = addChoice(left, "Game version", new String[]{
                "Backgammon / Sheish Beish",
                "Mahbouseh — rules pack coming soon",
                "Tawle 31 — rules pack coming soon"
        });

        if (!GameActivity.MODE_TWO_PLAYER.equals(mode)) {
            difficultySpinner = addChoice(left, "Difficulty", new String[]{"Easy", "Normal", "Hard"});
            difficultySpinner.setSelection(1);
        }

        directionSpinner = addChoice(left, "Player 1 direction", new String[]{
                "↺ Counter-clockwise", "↻ Clockwise"
        });
        colourSpinner = addChoice(left, "Player 1 colour", new String[]{"Light", "Dark"});
        matchSpinner = addChoice(left, "Match length", new String[]{
                "Single Game", "First to 3", "First to 5", "First to 7"
        });

        TextView summaryTitle = text("MATCH RULES", 16, true);
        summaryTitle.setGravity(Gravity.CENTER);
        right.addView(summaryTitle, new LinearLayout.LayoutParams(-1, dp(36)));

        TextView summary = text(
                "Player 2 automatically travels in the opposite direction.\n\n" +
                "Your checker colour is visual only and does not change movement rules.\n\n" +
                "Opening roll: each side rolls one die; ties reroll; the higher die starts using both opening dice.\n\n" +
                "Mahbouseh and Tawle 31 are visible here so the setup structure is ready, but v1.4.2 only enables the standard Backgammon / Sheish Beish ruleset.",
                11, false);
        summary.setTextColor(Color.rgb(194, 184, 157));
        summary.setGravity(Gravity.CENTER_VERTICAL);
        right.addView(summary, new LinearLayout.LayoutParams(-1, 0, 1f));

        Button start = new Button(this);
        start.setText("START MATCH");
        start.setAllCaps(false);
        start.setTextColor(Color.rgb(255, 240, 207));
        start.setTextSize(16);
        start.setTypeface(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD);
        start.setBackgroundResource(R.drawable.button_green);
        start.setStateListAnimator(null);
        start.setOnClickListener(v -> startMatch());
        LinearLayout.LayoutParams startLp = new LinearLayout.LayoutParams(-1, dp(50));
        startLp.topMargin = dp(8);
        right.addView(start, startLp);

        return root;
    }

    private void startMatch() {
        if (versionSpinner.getSelectedItemPosition() != 0) {
            Toast.makeText(this, "That ruleset is shown for the new setup system but is not playable yet.", Toast.LENGTH_LONG).show();
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

    private Spinner addChoice(LinearLayout parent, String label, String[] choices) {
        TextView l = text(label, 11, true);
        l.setTextColor(Color.rgb(231, 202, 139));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(22));
        lp.topMargin = dp(3);
        parent.addView(l, lp);

        Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, choices);
        spinner.setAdapter(adapter);
        spinner.setBackgroundResource(R.drawable.premium_tab);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(40));
        sp.bottomMargin = dp(4);
        parent.addView(spinner, sp);
        return spinner;
    }

    private LinearLayout panel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(9), dp(14), dp(10));
        panel.setBackgroundResource(R.drawable.premium_card);
        return panel;
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.rgb(248, 229, 181));
        v.setTextSize(sp);
        v.setTypeface(android.graphics.Typeface.SERIF, bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        return v;
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
