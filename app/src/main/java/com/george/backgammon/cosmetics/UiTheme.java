package com.george.backgammon.cosmetics;

/** Visual chrome paired with a board theme. Geometry and gameplay never live here. */
public final class UiTheme {
    public final String id;
    public final String displayName;
    public final int topBarDrawable;
    public final int bottomBarDrawable;
    public final int primaryButtonDrawable;
    public final int secondaryButtonDrawable;

    public UiTheme(String id, String displayName, int topBarDrawable, int bottomBarDrawable,
                   int primaryButtonDrawable, int secondaryButtonDrawable) {
        this.id = id; this.displayName = displayName; this.topBarDrawable = topBarDrawable;
        this.bottomBarDrawable = bottomBarDrawable; this.primaryButtonDrawable = primaryButtonDrawable;
        this.secondaryButtonDrawable = secondaryButtonDrawable;
    }
}
