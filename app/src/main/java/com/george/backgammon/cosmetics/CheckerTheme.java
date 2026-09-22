package com.george.backgammon.cosmetics;

public final class CheckerTheme {
    public final String id;
    public final String displayName;
    public final int lightBase;
    public final int lightEdge;
    public final int darkBase;
    public final int darkEdge;
    public final String lightAsset;
    public final String darkAsset;

    public CheckerTheme(String id, String displayName,
                        int lightBase, int lightEdge, int darkBase, int darkEdge,
                        String lightAsset, String darkAsset) {
        this.id = id;
        this.displayName = displayName;
        this.lightBase = lightBase;
        this.lightEdge = lightEdge;
        this.darkBase = darkBase;
        this.darkEdge = darkEdge;
        this.lightAsset = lightAsset;
        this.darkAsset = darkAsset;
    }
}
