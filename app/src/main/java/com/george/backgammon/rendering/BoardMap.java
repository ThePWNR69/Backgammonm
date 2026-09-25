package com.george.backgammon.rendering;

/**
 * Canonical invisible backgammon map for the premium 1.4.3 board template.
 *
 * Board artwork is only a skin.  Gameplay anchors are measured once from the approved
 * production board and reused by every board theme.  Cosmetics never own their own geometry.
 */
public final class BoardMap {
    private BoardMap() {}

    public static final int MASTER_WIDTH_PX = 2048;
    public static final int MASTER_HEIGHT_PX = 977;
    public static final float MASTER_ASPECT = (float) MASTER_WIDTH_PX / MASTER_HEIGHT_PX;

    // Measured directly from the actual point bases in the approved Classic Walnut artwork.
    // The two halves are deliberately stored separately: the centre bar is not part of either
    // six-point grid, so deriving all twelve columns from one long field rectangle caused a
    // small but visible horizontal drift on the left half in 1.4.2.
    public static final float LEFT_POINTS_LEFT = 251f / MASTER_WIDTH_PX;
    public static final float LEFT_POINTS_RIGHT = 996f / MASTER_WIDTH_PX;
    public static final float RIGHT_POINTS_LEFT = 1085f / MASTER_WIDTH_PX;
    public static final float RIGHT_POINTS_RIGHT = 1831f / MASTER_WIDTH_PX;

    public static final float FIELD_LEFT = LEFT_POINTS_LEFT;
    public static final float FIELD_RIGHT = RIGHT_POINTS_RIGHT;
    public static final float FIELD_TOP = 58f / MASTER_HEIGHT_PX;
    public static final float FIELD_BOTTOM = 916f / MASTER_HEIGHT_PX;

    public static final float LEFT_TRAY_LEFT = 78f / MASTER_WIDTH_PX;
    public static final float LEFT_TRAY_RIGHT = 219f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_LEFT = 1849f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_RIGHT = 1976f / MASTER_WIDTH_PX;

    public static final float BAR_LEFT = 996f / MASTER_WIDTH_PX;
    public static final float BAR_RIGHT = 1085f / MASTER_WIDTH_PX;
    public static final float TRIANGLE_HEIGHT = 356f / MASTER_HEIGHT_PX;

    public static final int CHECKER_CANVAS_PX = 512;
    public static final int CHECKER_VISIBLE_DIAMETER_PX = 448;
}
