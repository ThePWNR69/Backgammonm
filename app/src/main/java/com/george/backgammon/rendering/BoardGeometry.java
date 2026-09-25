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
        leftColWidth = (x(BoardMap.LEFT_POINTS_RIGHT) - x(BoardMap.LEFT_POINTS_LEFT)) / 6f;
        rightColWidth = (x(BoardMap.RIGHT_POINTS_RIGHT) - x(BoardMap.RIGHT_POINTS_LEFT)) / 6f;
        colWidth = (leftColWidth + rightColWidth) * 0.5f; // visual sizing only
        frame = boardH * 0.058f;
    }

    float x(float normalized) { return outerLeft + (outerRight - outerLeft) * normalized; }
    float y(float normalized) { return outerTop + (outerBottom - outerTop) * normalized; }

    float columnX(int visualIndex) {
        if (visualIndex < 6) {
            return x(BoardMap.LEFT_POINTS_LEFT) + visualIndex * leftColWidth;
        }
        return x(BoardMap.RIGHT_POINTS_LEFT) + (visualIndex - 6) * rightColWidth;
    }

    float columnWidth(int visualIndex) {
        return visualIndex < 6 ? leftColWidth : rightColWidth;
    }

    float checkerRadius() {
        float narrowestColumn = Math.min(leftColWidth, rightColWidth);
        return Math.min(narrowestColumn * 0.34f, triangleHeight * 0.120f);
    }

    float checkerSpacing(float r) {
        float fitFiveToTip = Math.max(r * 0.92f, (triangleHeight - 2f * r) / 4f);
        return Math.min(r * 1.55f, fitFiveToTip);
    }

    private int visualIndexForPoint(int point) {
        boolean topPoint = point >= 13;
        return topPoint ? point - 13 : 12 - point;
    }

    float[] landingCenter(int point, int existingCount) {
        boolean topPoint = point >= 13;
        int idx = visualIndexForPoint(point);
        float cx = columnX(idx) + columnWidth(idx) * 0.5f;
        float r = checkerRadius();
        float spacing = checkerSpacing(r);
        int slot = Math.min(Math.max(existingCount, 0), 4);
        float cy = topPoint ? fieldTop + r + slot * spacing : fieldBottom - r - slot * spacing;
        return new float[]{cx, cy};
    }

    float[] topCheckerCenter(int point, int visibleCount) {
        boolean topPoint = point >= 13;
        int idx = visualIndexForPoint(point);
        float cx = columnX(idx) + columnWidth(idx) * 0.5f;
        float r = checkerRadius();
        float spacing = checkerSpacing(r);
        int slot = Math.max(0, Math.min(visibleCount, 5) - 1);
        float cy = topPoint ? fieldTop + r + slot * spacing : fieldBottom - r - slot * spacing;
        return new float[]{cx, cy};
    }

    /**
     * Visual centre of the triangular point itself.  Used by the Board Map diagnostic overlay.
     * This is intentionally different from landingCenter(), which is the base-most checker slot.
     */
    float[] pointVisualCenter(int point) {
        boolean topPoint = point >= 13;
        int idx = visualIndexForPoint(point);
        float cx = columnX(idx) + columnWidth(idx) * 0.5f;
        // Triangle centroid lies one third of its height from the base.
        float cy = topPoint ? fieldTop + triangleHeight / 3f : fieldBottom - triangleHeight / 3f;
        return new float[]{cx, cy};
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

        int idx;
        float leftStart = x(BoardMap.LEFT_POINTS_LEFT);
        float leftEnd = x(BoardMap.LEFT_POINTS_RIGHT);
        float rightStart = x(BoardMap.RIGHT_POINTS_LEFT);
        float rightEnd = x(BoardMap.RIGHT_POINTS_RIGHT);
        if (px >= leftStart && px < leftEnd) {
            idx = (int)((px - leftStart) / leftColWidth);
        } else if (px >= rightStart && px <= rightEnd) {
            idx = 6 + (int)((px - rightStart) / rightColWidth);
        } else {
            return 0;
        }
        idx = Math.max(0, Math.min(11, idx));
        return py < (fieldTop + fieldBottom) * 0.5f ? 13 + idx : 12 - idx;
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
}
