package com.george.backgammon.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Game/session coordinator. Owns turn state and undo history, while RulesEngine owns legal move rules
 * and AiStrategy owns computer decisions. This class intentionally has no Android/rendering code.
 */
public final class BackgammonGame {
    public static final int WHITE = 1;
    public static final int BLACK = -1;
    public static final int BAR = -1;
    public static final int OFF_WHITE = 0;
    public static final int OFF_BLACK = 25;

    private static final class TurnSnapshot {
        final GameState state;
        final List<Integer> diceRemaining;
        final Move move;
        final boolean hit;

        TurnSnapshot(GameState state, List<Integer> diceRemaining, Move move, boolean hit) {
            this.state = state.copy();
            this.diceRemaining = new ArrayList<>(diceRemaining);
            this.move = move;
            this.hit = hit;
        }
    }

    private final Random random;
    private GameState state = new GameState();
    private int currentPlayer = WHITE;
    private final List<Integer> diceRemaining = new ArrayList<>();
    private final Deque<TurnSnapshot> undoHistory = new ArrayDeque<>();
    private int dieOne;
    private int dieTwo;
    private boolean rolled;
    private int winner;
    private boolean openingResolved;
    private boolean openingTie;

    public BackgammonGame() { this(new Random()); }
    public BackgammonGame(Random random) {
        this.random = random;
        reset();
    }

    public void reset() {
        state = new GameState();
        state.points[24] = 2;
        state.points[13] = 5;
        state.points[8] = 3;
        state.points[6] = 5;
        state.points[1] = -2;
        state.points[12] = -5;
        state.points[17] = -3;
        state.points[19] = -5;
        currentPlayer = WHITE;
        diceRemaining.clear();
        undoHistory.clear();
        dieOne = dieTwo = 0;
        rolled = false;
        winner = 0;
        openingResolved = false;
        openingTie = false;
    }

    public int getCurrentPlayer() { return currentPlayer; }
    public int getWinner() { return winner; }
    public boolean hasRolled() { return rolled; }
    public int getDieOne() { return dieOne; }
    public int getDieTwo() { return dieTwo; }
    public boolean isOpeningResolved() { return openingResolved; }
    public boolean wasOpeningTie() { return openingTie; }
    public List<Integer> getDiceRemaining() { return Collections.unmodifiableList(diceRemaining); }
    public int getWhiteBar() { return state.whiteBar; }
    public int getBlackBar() { return state.blackBar; }
    public int getWhiteOff() { return state.whiteOff; }
    public int getBlackOff() { return state.blackOff; }
    public int getPoint(int point) { return state.points[point]; }
    public int getMovesMadeThisTurn() { return undoHistory.size(); }
    public boolean canUndo() { return rolled && !undoHistory.isEmpty() && winner == 0; }
    public Move peekLastMove() { return undoHistory.isEmpty() ? null : undoHistory.peek().move; }
    public boolean peekLastMoveWasHit() { return !undoHistory.isEmpty() && undoHistory.peek().hit; }

    public GameState getStateSnapshot() { return state.copy(); }

    /** Exposed for pluggable AI strategies; returned sequences are detached from mutable game state. */
    public List<List<Move>> getPlayableSequencesSnapshot() {
        if (!rolled || winner != 0) return Collections.emptyList();
        List<List<Move>> source = RulesEngine.playableSequences(state, currentPlayer, diceRemaining);
        List<List<Move>> copy = new ArrayList<>();
        for (List<Move> seq : source) copy.add(new ArrayList<>(seq));
        return copy;
    }

    /** Standard opening roll. A tie leaves the opening unresolved so the same action can reroll. */
    public void rollOpeningDice() {
        if (winner != 0 || openingResolved || rolled) return;
        dieOne = random.nextInt(6) + 1;
        dieTwo = random.nextInt(6) + 1;
        diceRemaining.clear();
        undoHistory.clear();
        openingTie = dieOne == dieTwo;
        if (openingTie) {
            rolled = false;
            return;
        }
        openingResolved = true;
        currentPlayer = dieOne > dieTwo ? WHITE : BLACK;
        diceRemaining.add(dieOne);
        diceRemaining.add(dieTwo);
        rolled = true;
    }

    public void rollDice() {
        if (winner != 0 || rolled || !openingResolved) return;
        dieOne = random.nextInt(6) + 1;
        dieTwo = random.nextInt(6) + 1;
        diceRemaining.clear();
        undoHistory.clear();
        diceRemaining.add(dieOne);
        diceRemaining.add(dieTwo);
        if (dieOne == dieTwo) {
            diceRemaining.add(dieOne);
            diceRemaining.add(dieOne);
        }
        rolled = true;
    }

    public List<Move> getAllowedFirstMoves() {
        if (!rolled || winner != 0) return Collections.emptyList();
        return RulesEngine.allowedFirstMoves(state, currentPlayer, diceRemaining);
    }

    public boolean applyMove(Move requested) {
        if (requested == null || winner != 0) return false;
        Move chosen = null;
        for (Move m : getAllowedFirstMoves()) {
            if (m.equals(requested)) { chosen = m; break; }
        }
        if (chosen == null) return false;
        boolean hit = RulesEngine.isHit(state, currentPlayer, chosen);
        undoHistory.push(new TurnSnapshot(state, diceRemaining, chosen, hit));
        RulesEngine.apply(state, currentPlayer, chosen);
        diceRemaining.remove(Integer.valueOf(chosen.die));
        return true;
    }

    public boolean undoLastMove() {
        if (!canUndo()) return false;
        TurnSnapshot snapshot = undoHistory.pop();
        state = snapshot.state.copy();
        diceRemaining.clear();
        diceRemaining.addAll(snapshot.diceRemaining);
        return true;
    }

    public boolean canEndTurn() {
        if (!rolled || winner != 0) return false;
        if (state.whiteOff >= 15 || state.blackOff >= 15) return true;
        return diceRemaining.isEmpty() || getAllowedFirstMoves().isEmpty();
    }

    public boolean endTurn() {
        if (!canEndTurn()) return false;
        if (state.whiteOff >= 15) winner = WHITE;
        else if (state.blackOff >= 15) winner = BLACK;
        diceRemaining.clear();
        undoHistory.clear();
        rolled = false;
        dieOne = dieTwo = 0;
        openingTie = false;
        if (winner == 0) currentPlayer = -currentPlayer;
        return true;
    }
}
