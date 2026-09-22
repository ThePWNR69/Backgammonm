package com.george.backgammon.boss;

import com.george.backgammon.ai.AiStrategy;

import java.util.Collections;
import java.util.List;

/** Campaign/boss metadata kept separate from the match engine. */
public final class BossDefinition {
    public final String id;
    public final String displayName;
    public final AiStrategy ai;
    public final String boardThemeId;
    public final String checkerThemeId;
    public final List<String> rewardCosmeticIds;

    public BossDefinition(String id, String displayName, AiStrategy ai,
                          String boardThemeId, String checkerThemeId, List<String> rewardCosmeticIds) {
        this.id = id;
        this.displayName = displayName;
        this.ai = ai;
        this.boardThemeId = boardThemeId;
        this.checkerThemeId = checkerThemeId;
        this.rewardCosmeticIds = Collections.unmodifiableList(rewardCosmeticIds);
    }
}
