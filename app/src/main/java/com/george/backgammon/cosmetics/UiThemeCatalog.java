package com.george.backgammon.cosmetics;

import com.george.backgammon.R;

/** Central UI-theme registry. New complete themes can be added here without touching gameplay. */
public final class UiThemeCatalog {
    private UiThemeCatalog() {}

    /**
     * Production baseline measured from design/reference/CLASSIC_BURGUNDY_APPROVED.png.
     * These are raster chrome assets authored at exactly 4x the 510 x 280 logical frame,
     * so they scale back into the locked rectangles without changing their proportions.
     */
    public static final UiTheme CLASSIC_BURGUNDY = new UiTheme(
            "classic_burgundy", "Classic Burgundy",
            R.drawable.premium_player_left,
            R.drawable.premium_player_right,
            R.drawable.premium_status,
            R.drawable.premium_bottom_deck,
            R.drawable.premium_primary_button_selector,
            R.drawable.premium_secondary_button_selector,
            R.drawable.premium_square_button_selector);

    public static UiTheme defaultTheme() { return CLASSIC_BURGUNDY; }
}
