package com.george.backgammon.animation;

public final class AnimationCatalog {
    private AnimationCatalog() {}
    public static final MoveAnimationStyle SMOOTH_SLIDE = new SmoothSlideAnimation();
    public static final MoveAnimationStyle FAST_SNAP = new FastSnapAnimation();
    public static final MoveAnimationStyle ROLL_AND_FALL = new RollAndFallAnimation();
}
