package com.george.backgammon.rendering;

/** Device-sized projection of the permanent {@link BoardMap}. */
final class BoardGeometry {
    float outerLeft, outerTop, outerRight, outerBottom;
    float fieldLeft, fieldTop, fieldRight, fieldBottom;
    float leftTrayLeft, leftTrayRight;
    float offLeft, offRight, barLeft, barRight;
    float leftColWidth, rightColWidth, colWidth, triangleHeight;
    float frame;

    void compute(float width, float height) {
        float pad = Math.max(1f, Math.min(width, height) * 0.0025f);
        float availableW = Math.max(1f, width - pad * 2f);
        float availableH = Math.max(1f, height - pad * 2f);

        float boardW = Math.min(availableW, availableH * BoardMap.MASTER_ASPECT);
        float boardH = boardW / BoardMap.MASTER_ASPECT;
        if (boardH > availableH) {
            boardH = availableH;
            boardW = boardH * BoardMap.MASTER_ASPECT;
        }

        outerLeft = (width - boardW) * 0.5f;
        outerTop = (height - boardH) * 0.5f;
        outerRight = outerLeft + boardW;
        outerBottom = outerTop + boardH;

        fieldLeft = x(BoardMap.FIELD_LEFT);
        fieldRight = x(BoardMap.FIELD_RIGHT);
        fieldTop = y(BoardMap.FIELD_TOP);
        fieldBottom = y(BoardMap.FIELD_BOTTOM);
        leftTrayLeft = x(BoardMap.LEFT_TRAY_LEFT);
        leftTrayRight = x(BoardMap.LEFT_TRAY_RIGHT);
        offLeft = x(BoardMap.RIGHT_TRAY_LEFT);
        offRight = x(BoardMap.RIGHT_TRAY_RIGHT);
        barLeft = x(BoardMap.BAR_LEFT);
        barRight = x(BoardMap.BAR_RIGHT);
        triangleHeight = boardH * BoardMap.TRIANGLE_HEIGHT;

        // Kept for fallback/procedural art and general sizing only. Gameplay checker
        // placement no longer assumes these columns are the triangle centrelines.
        leftColWidth = (x(BoardMap.BAR_LEFT) - x(BoardMap.FIELD_LEFT)) / 6f;
        rightColWidth = (x(BoardMap.FIELD_RIGHT) - x(BoardMap.BAR_RIGHT)) / 6f;
        colWidth = (leftColWidth + rightColWidth) * 0.5f;
        frame = boardH * 0.058f;
    }

    float x(float normalized) { return outerLeft + (outerRight - outerLeft) * normalized; }
    float y(float normalized) { return outerTop + (outerBottom - outerTop) * normalized; }

    private float xFromMaster(float masterX) { return x(masterX / BoardMap.MASTER_WIDTH_PX); }
    private float yFromMaster(float masterY) { return y(masterY / BoardMap.MASTER_HEIGHT_PX); }
    private float masterY(float screenY) {
        float boardH = Math.max(1f, outerBottom - outerTop);
        return ((screenY - outerTop) / boardH) * BoardMap.MASTER_HEIGHT_PX;
    }

    float columnX(int visualIndex) {
        // Nominal/fallback column start only. Exact checker X comes from pointMidXAtY().
        float baseLeft = visualIndex < 6 ? BoardMap.TOP_BASE_LEFT_PX[visualIndex]
                : BoardMap.TOP_BASE_LEFT_PX[visualIndex];
        return xFromMaster(baseLeft);
    }

    float columnWidth(int visualIndex) {
        float w = BoardMap.TOP_BASE_RIGHT_PX[visualIndex] - BoardMap.TOP_BASE_LEFT_PX[visualIndex];
        return (outerRight - outerLeft) * (w / BoardMap.MASTER_WIDTH_PX);
    }

