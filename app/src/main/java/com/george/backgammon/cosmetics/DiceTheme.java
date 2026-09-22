package com.george.backgammon.cosmetics;

public final class DiceTheme {
    public final String id;
    public final String displayName;
    public final int bodyColor;
    public final int pipColor;
    public final int edgeColor;
    public final String faceAssetPattern;

    public DiceTheme(String id, String displayName, int bodyColor, int pipColor, int edgeColor,
                     String faceAssetPattern) {
        this.id = id;
        this.displayName = displayName;
        this.bodyColor = bodyColor;
        this.pipColor = pipColor;
        this.edgeColor = edgeColor;
        this.faceAssetPattern = faceAssetPattern;
    }
}
