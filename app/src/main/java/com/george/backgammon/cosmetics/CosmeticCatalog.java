package com.george.backgammon.cosmetics;

import com.george.backgammon.animation.AnimationCatalog;
import com.george.backgammon.animation.MoveAnimationStyle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Central cosmetic registry. v1.0 exposes three test options per major visual category. */
public final class CosmeticCatalog {
    private CosmeticCatalog() {}

    public static final BoardTheme CLASSIC_WALNUT = new BoardTheme(
            "board_classic_walnut", "Classic Walnut",
            0xFF5A2E17, 0xFF7D4624, 0xFF211813,
            0xFFF0E2C2, 0xFF8D452E, 0xFFC79243, 0xFFB77B3E,
            "boards/classic_walnut/board_base.webp",
            "boards/classic_walnut/frame.webp", "boards/classic_walnut/field.webp",
            "boards/classic_walnut/points.webp", "boards/classic_walnut/bar.webp");

    public static final BoardTheme GREEK_MARBLE = new BoardTheme(
            "board_greek_marble", "Aegean Marble",
            0xFFD8D0C1, 0xFFF2EEE5, 0xFFF7F2E8,
            0xFFF7F2E8, 0xFF163A63, 0xFFC9A04C, 0xFFC9A04C,
            null, "boards/greek_marble/frame.webp", "boards/greek_marble/field.webp",
            "boards/greek_marble/points.webp", "boards/greek_marble/bar.webp");

    public static final BoardTheme MODERN_TEAL = new BoardTheme(
            "board_modern_teal", "Midnight Teal",
            0xFF111A1C, 0xFF213136, 0xFF0F181A,
            0xFFDCE7E4, 0xFF16706C, 0xFFB79358, 0xFF2D8D88,
            null, "boards/modern_teal/frame.webp", "boards/modern_teal/field.webp",
            "boards/modern_teal/points.webp", "boards/modern_teal/bar.webp");

    public static final CheckerTheme IVORY_WALNUT = new CheckerTheme(
            "checkers_ivory_walnut", "Ivory & Walnut",
            0xFFF2E6CC, 0xFFBDAA87, 0xFF4B2619, 0xFF8F5938,
            "checkers/ivory_walnut/light.webp", "checkers/ivory_walnut/dark.webp");

    public static final CheckerTheme MARBLE_BRONZE = new CheckerTheme(
            "checkers_marble_bronze", "Marble & Bronze",
            0xFFF6F2E9, 0xFFCABDA4, 0xFF8D512C, 0xFFD09A56,
            "checkers/marble_bronze/light.webp", "checkers/marble_bronze/dark.webp");

    public static final CheckerTheme OBSIDIAN_GOLD = new CheckerTheme(
            "checkers_obsidian_gold", "Obsidian & Gold",
            0xFFE7D8AF, 0xFFC59A47, 0xFF151414, 0xFFB4873C,
            "checkers/obsidian_gold/light.webp", "checkers/obsidian_gold/dark.webp");

    public static final DiceTheme IVORY_DICE = new DiceTheme(
            "dice_ivory", "Ivory Dice", 0xFFF4E8CE, 0xFF17110E, 0xFFC8B895,
            "dice/ivory/die_%d.webp");

    public static final HighlightTheme CLASSIC_HIGHLIGHTS = new HighlightTheme(
            "highlight_classic",
            0xFF3DA8FF,  // selected checker
            0xFF63E67B,  // one-die legal move
            0xFFF5C451,  // two-dice same-checker destination
            0xFFFF9E38,  // capture
            0xFFFF554A); // invalid

    public static final List<BoardTheme> BOARD_THEMES = Collections.unmodifiableList(Arrays.asList(
            CLASSIC_WALNUT, GREEK_MARBLE, MODERN_TEAL));
    public static final List<CheckerTheme> CHECKER_THEMES = Collections.unmodifiableList(Arrays.asList(
            IVORY_WALNUT, MARBLE_BRONZE, OBSIDIAN_GOLD));
    public static final List<MoveAnimationStyle> MOVE_ANIMATIONS = Collections.unmodifiableList(Arrays.asList(
            AnimationCatalog.SMOOTH_SLIDE, AnimationCatalog.FAST_SNAP, AnimationCatalog.ROLL_AND_FALL));

    public static BoardTheme boardById(String id) {
        for (BoardTheme item : BOARD_THEMES) if (item.id.equals(id)) return item;
        return CLASSIC_WALNUT;
    }

