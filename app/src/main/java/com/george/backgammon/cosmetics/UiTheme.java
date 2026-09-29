package com.george.backgammon.cosmetics;

/**
 * Complete gameplay chrome paired with a board family.
 * Geometry and gameplay never live here: a theme only chooses visual resources.
 */
public final class UiTheme {
    public final String id;
    public final String displayName;
    public final int playerOnePanelDrawable;
    public final int playerTwoPanelDrawable;
    public final int statusBarDrawable;
    public final int bottomBarDrawable;
    public final int primaryButtonDrawable;
    public final int secondaryButtonDrawable;
    public final int hintButtonDrawable;
    public final int menuButtonDrawable;
    public final int settingsButtonDrawable;

    public UiTheme(String id, String displayName,
                   int playerOnePanelDrawable, int playerTwoPanelDrawable,
                   int statusBarDrawable, int bottomBarDrawable,
                   int primaryButtonDrawable, int secondaryButtonDrawable,
                   int hintButtonDrawable, int menuButtonDrawable,
                   int settingsButtonDrawable) {
        this.id = id;
        this.displayName = displayName;
        this.playerOnePanelDrawable = playerOnePanelDrawable;
        this.playerTwoPanelDrawable = playerTwoPanelDrawable;
        this.statusBarDrawable = statusBarDrawable;
        this.bottomBarDrawable = bottomBarDrawable;
        this.primaryButtonDrawable = primaryButtonDrawable;
        this.secondaryButtonDrawable = secondaryButtonDrawable;
        this.hintButtonDrawable = hintButtonDrawable;
        this.menuButtonDrawable = menuButtonDrawable;
        this.settingsButtonDrawable = settingsButtonDrawable;
    }
}
