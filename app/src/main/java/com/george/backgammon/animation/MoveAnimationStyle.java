package com.george.backgammon.animation;

/**
 * Data-driven movement style. Rendering consumes these values; future premium animations can
 * implement richer behavior behind the same contract without touching game rules.
 */
public interface MoveAnimationStyle {
    String id();
    String displayName();
    long normalDurationMs();
    long hitDurationMs();
    float arcHeightFactor();
    float scaleLiftFactor();
    float interpolation(float t);
}
