package com.george.backgammon.ai;

import com.george.backgammon.game.BackgammonGame;
import com.george.backgammon.game.Move;
import java.util.List;

public interface AiStrategy {
    String id();
    String displayName();
    List<Move> chooseTurn(BackgammonGame game);
}
