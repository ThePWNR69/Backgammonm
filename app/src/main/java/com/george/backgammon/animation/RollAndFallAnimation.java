package com.george.backgammon.animation;

/**
 * Reserved premium animation slot for the rolling-on-edge then falling-flat effect discussed for
 * a high-tier cosmetic. The current renderer uses its timing/arc now; rotation can be added later
 * without changing gameplay or loadout APIs.
 */
public final class RollAndFallAnimation implements MoveAnimationStyle {
    @Override public String id() { return "roll_and_fall"; }
    @Override public String displayName() { return "Roll & Fall"; }
    @Override public long normalDurationMs() { return 1350L; }
    @Override public long hitDurationMs() { return 1550L; }
    @Override public float arcHeightFactor() { return 0.20f; }
    @Override public float scaleLiftFactor() { return 0.08f; }
    @Override public float interpolation(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }
}
