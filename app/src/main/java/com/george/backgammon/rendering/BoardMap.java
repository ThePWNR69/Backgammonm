package com.george.backgammon.rendering;

/**
 * Canonical invisible backgammon map for the premium board template.
 *
 * v1.4.4 changes the mapping model from "one evenly-spaced column centre" to the
 * actual geometry of every visible triangle.  Each point stores its base-left,
 * base-right and apex coordinates measured from the production artwork.  Checker
 * centres are calculated halfway between the two sloping triangle edges at the
 * checker's own Y position.
 *
 * This means a slightly leaning/asymmetric triangle can no longer make a checker
 * appear biased to one side.  All board themes must continue to use this exact
 * master geometry.
 */
public final class BoardMap {
    private BoardMap() {}

    public static final int MASTER_WIDTH_PX = 2048;
    public static final int MASTER_HEIGHT_PX = 977;
    public static final float MASTER_ASPECT = (float) MASTER_WIDTH_PX / MASTER_HEIGHT_PX;

    public static final float FIELD_LEFT = 236f / MASTER_WIDTH_PX;
    public static final float FIELD_RIGHT = 1840f / MASTER_WIDTH_PX;
    public static final float FIELD_TOP = 58f / MASTER_HEIGHT_PX;
    public static final float FIELD_BOTTOM = 916f / MASTER_HEIGHT_PX;

    public static final float LEFT_TRAY_LEFT = 78f / MASTER_WIDTH_PX;
    public static final float LEFT_TRAY_RIGHT = 219f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_LEFT = 1849f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_RIGHT = 1976f / MASTER_WIDTH_PX;

    public static final float BAR_LEFT = 996f / MASTER_WIDTH_PX;
    public static final float BAR_RIGHT = 1085f / MASTER_WIDTH_PX;

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
            252.10f, 380.48f, 502.48f, 627.17f, 754.10f, 874.22f,
            1087.96f, 1219.06f, 1339.69f, 1464.48f, 1585.73f, 1716.30f
    };
    public static final float[] TOP_BASE_RIGHT_PX = {
            368.59f, 494.60f, 618.59f, 740.31f, 868.89f, 987.76f,
            1206.27f, 1332.58f, 1456.70f, 1578.72f, 1702.50f, 1830.35f
    };
    public static final float[] TOP_APEX_X_PX = {
            304.72f, 430.80f, 555.19f, 680.72f, 806.24f, 930.08f,
            1149.64f, 1275.44f, 1400.63f, 1525.31f, 1649.30f, 1777.03f
    };
    public static final float[] TOP_APEX_Y_PX = {
            416.25f, 417.07f, 416.62f, 417.84f, 416.26f, 415.92f,
            415.58f, 419.60f, 415.56f, 417.11f, 417.33f, 417.65f
    };

    public static final float[] BOTTOM_BASE_LEFT_PX = {
            236.40f, 360.17f, 486.77f, 611.01f, 738.46f, 866.35f,
            1088.11f, 1212.66f, 1339.87f, 1466.96f, 1593.81f, 1718.85f
    };
    public static final float[] BOTTOM_BASE_RIGHT_PX = {
            356.67f, 481.20f, 606.91f, 733.56f, 857.05f, 988.74f,
            1210.26f, 1335.39f, 1461.23f, 1588.51f, 1714.63f, 1839.46f
    };
    public static final float[] BOTTOM_APEX_X_PX = {
            305.28f, 429.88f, 555.59f, 680.54f, 806.81f, 930.70f,
            1149.96f, 1274.98f, 1400.73f, 1525.99f, 1650.80f, 1777.95f
    };
    public static final float[] BOTTOM_APEX_Y_PX = {
            524.01f, 525.02f, 523.10f, 527.57f, 523.43f, 527.40f,
            525.71f, 526.81f, 526.87f, 531.37f, 526.48f, 529.96f
    };

    // Source-art standards. Runtime controls final displayed diameter independently.
    public static final int CHECKER_CANVAS_PX = 512;
    public static final int CHECKER_VISIBLE_DIAMETER_PX = 448;
}
