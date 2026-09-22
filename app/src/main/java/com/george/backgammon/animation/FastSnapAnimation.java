package com.george.backgammon.animation;

/** Example second style proving animations are swappable. Not equipped by default. */
public final class FastSnapAnimation implements MoveAnimationStyle {
    @Override public String id() { return "fast_snap"; }
    @Override public String displayName() { return "Fast Snap"; }
    @Override public long normalDurationMs() { return 360L; }
    @Override public long hitDurationMs() { return 500L; }
    @Override public float arcHeightFactor() { return 0.06f; }
    @Override public float scaleLiftFactor() { return 0.02f; }
    @Override public float interpolation(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return 1f - (1f - t) * (1f - t);
    }
}
