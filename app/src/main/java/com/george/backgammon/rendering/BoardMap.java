package com.george.backgammon.rendering;

/**
 * Canonical invisible backgammon map.
 *
 * These values are production standards, not theme settings. Every board skin is authored to the
 * same 1860 x 1000 template and every checker/dice interaction is positioned from this map.
 * Changing a board cosmetic must never change gameplay geometry.
 */
public final class BoardMap {
    private BoardMap() {}

    public static final int MASTER_WIDTH_PX = 1860;
    public static final int MASTER_HEIGHT_PX = 1000;
    public static final float MASTER_ASPECT = 1.86f;

    // Normalised master-board regions. Keep in sync with production_templates/MASTER_BOARD_MAP.json.
    public static final float FIELD_LEFT = 181f / MASTER_WIDTH_PX;
    public static final float FIELD_RIGHT = 1679f / MASTER_WIDTH_PX;
    public static final float FIELD_TOP = 70f / MASTER_HEIGHT_PX;
    public static final float FIELD_BOTTOM = 930f / MASTER_HEIGHT_PX;

    public static final float LEFT_TRAY_LEFT = 58f / MASTER_WIDTH_PX;
    public static final float LEFT_TRAY_RIGHT = 163f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_LEFT = 1697f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_RIGHT = 1802f / MASTER_WIDTH_PX;

    public static final float BAR_LEFT = 887f / MASTER_WIDTH_PX;
    public static final float BAR_RIGHT = 973f / MASTER_WIDTH_PX;
    public static final float TRIANGLE_HEIGHT = 380f / MASTER_HEIGHT_PX;

    // Source-art standards. Runtime still enforces final display size independently of source art.
    public static final int CHECKER_CANVAS_PX = 512;
    public static final int CHECKER_VISIBLE_DIAMETER_PX = 448;
}
