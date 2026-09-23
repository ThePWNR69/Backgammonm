package com.george.backgammon.cosmetics;

public final class HighlightTheme {
    public final String id;
    public final int selectedColor;
    public final int validColor;
    /** Final landing that consumes two dice with the same checker. */
    public final int combinedColor;
    public final int captureColor;
    public final int invalidColor;

    public HighlightTheme(String id, int selectedColor, int validColor, int combinedColor,
                          int captureColor, int invalidColor) {
        this.id = id;
        this.selectedColor = selectedColor;
        this.validColor = validColor;
        this.combinedColor = combinedColor;
        this.captureColor = captureColor;
        this.invalidColor = invalidColor;
    }
}
