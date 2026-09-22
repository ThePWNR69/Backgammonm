package com.george.backgammon.boss;

import com.george.backgammon.ai.PositionalAi;
import com.george.backgammon.cosmetics.CosmeticCatalog;

import java.util.Arrays;

/** Example registry entry; campaign UI/gameplay will use this later. */
public final class BossCatalog {
    private BossCatalog() {}

    public static final BossDefinition BRONZE_STRATEGIST = new BossDefinition(
            "boss_bronze_strategist",
            "The Bronze Strategist",
            new PositionalAi(),
            CosmeticCatalog.GREEK_MARBLE.id,
            CosmeticCatalog.MARBLE_BRONZE.id,
            Arrays.asList(CosmeticCatalog.GREEK_MARBLE.id, CosmeticCatalog.MARBLE_BRONZE.id));
}
