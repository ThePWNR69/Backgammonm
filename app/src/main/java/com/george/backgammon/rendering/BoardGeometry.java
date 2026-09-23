package com.george.backgammon.rendering;

/**
 * Pure layout math for the board. v0.8 uses proportions based on the premium production artwork
 * rather than stretching the play field to the device's landscape aspect ratio.
 */
final class BoardGeometry {
    float outerLeft, outerTop, outerRight, outerBottom;
    float fieldLeft, fieldTop, fieldRight, fieldBottom;
    float leftTrayLeft, leftTrayRight;
    float offLeft, offRight, barLeft, barRight, colWidth, triangleHeight;
    float frame;

    void compute(float width, float height) {
        float pad = Math.max(3f, Math.min(width, height) * 0.006f);
        float availableW = width - pad * 2f;
        float availableH = height - pad * 2f;

        // Matches the 1860 x 1000 master board artwork.
        float desiredAspect = 1.86f;
        float boardW = Math.min(availableW, availableH * desiredAspect);
        float boardH = Math.min(availableH, boardW / desiredAspect);

        outerLeft = (width - boardW) / 2f;
        outerRight = outerLeft + boardW;
        outerTop = (height - boardH) / 2f;
        outerBottom = outerTop + boardH;

        frame = boardH * 0.030f;
        float trayWidth = boardW * 0.055f;
        float trayGap = frame * 0.36f;

        leftTrayLeft = outerLeft + frame * 0.65f;
        leftTrayRight = leftTrayLeft + trayWidth;
        fieldLeft = leftTrayRight + trayGap;

        offRight = outerRight - frame * 0.65f;
        offLeft = offRight - trayWidth;
        fieldRight = offLeft - trayGap;

        fieldTop = outerTop + frame;
        fieldBottom = outerBottom - frame;

        float playWidth = fieldRight - fieldLeft;
        float barWidth = Math.max(34f, playWidth * 0.054f);
        colWidth = (playWidth - barWidth) / 12f;
        barLeft = fieldLeft + 6f * colWidth;
        barRight = barLeft + barWidth;
        triangleHeight = (fieldBottom - fieldTop) * 0.445f;
    }

    float columnX(int visualIndex) {
        return fieldLeft + visualIndex * colWidth + (visualIndex >= 6 ? (barRight - barLeft) : 0);
    }
}
