package com.george.backgammon.rendering;

/**
 * Canonical invisible backgammon map for the premium 1.4.2 board template.
 *
 * Visual board skins are replaceable images under this permanent map.  All checker anchors,
 * hitboxes, bar/off positions and animation destinations come from these coordinates, never
 * from a board cosmetic.
 */
public final class BoardMap {
    private BoardMap() {}

    public static final int MASTER_WIDTH_PX = 2048;
    public static final int MASTER_HEIGHT_PX = 977;
    public static final float MASTER_ASPECT = (float) MASTER_WIDTH_PX / MASTER_HEIGHT_PX;

    // Exact regions measured from the approved premium reference board.
    public static final float FIELD_LEFT = 238f / MASTER_WIDTH_PX;
    public static final float FIELD_RIGHT = 1835f / MASTER_WIDTH_PX;
    public static final float FIELD_TOP = 54f / MASTER_HEIGHT_PX;
    public static final float FIELD_BOTTOM = 913f / MASTER_HEIGHT_PX;

    public static final float LEFT_TRAY_LEFT = 78f / MASTER_WIDTH_PX;
    public static final float LEFT_TRAY_RIGHT = 219f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_LEFT = 1849f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_RIGHT = 1976f / MASTER_WIDTH_PX;

    public static final float BAR_LEFT = 990f / MASTER_WIDTH_PX;
    public static final float BAR_RIGHT = 1081f / MASTER_WIDTH_PX;
    public static final float TRIANGLE_HEIGHT = 382f / MASTER_HEIGHT_PX;

    public static final int CHECKER_CANVAS_PX = 512;
    public static final int CHECKER_VISIBLE_DIAMETER_PX = 448;
}
