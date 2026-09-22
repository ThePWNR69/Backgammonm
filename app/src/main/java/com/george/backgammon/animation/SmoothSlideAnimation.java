package com.george.backgammon.animation;

public final class SmoothSlideAnimation implements MoveAnimationStyle {
    @Override public String id() { return "smooth_slide"; }
    @Override public String displayName() { return "Smooth Slide"; }
    @Override public long normalDurationMs() { return 820L; }
    @Override public long hitDurationMs() { return 1040L; }
    @Override public float arcHeightFactor() { return 0.12f; }
    @Override public float scaleLiftFactor() { return 0.045f; }

    @Override public float interpolation(float t) {
        // Smoothstep: deterministic and renderer-agnostic.
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }
}
