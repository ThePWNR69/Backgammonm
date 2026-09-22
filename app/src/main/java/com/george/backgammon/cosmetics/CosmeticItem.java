package com.george.backgammon.cosmetics;

public final class CosmeticItem<T> {
    public final String id;
    public final String displayName;
    public final CosmeticCategory category;
    public final String rarity;
    public final UnlockRule unlockRule;
    public final T content;

    public CosmeticItem(String id, String displayName, CosmeticCategory category,
                        String rarity, UnlockRule unlockRule, T content) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.rarity = rarity;
        this.unlockRule = unlockRule;
        this.content = content;
    }
}
