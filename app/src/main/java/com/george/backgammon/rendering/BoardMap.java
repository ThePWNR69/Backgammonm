package com.george.backgammon.rendering;

/**
 * Canonical invisible backgammon map for the premium board template.
 *
 * v1.6.4 keeps the measured-point mapping model: gameplay follows the
 * actual geometry of every visible triangle rather than an evenly spaced grid.  Each point stores its base-left,
 * base-right and apex coordinates measured from the production artwork.  Checker
 * centres are calculated halfway between the two sloping triangle edges at the
 * checker's own Y position.
 *
 * The v1.6.4 production art is also horizontally rebalanced around the centre bar,
 * and these coordinates are the matching post-rebalance measurements. This keeps the
 * outer wood rails visually even without decoupling checker geometry from the artwork. All board themes must continue to use this exact
 * master geometry.
 */
public final class BoardMap {
    private BoardMap() {}

    public static final int MASTER_WIDTH_PX = 2048;
    public static final int MASTER_HEIGHT_PX = 977;
    public static final float MASTER_ASPECT = (float) MASTER_WIDTH_PX / MASTER_HEIGHT_PX;

    public static final float FIELD_LEFT = 232.09f / MASTER_WIDTH_PX;
    public static final float FIELD_RIGHT = 1836.44f / MASTER_WIDTH_PX;
    public static final float FIELD_TOP = 58f / MASTER_HEIGHT_PX;
    public static final float FIELD_BOTTOM = 916f / MASTER_HEIGHT_PX;

    public static final float LEFT_TRAY_LEFT = 76.71f / MASTER_WIDTH_PX;
    public static final float LEFT_TRAY_RIGHT = 215.37f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_LEFT = 1845.59f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_RIGHT = 1974.77f / MASTER_WIDTH_PX;

    public static final float BAR_LEFT = 979.50f / MASTER_WIDTH_PX;
    public static final float BAR_RIGHT = 1068.50f / MASTER_WIDTH_PX;

    // Retained for fallback/procedural rendering. Actual checker stacking now uses the
    // measured per-point apex/base distance below.
    public static final float TRIANGLE_HEIGHT = 374f / MASTER_HEIGHT_PX;

    /**
     * Triangle geometry in master-image pixels, visual index 0..11 left-to-right.
     * Top points map 13..24; bottom points map 12..1.
     *
     * Values were measured from the actual Classic Walnut production artwork using
     * the visible left/right triangle edges across many scanlines, then line-fitted
     * to the base and apex.  This intentionally preserves the small natural lean of
     * individual points instead of forcing an artificial vertical centreline.
     */
    public static final float[] TOP_BASE_LEFT_PX = {
            247.92f, 374.18f, 494.16f, 616.78f, 741.61f, 859.74f,
            1071.51f, 1204.86f, 1327.55f, 1454.48f, 1577.81f, 1710.62f
    };
    public static final float[] TOP_BASE_RIGHT_PX = {
            362.48f, 486.41f, 608.34f, 728.05f, 854.50f, 971.40f,
            1191.85f, 1320.32f, 1446.57f, 1570.68f, 1696.58f, 1826.62f
    };
    public static final float[] TOP_APEX_X_PX = {
            299.67f, 423.66f, 545.99f, 669.44f, 792.88f, 914.67f,
            1134.25f, 1262.20f, 1389.54f, 1516.35f, 1642.47f, 1772.39f
    };
    public static final float[] TOP_APEX_Y_PX = {
            416.25f, 417.07f, 416.62f, 417.84f, 416.26f, 415.92f,
            415.58f, 419.60f, 415.56f, 417.11f, 417.33f, 417.65f
    };

    public static final float[] BOTTOM_BASE_LEFT_PX = {
            232.48f, 354.20f, 478.71f, 600.89f, 726.23f, 852.00f,
            1071.66f, 1198.35f, 1327.74f, 1457.00f, 1586.03f, 1713.21f
    };
    public static final float[] BOTTOM_BASE_RIGHT_PX = {
            350.76f, 473.23f, 596.86f, 721.41f, 842.85f, 972.36f,
            1195.91f, 1323.18f, 1451.18f, 1580.64f, 1708.92f, 1835.89f
    };
    public static final float[] BOTTOM_APEX_X_PX = {
            300.22f, 422.76f, 546.39f, 669.27f, 793.44f, 915.28f,
            1134.57f, 1261.74f, 1389.64f, 1517.05f, 1643.99f, 1773.32f
    };
    public static final float[] BOTTOM_APEX_Y_PX = {
            524.01f, 525.02f, 523.10f, 527.57f, 523.43f, 527.40f,
            525.71f, 526.81f, 526.87f, 531.37f, 526.48f, 529.96f
    };

    // Source-art standards. Runtime controls final displayed diameter independently.
    public static final int CHECKER_CANVAS_PX = 512;
    public static final int CHECKER_VISIBLE_DIAMETER_PX = 448;
}
