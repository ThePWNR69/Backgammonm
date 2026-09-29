package com.george.backgammon.progression;

public final class MatchReward {
    public final int xp;
    public final int gold;
    public final int baseXp;
    public final int baseGold;
    public final int performanceXp;
    public final int performanceGold;
    public final int hintPercent;

    public MatchReward(int xp, int gold, int baseXp, int baseGold, int performanceXp, int performanceGold, int hintPercent) {
        this.xp = xp; this.gold = gold; this.baseXp = baseXp; this.baseGold = baseGold;
        this.performanceXp = performanceXp; this.performanceGold = performanceGold; this.hintPercent = hintPercent;
    }
}
