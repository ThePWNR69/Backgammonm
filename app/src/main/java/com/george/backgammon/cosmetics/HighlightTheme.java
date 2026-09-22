package com.george.backgammon.cosmetics;

public final class HighlightTheme {
    public final String id;
    public final int selectedColor;
    public final int validColor;
    public final int captureColor;
    public final int invalidColor;

    public HighlightTheme(String id, int selectedColor, int validColor, int captureColor, int invalidColor) {
        this.id = id;
        this.selectedColor = selectedColor;
        this.validColor = validColor;
        this.captureColor = captureColor;
        this.invalidColor = invalidColor;
    }
}
