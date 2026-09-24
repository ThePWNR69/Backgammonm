package com.george.backgammon.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.george.backgammon.animation.MoveAnimationStyle;
import com.george.backgammon.cosmetics.BoardTheme;
import com.george.backgammon.cosmetics.CheckerTheme;
import com.george.backgammon.cosmetics.CosmeticCatalog;
import com.george.backgammon.rendering.CosmeticPreviewView;

import java.util.List;

/** Responsive v0.9 graphics customiser using the exact gameplay preview geometry. */
public final class CustomiseActivity extends Activity {
    private static final int GOLD = 0xFFD3AE67;
    private static final int CREAM = 0xFFF4E6C9;
    private static final int GREEN = 0xFF08362E;
    private static final int GREEN_SELECTED = 0xFF0C5948;

    private LinearLayout cards;
    private SharedPreferences prefs;
    private String boardId;
    private String checkerId;
    private String animationId;
    private int currentTab = 0;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        enterImmersiveMode();
        prefs = getSharedPreferences("backgammon_visuals", MODE_PRIVATE);
        boardId = prefs.getString("board", CosmeticCatalog.CLASSIC_WALNUT.id);
        checkerId = prefs.getString("checkers", CosmeticCatalog.IVORY_WALNUT.id);
        animationId = prefs.getString("animation", CosmeticCatalog.MOVE_ANIMATIONS.get(0).id());
        buildScreen();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(10), dp(18), dp(12));
        root.setBackgroundColor(0xFF052A24);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        Button back = textButton("‹", dp(58), dp(46));
        back.setTextSize(34);
        back.setOnClickListener(v -> finish());
        header.addView(back);
        TextView title = label("CUSTOMISE", 27, true);
        title.setGravity(Gravity.CENTER);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(50), 1f));
        View spacer = new View(this);
        header.addView(spacer, new LinearLayout.LayoutParams(dp(58), dp(1)));
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(52)));

        LinearLayout tabs = new LinearLayout(this);
        tabs.setGravity(Gravity.CENTER);
        String[] names = {"Board", "Checkers", "Animation"};
        for (int i=0;i<3;i++) {
            final int tab=i;
            Button b=textButton(names[i], 0, dp(46));
            b.setTag(i);
            b.setOnClickListener(v -> { currentTab=tab; renderCards(); updateTabs(tabs); });
            tabs.addView(b,new LinearLayout.LayoutParams(0,dp(46),1f));
            if (i<2) {
                View gap=new View(this); tabs.addView(gap,new LinearLayout.LayoutParams(dp(10),1));
            }
        }
        root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(52)));

        TextView help=label("Choose a cosmetic. Previews use the same map and checker sizing as gameplay.",11,false);
        help.setGravity(Gravity.CENTER);
        help.setTextColor(0xFF9FB8AC);
        root.addView(help,new LinearLayout.LayoutParams(-1,dp(30)));

        cards=new LinearLayout(this);
        cards.setGravity(Gravity.CENTER);
        root.addView(cards,new LinearLayout.LayoutParams(-1,0,1f));
        setContentView(root);
        updateTabs(tabs);
        renderCards();
    }

    private void updateTabs(LinearLayout tabs) {
        for(int i=0;i<tabs.getChildCount();i++) {
            View v=tabs.getChildAt(i);
            if(v instanceof Button && v.getTag() instanceof Integer) {
                boolean selected=((Integer)v.getTag())==currentTab;
                v.setBackground(panel(selected));
                ((Button)v).setTextColor(selected?CREAM:0xFFC2B89F);
            }
        }
    }

    private void renderCards() {
        cards.removeAllViews();
        if(currentTab==0) renderBoards();
        else if(currentTab==1) renderCheckers();
        else renderAnimations();
    }

    private void renderBoards() {
        CheckerTheme currentChecker=CosmeticCatalog.checkersById(checkerId);
        for(BoardTheme theme:CosmeticCatalog.BOARD_THEMES) {
            boolean equipped=theme.id.equals(boardId);
            CosmeticPreviewView preview=new CosmeticPreviewView(this);
            preview.showBoard(theme,currentChecker);
            addCard(preview,theme.displayName,equipped,()->{
                boardId=theme.id; prefs.edit().putString("board",boardId).apply(); renderCards();
            });
        }
    }

    private void renderCheckers() {
        for(CheckerTheme theme:CosmeticCatalog.CHECKER_THEMES) {
            boolean equipped=theme.id.equals(checkerId);
            CosmeticPreviewView preview=new CosmeticPreviewView(this);
            preview.showCheckers(theme);
            addCard(preview,theme.displayName,equipped,()->{
                checkerId=theme.id; prefs.edit().putString("checkers",checkerId).apply(); renderCards();
            });
        }
    }

    private void renderAnimations() {
        List<MoveAnimationStyle> list=CosmeticCatalog.MOVE_ANIMATIONS;
        for(MoveAnimationStyle style:list) {
            boolean equipped=style.id().equals(animationId);
            LinearLayout preview=new LinearLayout(this);
            preview.setGravity(Gravity.CENTER);
            preview.setBackground(panel(false));
            TextView glyph=label("●  →  ●",24,true);
            glyph.setTextColor(style.id().equals("roll_and_fall")?0xFFF0C85A:0xFF7DE0C5);
            preview.addView(glyph);
            addCard(preview,style.displayName(),equipped,()->{
                animationId=style.id(); prefs.edit().putString("animation",animationId).apply(); renderCards();
            });
        }
    }

    private void addCard(View preview,String name,boolean equipped,Runnable tap) {
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8),dp(8),dp(8),dp(7));
        card.setGravity(Gravity.CENTER);
        card.setBackground(panel(equipped));
        card.setOnClickListener(v->tap.run());
        card.addView(preview,new LinearLayout.LayoutParams(-1,0,1f));
        TextView n=label(name,17,true); n.setGravity(Gravity.CENTER); card.addView(n,new LinearLayout.LayoutParams(-1,dp(34)));
        TextView state=label(equipped?"✓ EQUIPPED":"TAP TO EQUIP",10,true); state.setGravity(Gravity.CENTER); state.setTextColor(GOLD); card.addView(state,new LinearLayout.LayoutParams(-1,dp(22)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1f); lp.setMargins(dp(7),dp(4),dp(7),dp(4)); cards.addView(card,lp);
    }

    private Button textButton(String text,int width,int height) {
        Button b=new Button(this); b.setText(text); b.setTextAllCaps(false); b.setTextSize(16); b.setTextColor(CREAM); b.setGravity(Gravity.CENTER); b.setBackground(panel(false)); b.setStateListAnimator(null);
        if(width>0)b.setLayoutParams(new LinearLayout.LayoutParams(width,height)); return b;
    }

    private TextView label(String text,int sp,boolean bold) {
        TextView t=new TextView(this); t.setText(text); t.setTextColor(CREAM); t.setTextSize(sp); t.setGravity(Gravity.CENTER_VERTICAL); t.setFontFeatureSettings("kern"); if(bold)t.setTypeface(android.graphics.Typeface.create("serif",android.graphics.Typeface.BOLD)); return t;
    }

    private GradientDrawable panel(boolean selected) {
        GradientDrawable g=new GradientDrawable(); g.setColor(selected?GREEN_SELECTED:GREEN); g.setCornerRadius(dp(15)); g.setStroke(dp(selected?2:1),selected?0xFFE2BE72:0xFF5E5743); return g;
    }

    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private void enterImmersiveMode(){getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)enterImmersiveMode();}
}
