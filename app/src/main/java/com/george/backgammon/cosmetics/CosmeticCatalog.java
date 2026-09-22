package com.george.backgammon.cosmetics;

import com.george.backgammon.animation.AnimationCatalog;
import com.george.backgammon.animation.MoveAnimationStyle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Central cosmetic registry. Shop/level/achievement metadata can attach here later. */
public final class CosmeticCatalog {
    private CosmeticCatalog() {}

    public static final BoardTheme CLASSIC_WALNUT = new BoardTheme(
            "board_classic_walnut", "Classic Walnut",
            0xFF5A2E17, 0xFF7D4624, 0xFF211813,
            0xFFF0E2C2, 0xFF8D452E, 0xFFC79243, 0xFFB77B3E,
            "boards/classic_walnut/frame.webp", "boards/classic_walnut/field.webp",
            "boards/classic_walnut/points.webp", "boards/classic_walnut/bar.webp");

    public static final BoardTheme GREEK_MARBLE = new BoardTheme(
            "board_greek_marble", "Aegean Marble",
            0xFFD8D0C1, 0xFFF2EEE5, 0xFFF7F2E8,
            0xFFF7F2E8, 0xFF163A63, 0xFFC9A04C, 0xFFC9A04C,
            "boards/greek_marble/frame.webp", "boards/greek_marble/field.webp",
            "boards/greek_marble/points.webp", "boards/greek_marble/bar.webp");

    public static final BoardTheme MODERN_TEAL = new BoardTheme(
            "board_modern_teal", "Midnight Teal",
            0xFF151C1D, 0xFF263335, 0xFF111718,
            0xFFE4E6E2, 0xFF176B68, 0xFFB68D51, 0xFF2D8682,
            "boards/modern_teal/frame.webp", "boards/modern_teal/field.webp",
            "boards/modern_teal/points.webp", "boards/modern_teal/bar.webp");

    public static final CheckerTheme IVORY_WALNUT = new CheckerTheme(
            "checkers_ivory_walnut", "Ivory & Walnut",
            0xFFF2E6CC, 0xFFBDAA87, 0xFF4B2619, 0xFF8F5938,
            "checkers/ivory_walnut/light.webp", "checkers/ivory_walnut/dark.webp");

    public static final CheckerTheme MARBLE_BRONZE = new CheckerTheme(
            "checkers_marble_bronze", "Marble & Bronze",
            0xFFF4F0E6, 0xFFCFC4AE, 0xFF8C542E, 0xFFC18A4B,
            "checkers/marble_bronze/light.webp", "checkers/marble_bronze/dark.webp");

    public static final DiceTheme IVORY_DICE = new DiceTheme(
            "dice_ivory", "Ivory Dice", 0xFFF4E8CE, 0xFF17110E, 0xFFC8B895,
            "dice/ivory/die_%d.webp");

    public static final HighlightTheme CLASSIC_HIGHLIGHTS = new HighlightTheme(
            "highlight_classic", 0xFF3DA8FF, 0xFF66E47C, 0xFFFF9E38, 0xFFFF554A);


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

    public static final CosmeticItem<MoveAnimationStyle> ITEM_SMOOTH_SLIDE = new CosmeticItem<>(
            AnimationCatalog.SMOOTH_SLIDE.id(), AnimationCatalog.SMOOTH_SLIDE.displayName(),
            CosmeticCategory.MOVE_ANIMATION, "Common", UnlockRule.defaultUnlocked(), AnimationCatalog.SMOOTH_SLIDE);
    public static final CosmeticItem<MoveAnimationStyle> ITEM_ROLL_AND_FALL = new CosmeticItem<>(
            AnimationCatalog.ROLL_AND_FALL.id(), AnimationCatalog.ROLL_AND_FALL.displayName(),
            CosmeticCategory.MOVE_ANIMATION, "Legendary", UnlockRule.shop(45, 25000), AnimationCatalog.ROLL_AND_FALL);

    public static final List<CosmeticItem<?>> ALL = Collections.unmodifiableList(Arrays.asList(
            ITEM_CLASSIC_WALNUT, ITEM_GREEK_MARBLE, ITEM_MODERN_TEAL,
            ITEM_IVORY_WALNUT, ITEM_MARBLE_BRONZE, ITEM_SMOOTH_SLIDE, ITEM_ROLL_AND_FALL));

    public static PlayerLoadout defaultLoadout() {
        return new PlayerLoadout(CLASSIC_WALNUT, IVORY_WALNUT, IVORY_DICE,
                CLASSIC_HIGHLIGHTS, AnimationCatalog.SMOOTH_SLIDE);
    }
}
