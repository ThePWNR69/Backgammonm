package com.george.backgammon.progression;

import com.george.backgammon.cosmetics.CosmeticItem;
import com.george.backgammon.cosmetics.UnlockRule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Progression model only; persistence/shop UI comes later. Keeping this out of rendering/gameplay
 * means level locks, coins, achievements and boss rewards won't contaminate the rules engine.
 */
public final class PlayerProgress {
    private int level = 1;
    private int coins = 0;
    private final Set<String> achievementIds = new HashSet<>();
    private final Set<String> defeatedBossIds = new HashSet<>();
    private final Set<String> ownedCosmeticIds = new HashSet<>();

    public int getLevel() { return level; }
    public int getCoins() { return coins; }
    public Set<String> getOwnedCosmeticIds() { return Collections.unmodifiableSet(ownedCosmeticIds); }

    public void setLevel(int level) { this.level = Math.max(1, level); }
    public void addCoins(int amount) { coins = Math.max(0, coins + Math.max(0, amount)); }
    public void grantAchievement(String id) { if (id != null) achievementIds.add(id); }
    public void defeatBoss(String id) { if (id != null) defeatedBossIds.add(id); }
    public void grantCosmetic(String id) { if (id != null) ownedCosmeticIds.add(id); }

    public boolean canUnlock(CosmeticItem<?> item) {
        UnlockRule rule = item.unlockRule;
        switch (rule.type) {
            case DEFAULT: return true;
            case LEVEL_AND_COINS: return level >= rule.requiredLevel && coins >= rule.coinPrice;
            case ACHIEVEMENT: return achievementIds.contains(rule.sourceId);
            case BOSS: return defeatedBossIds.contains(rule.sourceId);
            default: return false;
        }
    }
}
