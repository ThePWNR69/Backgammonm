package com.george.backgammon.cosmetics;

/** Future progression hook: shop items, achievement boards and boss-exclusive sets use one model. */
public final class UnlockRule {
    public enum Type { DEFAULT, LEVEL_AND_COINS, ACHIEVEMENT, BOSS }

    public final Type type;
    public final int requiredLevel;
    public final int coinPrice;
    public final String sourceId;

    private UnlockRule(Type type, int requiredLevel, int coinPrice, String sourceId) {
        this.type = type;
        this.requiredLevel = requiredLevel;
        this.coinPrice = coinPrice;
        this.sourceId = sourceId;
    }

    public static UnlockRule defaultUnlocked() { return new UnlockRule(Type.DEFAULT, 0, 0, null); }
    public static UnlockRule shop(int level, int price) { return new UnlockRule(Type.LEVEL_AND_COINS, level, price, null); }
    public static UnlockRule achievement(String id) { return new UnlockRule(Type.ACHIEVEMENT, 0, 0, id); }
    public static UnlockRule boss(String id) { return new UnlockRule(Type.BOSS, 0, 0, id); }
}
