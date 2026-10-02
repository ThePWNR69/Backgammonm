package com.george.backgammon.cosmetics;

/**
 * Checker cosmetic pair. SIDE_LIGHT / SIDE_DARK describe gameplay-side pairing, not literal colours.
 * A future set can therefore be gold/navy, emerald/cream, etc. and still work with Auto opponent selection.
 *
 * v1.25.0 also supports optional side/edge artwork used in the bear-off tray.  This keeps the
 * on-board top-down checker art independent from the end-zone edge view while preserving one
 * cosmetic theme and one LIGHT/DARK metadata pairing.
 */
public final class CheckerTheme {
    public static final String SIDE_LIGHT = "LIGHT";
    public static final String SIDE_DARK = "DARK";

    public final String id;
    public final String displayName;
    public final int lightBase;
    public final int lightEdge;
    public final int darkBase;
    public final int darkEdge;
    public final String lightAsset;
    public final String darkAsset;
    public final String lightSideAsset;
    public final String darkSideAsset;
    public final String lightSideTag;
    public final String darkSideTag;

    public CheckerTheme(String id, String displayName,
                        int lightBase, int lightEdge, int darkBase, int darkEdge,
                        String lightAsset, String darkAsset) {
        this(id, displayName, lightBase, lightEdge, darkBase, darkEdge,
                lightAsset, darkAsset, null, null, SIDE_LIGHT, SIDE_DARK);
    }

    public CheckerTheme(String id, String displayName,
                        int lightBase, int lightEdge, int darkBase, int darkEdge,
                        String lightAsset, String darkAsset,
                        String lightSideTag, String darkSideTag) {
        this(id, displayName, lightBase, lightEdge, darkBase, darkEdge,
                lightAsset, darkAsset, null, null, lightSideTag, darkSideTag);
    }

    public CheckerTheme(String id, String displayName,
                        int lightBase, int lightEdge, int darkBase, int darkEdge,
                        String lightAsset, String darkAsset,
                        String lightSideAsset, String darkSideAsset,
                        String lightSideTag, String darkSideTag) {
        this.id = id;
        this.displayName = displayName;
        this.lightBase = lightBase;
        this.lightEdge = lightEdge;
        this.darkBase = darkBase;
        this.darkEdge = darkEdge;
        this.lightAsset = lightAsset;
        this.darkAsset = darkAsset;
        this.lightSideAsset = lightSideAsset;
        this.darkSideAsset = darkSideAsset;
        this.lightSideTag = lightSideTag;
        this.darkSideTag = darkSideTag;
    }
}
