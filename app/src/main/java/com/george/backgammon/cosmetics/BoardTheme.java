package com.george.backgammon.cosmetics;

/**
 * Data-only board skin. Gameplay geometry never lives here.
 *
 * A board theme may provide a complete frame/board asset plus optional material textures.
 * Material textures are clipped into the canonical BoardMap at runtime, so visual realism can
 * improve without moving triangles, checker anchors, touch zones or bear-off/bar geometry.
 */
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

    /** Optional complete empty physical board/frame image aligned to BoardGeometry. */
    public final String boardAsset;
    public final String frameAsset;
    public final String fieldAsset;
    public final String pointsAsset;
    public final String barAsset;

    /** Optional realistic material textures, repeated and clipped by the renderer. */
    public final String woodTextureAsset;
    public final String fieldTextureAsset;
    public final String trayTextureAsset;
    public final String lightPointTextureAsset;
    public final String darkPointTextureAsset;

    public BoardTheme(String id, String displayName,
                      int outerWood, int innerWood, int leather,
                      int lightPoint, int darkPoint, int metalAccent, int trim,
                      String boardAsset, String frameAsset, String fieldAsset,
                      String pointsAsset, String barAsset) {
        this(id, displayName, outerWood, innerWood, leather, lightPoint, darkPoint,
                metalAccent, trim, boardAsset, frameAsset, fieldAsset, pointsAsset, barAsset,
                null, null, null, null, null);
    }

    public BoardTheme(String id, String displayName,
                      int outerWood, int innerWood, int leather,
                      int lightPoint, int darkPoint, int metalAccent, int trim,
                      String boardAsset, String frameAsset, String fieldAsset,
                      String pointsAsset, String barAsset,
                      String woodTextureAsset, String fieldTextureAsset, String trayTextureAsset,
                      String lightPointTextureAsset, String darkPointTextureAsset) {
        this.id = id;
        this.displayName = displayName;
        this.outerWood = outerWood;
        this.innerWood = innerWood;
        this.leather = leather;
        this.lightPoint = lightPoint;
        this.darkPoint = darkPoint;
        this.metalAccent = metalAccent;
        this.trim = trim;
        this.boardAsset = boardAsset;
        this.frameAsset = frameAsset;
        this.fieldAsset = fieldAsset;
        this.pointsAsset = pointsAsset;
        this.barAsset = barAsset;
        this.woodTextureAsset = woodTextureAsset;
        this.fieldTextureAsset = fieldTextureAsset;
        this.trayTextureAsset = trayTextureAsset;
        this.lightPointTextureAsset = lightPointTextureAsset;
        this.darkPointTextureAsset = darkPointTextureAsset;
    }
}