    /** One logical checker size for every checker skin. */
    float checkerRadius() {
        float minBasePx = Float.MAX_VALUE;
        for (int i = 0; i < 12; i++) {
            minBasePx = Math.min(minBasePx, BoardMap.TOP_BASE_RIGHT_PX[i] - BoardMap.TOP_BASE_LEFT_PX[i]);
            minBasePx = Math.min(minBasePx, BoardMap.BOTTOM_BASE_RIGHT_PX[i] - BoardMap.BOTTOM_BASE_LEFT_PX[i]);
        }
        float baseWidthScreen = (outerRight - outerLeft) * (minBasePx / BoardMap.MASTER_WIDTH_PX);
        // v1.6.4: slightly smaller pieces leave more triangle visible and make five-checker stacks cleaner.
        // Diameter ~= 74% of the narrowest point base.
        return baseWidthScreen * 0.37f;
    }

    /** Legacy/global spacing helper retained for code that needs a generic value. */
    float checkerSpacing(float r) {
        return Math.max(r * 1.15f, Math.min(r * 1.45f, (triangleHeight - 2f * r) / 4f));
    }

    private int visualIndexForPoint(int point) {
        boolean topPoint = point >= 13;
        return topPoint ? point - 13 : 12 - point;
    }

    private float baseMasterY(boolean topPoint) {
        return topPoint ? BoardMap.FIELD_TOP * BoardMap.MASTER_HEIGHT_PX
                : BoardMap.FIELD_BOTTOM * BoardMap.MASTER_HEIGHT_PX;
    }

    private float apexMasterX(boolean topPoint, int idx) {
        return topPoint ? BoardMap.TOP_APEX_X_PX[idx] : BoardMap.BOTTOM_APEX_X_PX[idx];
    }

    private float apexMasterY(boolean topPoint, int idx) {
        return topPoint ? BoardMap.TOP_APEX_Y_PX[idx] : BoardMap.BOTTOM_APEX_Y_PX[idx];
    }

    private float baseLeftMasterX(boolean topPoint, int idx) {
        return topPoint ? BoardMap.TOP_BASE_LEFT_PX[idx] : BoardMap.BOTTOM_BASE_LEFT_PX[idx];
    }

    private float baseRightMasterX(boolean topPoint, int idx) {
        return topPoint ? BoardMap.TOP_BASE_RIGHT_PX[idx] : BoardMap.BOTTOM_BASE_RIGHT_PX[idx];
    }

    /**
     * True visual midpoint between the two sloping triangle edges at this exact Y.
     * This is the core v1.4.4 alignment rule.
     */
    float pointMidXAtY(int point, float screenY) {
        boolean topPoint = point >= 13;
        int idx = visualIndexForPoint(point);
        float baseY = baseMasterY(topPoint);
        float apexY = apexMasterY(topPoint, idx);
        float my = masterY(screenY);
        float denom = apexY - baseY;
        float t = Math.abs(denom) < 0.001f ? 0f : (my - baseY) / denom;
        t = Math.max(0f, Math.min(1f, t));

        float apexX = apexMasterX(topPoint, idx);
        float leftX = lerp(baseLeftMasterX(topPoint, idx), apexX, t);
        float rightX = lerp(baseRightMasterX(topPoint, idx), apexX, t);
        return xFromMaster((leftX + rightX) * 0.5f);
    }

    private float checkerSpacingForPoint(int point, float r) {
        boolean topPoint = point >= 13;
        int idx = visualIndexForPoint(point);
        float baseY = yFromMaster(baseMasterY(topPoint));
        float apexY = yFromMaster(apexMasterY(topPoint, idx));
        float height = Math.abs(apexY - baseY);
        // Five visible checker centres remain between the base and apex.  A little overlap
        // is allowed, but the fifth checker never needs to wander beyond the point tip.
        float exactFiveFit = Math.max(r * 0.92f, (height - 2f * r) / 4f);
        return Math.min(r * 1.50f, exactFiveFit);
    }

    float[] landingCenter(int point, int existingCount) {
        boolean topPoint = point >= 13;
        float r = checkerRadius();
        float spacing = checkerSpacingForPoint(point, r);
        int slot = Math.min(Math.max(existingCount, 0), 4);
        float cy = topPoint ? fieldTop + r + slot * spacing : fieldBottom - r - slot * spacing;
        float cx = pointMidXAtY(point, cy);
        return new float[]{cx, cy};
    }