    public static CheckerTheme checkersById(String id) {
        for (CheckerTheme item : CHECKER_THEMES) if (item.id.equals(id)) return item;
        return IVORY_WALNUT;
    }

    public static MoveAnimationStyle animationById(String id) {
        for (MoveAnimationStyle item : MOVE_ANIMATIONS) if (item.id().equals(id)) return item;
        return AnimationCatalog.SMOOTH_SLIDE;
    }

    // Unlock metadata remains in place for the later shop/progression system. v1.0 lets all
    // three showcase options be equipped from the Graphics Lab so their feel can be tested now.
    public static final CosmeticItem<BoardTheme> ITEM_CLASSIC_WALNUT = new CosmeticItem<>(
            CLASSIC_WALNUT.id, CLASSIC_WALNUT.displayName, CosmeticCategory.BOARD,
            "Common", UnlockRule.defaultUnlocked(), CLASSIC_WALNUT);
    public static final CosmeticItem<BoardTheme> ITEM_GREEK_MARBLE = new CosmeticItem<>(
            GREEK_MARBLE.id, GREEK_MARBLE.displayName, CosmeticCategory.BOARD,
            "Boss", UnlockRule.boss("boss_bronze_strategist"), GREEK_MARBLE);
    public static final CosmeticItem<BoardTheme> ITEM_MODERN_TEAL = new CosmeticItem<>(
            MODERN_TEAL.id, MODERN_TEAL.displayName, CosmeticCategory.BOARD,
            "Rare", UnlockRule.achievement("achievement_modern_mastery"), MODERN_TEAL);

    public static final CosmeticItem<CheckerTheme> ITEM_IVORY_WALNUT = new CosmeticItem<>(
            IVORY_WALNUT.id, IVORY_WALNUT.displayName, CosmeticCategory.CHECKERS,
            "Common", UnlockRule.defaultUnlocked(), IVORY_WALNUT);
    public static final CosmeticItem<CheckerTheme> ITEM_MARBLE_BRONZE = new CosmeticItem<>(
            MARBLE_BRONZE.id, MARBLE_BRONZE.displayName, CosmeticCategory.CHECKERS,
            "Epic", UnlockRule.shop(20, 6500), MARBLE_BRONZE);
    public static final CosmeticItem<CheckerTheme> ITEM_OBSIDIAN_GOLD = new CosmeticItem<>(
            OBSIDIAN_GOLD.id, OBSIDIAN_GOLD.displayName, CosmeticCategory.CHECKERS,
            "Legendary", UnlockRule.shop(35, 14000), OBSIDIAN_GOLD);

    public static final CosmeticItem<MoveAnimationStyle> ITEM_SMOOTH_SLIDE = new CosmeticItem<>(
            AnimationCatalog.SMOOTH_SLIDE.id(), AnimationCatalog.SMOOTH_SLIDE.displayName(),
            CosmeticCategory.MOVE_ANIMATION, "Common", UnlockRule.defaultUnlocked(), AnimationCatalog.SMOOTH_SLIDE);
    public static final CosmeticItem<MoveAnimationStyle> ITEM_FAST_SNAP = new CosmeticItem<>(
            AnimationCatalog.FAST_SNAP.id(), AnimationCatalog.FAST_SNAP.displayName(),
            CosmeticCategory.MOVE_ANIMATION, "Rare", UnlockRule.shop(10, 2500), AnimationCatalog.FAST_SNAP);
    public static final CosmeticItem<MoveAnimationStyle> ITEM_ROLL_AND_FALL = new CosmeticItem<>(
            AnimationCatalog.ROLL_AND_FALL.id(), AnimationCatalog.ROLL_AND_FALL.displayName(),
            CosmeticCategory.MOVE_ANIMATION, "Legendary", UnlockRule.shop(45, 25000), AnimationCatalog.ROLL_AND_FALL);

    public static final List<CosmeticItem<?>> ALL = Collections.unmodifiableList(Arrays.asList(
            ITEM_CLASSIC_WALNUT, ITEM_GREEK_MARBLE, ITEM_MODERN_TEAL,
            ITEM_IVORY_WALNUT, ITEM_MARBLE_BRONZE, ITEM_OBSIDIAN_GOLD,
            ITEM_SMOOTH_SLIDE, ITEM_FAST_SNAP, ITEM_ROLL_AND_FALL));

    public static PlayerLoadout defaultLoadout() {
        return new PlayerLoadout(CLASSIC_WALNUT, IVORY_WALNUT, IVORY_DICE,
                CLASSIC_HIGHLIGHTS, AnimationCatalog.SMOOTH_SLIDE);
    }
}
