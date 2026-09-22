package com.george.backgammon.ai;

import com.george.backgammon.game.BackgammonGame;
import com.george.backgammon.game.GameState;
import com.george.backgammon.game.Move;
import com.george.backgammon.game.RulesEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** First modular AI. Future Easy/Hard/Boss personalities can implement AiStrategy independently. */
public final class PositionalAi implements AiStrategy {
    private final Random random = new Random();

    @Override public String id() { return "positional_normal"; }
    @Override public String displayName() { return "Normal"; }

    @Override public List<Move> chooseTurn(BackgammonGame game) {
        List<List<Move>> sequences = game.getPlayableSequencesSnapshot();
        if (sequences.isEmpty()) return Collections.emptyList();
        int player = game.getCurrentPlayer();
        GameState before = game.getStateSnapshot();
        double best = -Double.MAX_VALUE;
        List<Move> bestSequence = Collections.emptyList();
        for (List<Move> seq : sequences) {
            GameState next = before.copy();
            for (Move m : seq) RulesEngine.apply(next, player, m);
            double score = evaluateState(next, player) + tacticalBonus(before, next, player, seq);
            score += random.nextDouble() * 0.35;
            if (score > best) {
                best = score;
                bestSequence = new ArrayList<>(seq);
            }
        }
        return bestSequence;
    }

    private static double tacticalBonus(GameState before, GameState after, int player, List<Move> seq) {
        int beforeOppBar = player == BackgammonGame.WHITE ? before.getBlackBar() : before.getWhiteBar();
        int afterOppBar = player == BackgammonGame.WHITE ? after.getBlackBar() : after.getWhiteBar();
        double score = (afterOppBar - beforeOppBar) * 22.0;
        for (Move m : seq) {
            if (m.to == BackgammonGame.OFF_WHITE || m.to == BackgammonGame.OFF_BLACK) score += 16.0;
        }
        return score;
    }

    private static double evaluateState(GameState s, int player) {
        int ownOff = player == BackgammonGame.WHITE ? s.getWhiteOff() : s.getBlackOff();
        int oppOff = player == BackgammonGame.WHITE ? s.getBlackOff() : s.getWhiteOff();
        int ownBar = player == BackgammonGame.WHITE ? s.getWhiteBar() : s.getBlackBar();
        int oppBar = player == BackgammonGame.WHITE ? s.getBlackBar() : s.getWhiteBar();
        double score = ownOff * 110.0 - oppOff * 95.0;
        score += oppBar * 34.0 - ownBar * 42.0;
        int ownPips = 0, ownBlots = 0, ownMade = 0, ownHomeMade = 0;
        for (int p = 1; p <= 24; p++) {
            int v = s.getPoint(p);
            int ownCount = player == BackgammonGame.WHITE ? Math.max(0, v) : Math.max(0, -v);
            if (ownCount == 0) continue;
            int distance = player == BackgammonGame.WHITE ? p : 25 - p;
            ownPips += ownCount * distance;
            if (ownCount == 1) ownBlots++;
            if (ownCount >= 2) {
                ownMade++;
                boolean home = player == BackgammonGame.WHITE ? p <= 6 : p >= 19;
                if (home) ownHomeMade++;
            }
        }
        score -= ownPips * 0.42;
        score -= ownBlots * 4.8;
        score += ownMade * 5.0;
        score += ownHomeMade * 4.0;
        return score;
    }
}
