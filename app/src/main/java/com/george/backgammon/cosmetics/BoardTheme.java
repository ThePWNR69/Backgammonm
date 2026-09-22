package com.george.backgammon.cosmetics;

/** Data-only board skin. Future image asset IDs can be added without touching rules/gameplay. */
public final class BoardTheme {
    public final String id;
    public final String displayName;
    public final int outerWood;
    public final int innerWood;
    public final int leather;
    public final int lightPoint;
    public final int darkPoint;
    public final int metalAccent;
    public final int trim;
    public final String frameAsset;
    public final String fieldAsset;
    public final String pointsAsset;
    public final String barAsset;

    public BoardTheme(String id, String displayName,
                      int outerWood, int innerWood, int leather,
                      int lightPoint, int darkPoint, int metalAccent, int trim,
                      String frameAsset, String fieldAsset, String pointsAsset, String barAsset) {
        this.id = id;
        this.displayName = displayName;
        this.outerWood = outerWood;
        this.innerWood = innerWood;
        this.leather = leather;
        this.lightPoint = lightPoint;
        this.darkPoint = darkPoint;
        this.metalAccent = metalAccent;
        this.trim = trim;
        this.frameAsset = frameAsset;
        this.fieldAsset = fieldAsset;
        this.pointsAsset = pointsAsset;
        this.barAsset = barAsset;
    }
}
