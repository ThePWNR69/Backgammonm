package com.george.backgammon.progression;

import com.george.backgammon.game.GameVariant;

/** Economy tuning lives here so rulesets never contain currency logic. */
public final class RewardCalculator {
    private RewardCalculator() {}

    public static MatchReward calculate(boolean won, int checkersBorneOff, int difficulty, int hintsUsed,
                                        boolean versusAi, GameVariant variant) {
        int baseXp = won ? 100 : 60;
        int baseGold = won ? 60 : 30;
        int perfXp = Math.min(30, Math.max(0, checkersBorneOff) * 2);
        int perfGold = Math.min(15, Math.max(0, checkersBorneOff));

        double difficultyMultiplier = 1.0;
        if (versusAi) difficultyMultiplier = difficulty <= 0 ? 0.80 : (difficulty >= 2 ? 1.30 : 1.0);

        // Variants start economy-neutral. This hook lets telemetry rebalance XP/gold per average minute later.
        double variantMultiplier = 1.0;
        if (variant == GameVariant.MAHBOUSEH) variantMultiplier = 1.0;
        if (variant == GameVariant.TAWLA_31) variantMultiplier = 1.0;

        int hintPercent = hintsUsed <= 0 ? 100 : hintsUsed == 1 ? 95 : hintsUsed == 2 ? 90 : hintsUsed == 3 ? 85 : 75;
        // Participation base is protected; hints reduce only result/performance upside.
        int protectedXp = 40;
        int protectedGold = 20;
        double hintMultiplier = hintPercent / 100.0;
        int xp = protectedXp + (int)Math.round(((baseXp - protectedXp) + perfXp) * difficultyMultiplier * variantMultiplier * hintMultiplier);
        int gold = protectedGold + (int)Math.round(((baseGold - protectedGold) + perfGold) * difficultyMultiplier * variantMultiplier * hintMultiplier);
        return new MatchReward(Math.max(1, xp), Math.max(1, gold), baseXp, baseGold, perfXp, perfGold, hintPercent);
    }
}
