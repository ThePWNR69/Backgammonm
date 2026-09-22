package com.george.backgammon.rendering;

/** Pure layout math for the board. Keeps drawing/input code independent from screen proportions. */
final class BoardGeometry {
    float outerLeft, outerTop, outerRight, outerBottom;
    float fieldLeft, fieldTop, fieldRight, fieldBottom;
    float offLeft, offRight, barLeft, barRight, colWidth, triangleHeight;
    float frame;

    void compute(float width, float height) {
        float pad = Math.max(4f, Math.min(width, height) * 0.008f);
        float availableW = width - pad * 2f;
        float availableH = height - pad * 2f;
        float desiredAspect = 2.38f;
        float boardW = Math.min(availableW, availableH * desiredAspect);
        float boardH = Math.min(availableH, boardW / desiredAspect);

        outerLeft = (width - boardW) / 2f;
        outerRight = outerLeft + boardW;
        outerTop = (height - boardH) / 2f;
        outerBottom = outerTop + boardH;

        frame = Math.max(9f, boardH * 0.032f);
        float offWidth = Math.max(38f, boardW * 0.043f);
        offRight = outerRight - frame * 0.45f;
        offLeft = offRight - offWidth;

        fieldLeft = outerLeft + frame;
        fieldTop = outerTop + frame;
        fieldRight = offLeft - frame * 0.45f;
        fieldBottom = outerBottom - frame;

        float playWidth = fieldRight - fieldLeft;
        float barWidth = Math.max(34f, playWidth * 0.047f);
        colWidth = (playWidth - barWidth) / 12f;
        barLeft = fieldLeft + 6f * colWidth;
        barRight = barLeft + barWidth;
        triangleHeight = (fieldBottom - fieldTop) * 0.445f;
    }

    float columnX(int visualIndex) {
        return fieldLeft + visualIndex * colWidth + (visualIndex >= 6 ? (barRight - barLeft) : 0);
    }
}
