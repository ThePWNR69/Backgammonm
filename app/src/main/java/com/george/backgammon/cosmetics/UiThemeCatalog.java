package com.george.backgammon.cosmetics;

import com.george.backgammon.R;

/** Central UI-theme registry. Future board themes swap resources here without touching GameActivity. */
public final class UiThemeCatalog {
    private UiThemeCatalog() {}
    public static final UiTheme CLASSIC_BURGUNDY = new UiTheme(
            "classic_burgundy", "Classic Burgundy",
            R.drawable.gameplay_bar_burgundy, R.drawable.gameplay_bottom_bar,
            R.drawable.button_burgundy, R.drawable.button_brown_gold);
    public static UiTheme defaultTheme() { return CLASSIC_BURGUNDY; }
}
