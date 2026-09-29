package com.george.backgammon.cosmetics;

import com.george.backgammon.R;

/** Central UI-theme registry. New complete themes can be added here without touching gameplay. */
public final class UiThemeCatalog {
    private UiThemeCatalog() {}

    /**
     * Classic Burgundy baseline. The overall match composition still uses the 510 x 280 logical frame,
     * while v1.12.0 retains the v1.11 slimmer 30-unit top chrome and 39-unit bottom deck, while detailed raster chrome keeps the board
     * regains visual height. Raster assets are authored at 4x their locked logical rectangles.
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