    float[] topCheckerCenter(int point, int visibleCount) {
        int slot = Math.max(0, Math.min(visibleCount, 5) - 1);
        return landingCenter(point, slot);
    }

    /** Visual centroid of the exact painted triangle for the Board Map diagnostic. */
    float[] pointVisualCenter(int point) {
        boolean topPoint = point >= 13;
        int idx = visualIndexForPoint(point);
        float baseY = yFromMaster(baseMasterY(topPoint));
        float apexY = yFromMaster(apexMasterY(topPoint, idx));
        float cy = baseY + (apexY - baseY) / 3f;
        return new float[]{pointMidXAtY(point, cy), cy};
    }

    float[] pointBaseLeft(int point) {
        boolean topPoint = point >= 13;
        int idx = visualIndexForPoint(point);
        return new float[]{xFromMaster(baseLeftMasterX(topPoint, idx)), yFromMaster(baseMasterY(topPoint))};
    }

    float[] pointBaseRight(int point) {
        boolean topPoint = point >= 13;
        int idx = visualIndexForPoint(point);
        return new float[]{xFromMaster(baseRightMasterX(topPoint, idx)), yFromMaster(baseMasterY(topPoint))};
    }

    float[] pointApex(int point) {
        boolean topPoint = point >= 13;
        int idx = visualIndexForPoint(point);
        return new float[]{xFromMaster(apexMasterX(topPoint, idx)), yFromMaster(apexMasterY(topPoint, idx))};
    }

    float[] pointBaseCenter(int point) {
        float[] l = pointBaseLeft(point);
        float[] r = pointBaseRight(point);
        return new float[]{(l[0] + r[0]) * 0.5f, (l[1] + r[1]) * 0.5f};
    }

    float[] barCenter(boolean white) {
        float cx = (barLeft + barRight) * 0.5f;
        float cy = white ? fieldTop + (fieldBottom - fieldTop) * 0.37f
                : fieldTop + (fieldBottom - fieldTop) * 0.63f;
        return new float[]{cx, cy};
    }

    float[] offCenter(boolean white) {
        float cx = (offLeft + offRight) * 0.5f;
        float cy = white ? fieldTop + (fieldBottom - fieldTop) * 0.21f
                : fieldBottom - (fieldBottom - fieldTop) * 0.18f;
        return new float[]{cx, cy};
    }

    int hitPoint(float px, float py) {
        if (py < fieldTop || py > fieldBottom || (px >= barLeft && px <= barRight)) return 0;
        boolean top = py < (fieldTop + fieldBottom) * 0.5f;

        // Choose the nearest actual triangle midpoint at this Y instead of deriving the
        // point from an assumed equal-width grid.  This mirrors the visual geometry.
        int bestPoint = 0;
        float bestDistance = Float.MAX_VALUE;
        int start = top ? 13 : 1;
        int end = top ? 24 : 12;
        for (int point = start; point <= end; point++) {
            float midX = pointMidXAtY(point, py);
            float d = Math.abs(px - midX);
            if (d < bestDistance) {
                bestDistance = d;
                bestPoint = point;
            }
        }

        // Reject taps that are clearly outside the point grid rather than snapping across trays/bar.
        float maxPointWidth = (outerRight - outerLeft) * (130f / BoardMap.MASTER_WIDTH_PX);
        return bestDistance <= maxPointWidth * 0.72f ? bestPoint : 0;
    }

    int nearestPointBesideBar(float px, float py) {
        boolean top = py < (fieldTop + fieldBottom) * 0.5f;
        return top ? (px < (barLeft + barRight) * 0.5f ? 18 : 19)
                : (px < (barLeft + barRight) * 0.5f ? 7 : 6);
    }

    boolean isInOffTray(float px, float py) {
        return px >= offLeft && px <= offRight && py >= fieldTop && py <= fieldBottom;
    }

    boolean isInBar(float px, float py) {
        return px >= barLeft && px <= barRight && py >= fieldTop && py <= fieldBottom;
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
}
