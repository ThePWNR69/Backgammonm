package com.george.backgammon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BackgammonGame {
    public static final int WHITE = 1;
    public static final int BLACK = -1;
    public static final int BAR = -1;
    public static final int OFF_WHITE = 0;
    public static final int OFF_BLACK = 25;

    public static class Move {
        public final int from;
        public final int to;
        public final int die;

        public Move(int from, int to, int die) {
            this.from = from;
            this.to = to;
            this.die = die;
        }

        @Override public String toString() {
            return "Move{" + from + "->" + to + ", d=" + die + "}";
        }
    }

    private static class State {
        int[] points = new int[25]; // 1..24: +white count, -black count
        int whiteBar, blackBar, whiteOff, blackOff;

        State copy() {
            State s = new State();
            System.arraycopy(points, 0, s.points, 0, points.length);
            s.whiteBar = whiteBar;
            s.blackBar = blackBar;
            s.whiteOff = whiteOff;
            s.blackOff = blackOff;
            return s;
        }
    }

    private final Random random = new Random();
    private State state = new State();
    private int currentPlayer = WHITE;
    private final List<Integer> diceRemaining = new ArrayList<>();
    private int dieOne = 0, dieTwo = 0;
    private boolean rolled = false;
    private int winner = 0;

    public BackgammonGame() { reset(); }

    public void reset() {
        state = new State();
        // White moves 24 -> 1.
        state.points[24] = 2;
        state.points[13] = 5;
        state.points[8] = 3;
        state.points[6] = 5;
        // Black moves 1 -> 24.
        state.points[1] = -2;
        state.points[12] = -5;
        state.points[17] = -3;
        state.points[19] = -5;
        currentPlayer = WHITE;
        diceRemaining.clear();
        dieOne = dieTwo = 0;
        rolled = false;
        winner = 0;
    }

    public int getCurrentPlayer() { return currentPlayer; }
    public int getWinner() { return winner; }
    public boolean hasRolled() { return rolled; }
    public int getDieOne() { return dieOne; }
    public int getDieTwo() { return dieTwo; }
    public List<Integer> getDiceRemaining() { return Collections.unmodifiableList(diceRemaining); }
    public int getWhiteBar() { return state.whiteBar; }
    public int getBlackBar() { return state.blackBar; }
    public int getWhiteOff() { return state.whiteOff; }
    public int getBlackOff() { return state.blackOff; }
    public int getPoint(int point) { return state.points[point]; }

    public void rollDice() {
        if (winner != 0 || rolled) return;
        dieOne = random.nextInt(6) + 1;
        dieTwo = random.nextInt(6) + 1;
        diceRemaining.clear();
        diceRemaining.add(dieOne);
        diceRemaining.add(dieTwo);
        if (dieOne == dieTwo) {
            diceRemaining.add(dieOne);
            diceRemaining.add(dieOne);
        }
        rolled = true;
        if (getAllowedFirstMoves().isEmpty()) endTurn();
    }

    public List<Move> getAllowedFirstMoves() {
        if (!rolled || winner != 0 || diceRemaining.isEmpty()) return Collections.emptyList();
        List<List<Move>> sequences = enumerateSequences(state, currentPlayer, diceRemaining);
        if (sequences.isEmpty()) return Collections.emptyList();

        int maxLength = 0;
        for (List<Move> seq : sequences) maxLength = Math.max(maxLength, seq.size());
        if (maxLength == 0) return Collections.emptyList();

        // If only one die can be played from a non-double roll, the higher die must be used.
        int requiredDie = -1;
        if (maxLength == 1 && distinctCount(diceRemaining) > 1) {
            for (List<Move> seq : sequences) {
                if (seq.size() == 1) requiredDie = Math.max(requiredDie, seq.get(0).die);
            }
        }

        List<Move> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (List<Move> seq : sequences) {
            if (seq.size() != maxLength || seq.isEmpty()) continue;
            Move m = seq.get(0);
            if (requiredDie != -1 && m.die != requiredDie) continue;
            String key = m.from + ":" + m.to + ":" + m.die;
            if (seen.add(key)) result.add(m);
        }
        return result;
    }

    public boolean applyMove(Move requested) {
        if (requested == null) return false;
        Move chosen = null;
        for (Move m : getAllowedFirstMoves()) {
            if (m.from == requested.from && m.to == requested.to && m.die == requested.die) {
                chosen = m; break;
            }
        }
        if (chosen == null) return false;

        applyToState(state, currentPlayer, chosen);
        diceRemaining.remove(Integer.valueOf(chosen.die));
        if (state.whiteOff >= 15) winner = WHITE;
        if (state.blackOff >= 15) winner = BLACK;
        if (winner != 0) {
            rolled = false;
            diceRemaining.clear();
            return true;
        }
        if (diceRemaining.isEmpty() || getAllowedFirstMoves().isEmpty()) endTurn();
        return true;
    }

    private void endTurn() {
        diceRemaining.clear();
        rolled = false;
        dieOne = dieTwo = 0;
        currentPlayer = -currentPlayer;
    }

    private static int distinctCount(List<Integer> dice) {
        return new HashSet<>(dice).size();
    }

    private static List<List<Move>> enumerateSequences(State s, int player, List<Integer> dice) {
        List<List<Move>> output = new ArrayList<>();
        enumerateRec(s, player, new ArrayList<>(dice), new ArrayList<>(), output);
        return output;
    }

    private static void enumerateRec(State s, int player, List<Integer> dice,
                                     List<Move> prefix, List<List<Move>> output) {
        if (dice.isEmpty()) {
            output.add(new ArrayList<>(prefix));
            return;
        }

        boolean advanced = false;
        Set<Integer> triedDice = new HashSet<>();
        for (int i = 0; i < dice.size(); i++) {
            int die = dice.get(i);
            if (!triedDice.add(die)) continue;
            List<Move> moves = legalMovesForDie(s, player, die);
            if (moves.isEmpty()) continue;
            advanced = true;
            List<Integer> nextDice = new ArrayList<>(dice);
            nextDice.remove(i);
            for (Move move : moves) {
                State next = s.copy();
                applyToState(next, player, move);
                prefix.add(move);
                enumerateRec(next, player, nextDice, prefix, output);
                prefix.remove(prefix.size() - 1);
            }
        }
        if (!advanced) output.add(new ArrayList<>(prefix));
    }

    private static List<Move> legalMovesForDie(State s, int player, int die) {
        List<Move> moves = new ArrayList<>();
        int barCount = player == WHITE ? s.whiteBar : s.blackBar;
        if (barCount > 0) {
            int target = player == WHITE ? 25 - die : die;
            if (isOpen(s, player, target)) moves.add(new Move(BAR, target, die));
            return moves;
        }

        for (int point = 1; point <= 24; point++) {
            int value = s.points[point];
            if ((player == WHITE && value <= 0) || (player == BLACK && value >= 0)) continue;
            int target = point + (player == WHITE ? -die : die);
            if (target >= 1 && target <= 24) {
                if (isOpen(s, player, target)) moves.add(new Move(point, target, die));
            } else if (canBearOff(s, player) && canBearOffFrom(s, player, point, die)) {
                moves.add(new Move(point, player == WHITE ? OFF_WHITE : OFF_BLACK, die));
            }
        }
        return moves;
    }

    private static boolean isOpen(State s, int player, int target) {
        int v = s.points[target];
        return player == WHITE ? v >= -1 : v <= 1;
    }

    private static boolean canBearOff(State s, int player) {
        if ((player == WHITE ? s.whiteBar : s.blackBar) > 0) return false;
        if (player == WHITE) {
            for (int p = 7; p <= 24; p++) if (s.points[p] > 0) return false;
        } else {
            for (int p = 1; p <= 18; p++) if (s.points[p] < 0) return false;
        }
        return true;
    }

    private static boolean canBearOffFrom(State s, int player, int point, int die) {
        if (player == WHITE) {
            if (die == point) return true;
            if (die < point) return false;
            // Oversized die: no white checker may be farther from bear-off.
            for (int p = point + 1; p <= 6; p++) if (s.points[p] > 0) return false;
            return true;
        } else {
            int distance = 25 - point;
            if (die == distance) return true;
            if (die < distance) return false;
            // Oversized die: no black checker may be farther from bear-off.
            for (int p = 19; p < point; p++) if (s.points[p] < 0) return false;
            return true;
        }
    }

    private static void applyToState(State s, int player, Move move) {
        if (move.from == BAR) {
            if (player == WHITE) s.whiteBar--; else s.blackBar--;
        } else {
            s.points[move.from] -= player;
        }

        if (move.to == OFF_WHITE || move.to == OFF_BLACK) {
            if (player == WHITE) s.whiteOff++; else s.blackOff++;
            return;
        }

        int dest = s.points[move.to];
        if (player == WHITE && dest == -1) {
            s.points[move.to] = 0;
            s.blackBar++;
        } else if (player == BLACK && dest == 1) {
            s.points[move.to] = 0;
            s.whiteBar++;
        }
        s.points[move.to] += player;
    }
}
