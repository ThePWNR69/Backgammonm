package com.george.backgammon.rendering;

/**
 * Canonical invisible backgammon map.
 *
 * These values are production standards, not theme settings. Every board skin is authored to the
 * same 2048 x 992 template and every checker/dice interaction is positioned from this map.
 * Changing a board cosmetic must never change gameplay geometry.
 */
public final class BoardMap {
    private BoardMap() {}

    public static final int MASTER_WIDTH_PX = 2048;
    public static final int MASTER_HEIGHT_PX = 992;
    public static final float MASTER_ASPECT = (float) MASTER_WIDTH_PX / MASTER_HEIGHT_PX;

    // Normalised production-board regions. Keep in sync with DESIGN_STANDARDS.md.
    public static final float FIELD_LEFT = 250f / MASTER_WIDTH_PX;
    public static final float FIELD_RIGHT = 1798f / MASTER_WIDTH_PX;
    public static final float FIELD_TOP = 108f / MASTER_HEIGHT_PX;
    public static final float FIELD_BOTTOM = 884f / MASTER_HEIGHT_PX;

    public static final float LEFT_TRAY_LEFT = 95f / MASTER_WIDTH_PX;
    public static final float LEFT_TRAY_RIGHT = 220f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_LEFT = 1828f / MASTER_WIDTH_PX;
    public static final float RIGHT_TRAY_RIGHT = 1953f / MASTER_WIDTH_PX;

    public static final float BAR_LEFT = 999f / MASTER_WIDTH_PX;
    public static final float BAR_RIGHT = 1049f / MASTER_WIDTH_PX;
    public static final float TRIANGLE_HEIGHT = 370f / MASTER_HEIGHT_PX;

    // Source-art standards. Runtime still enforces final display size independently of source art.
    public static final int CHECKER_CANVAS_PX = 512;
    public static final int CHECKER_VISIBLE_DIAMETER_PX = 448;
}
