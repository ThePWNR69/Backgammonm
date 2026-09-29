package com.george.backgammon.cosmetics;

import com.george.backgammon.R;

/** Central UI-theme registry. New complete themes can be added here without touching gameplay. */
public final class UiThemeCatalog {
    private UiThemeCatalog() {}

    /** Classic Burgundy layered-image baseline. Text remains live Android text. */
    public static final UiTheme CLASSIC_BURGUNDY = new UiTheme(
            "classic_burgundy", "Classic Burgundy",
            R.drawable.premium_player_left,
            R.drawable.premium_player_right,
            R.drawable.premium_status,
            R.drawable.premium_bottom_deck,
            R.drawable.premium_primary_button_selector,
            R.drawable.premium_secondary_button_selector,
            R.drawable.premium_hint_button_selector,
            R.drawable.premium_square_button_selector,
            R.drawable.premium_settings_button_selector);

    public static UiTheme defaultTheme() { return CLASSIC_BURGUNDY; }
}
