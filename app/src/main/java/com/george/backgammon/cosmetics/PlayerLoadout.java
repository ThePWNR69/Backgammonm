package com.george.backgammon.cosmetics;

import com.george.backgammon.animation.MoveAnimationStyle;

/** Equipped visual items. This will later be persisted per player/profile. */
public final class PlayerLoadout {
    public BoardTheme board;
    public CheckerTheme checkers;
    public DiceTheme dice;
    public HighlightTheme highlights;
    public MoveAnimationStyle moveAnimation;

    public PlayerLoadout(BoardTheme board, CheckerTheme checkers, DiceTheme dice,
                         HighlightTheme highlights, MoveAnimationStyle moveAnimation) {
        this.board = board;
        this.checkers = checkers;
        this.dice = dice;
        this.highlights = highlights;
        this.moveAnimation = moveAnimation;
    }
}
