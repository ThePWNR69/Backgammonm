package com.george.backgammon.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pure move rules shared by all supported variants. */
public final class RulesEngine {
    private RulesEngine() {}

    public static List<List<Move>> playableSequences(GameState state, int player, List<Integer> diceRemaining, GameVariant variant) {
        if (diceRemaining.isEmpty()) return Collections.emptyList();
        List<List<Move>> sequences = enumerateSequences(state, player, diceRemaining, variant);
        if (sequences.isEmpty()) return Collections.emptyList();
        int maxLength = 0;
        for (List<Move> seq : sequences) maxLength = Math.max(maxLength, seq.size());
        if (maxLength == 0) return Collections.emptyList();
        int requiredDie = -1;
        if (maxLength == 1 && new HashSet<>(diceRemaining).size() > 1) {
            for (List<Move> seq : sequences) if (seq.size() == 1) requiredDie = Math.max(requiredDie, seq.get(0).die);
        }
        List<List<Move>> filtered = new ArrayList<>();
        for (List<Move> seq : sequences) {
            if (seq.size() != maxLength || seq.isEmpty()) continue;
            if (requiredDie != -1 && seq.get(0).die != requiredDie) continue;
            filtered.add(seq);
        }
        return filtered;
    }

    public static List<Move> allowedFirstMoves(GameState state, int player, List<Integer> diceRemaining, GameVariant variant) {
        List<List<Move>> sequences = playableSequences(state, player, diceRemaining, variant);
        if (sequences.isEmpty()) return Collections.emptyList();
        List<Move> result = new ArrayList<>();
        Set<Move> seen = new HashSet<>();
        for (List<Move> seq : sequences) if (!seq.isEmpty() && seen.add(seq.get(0))) result.add(seq.get(0));
        return result;
    }

    public static boolean isHit(GameState s, int player, Move move, GameVariant variant) {
        return variant == GameVariant.BACKGAMMON && move.to >= 1 && move.to <= 24 && s.points[move.to] == -player;
    }

    public static void apply(GameState s, int player, Move move, GameVariant variant) {
        if (move.from == BackgammonGame.BAR) {
            if (player == BackgammonGame.WHITE) s.whiteBar--; else s.blackBar--;
        } else {
            // In Mahbouseh the visible/top checker may be pinning an opponent underneath it.
            boolean releasingPin = variant == GameVariant.MAHBOUSEH &&
                    (player == BackgammonGame.WHITE ? s.whitePins[move.from] : s.blackPins[move.from]);
            if (releasingPin) {
                if (player == BackgammonGame.WHITE) s.whitePins[move.from] = false; else s.blackPins[move.from] = false;
                s.points[move.from] = -player; // trapped checker becomes free and visible again
            } else {
                s.points[move.from] -= player;
            }
        }

        if (move.to == BackgammonGame.OFF_WHITE || move.to == BackgammonGame.OFF_BLACK) {
            if (player == BackgammonGame.WHITE) s.whiteOff++; else s.blackOff++;
            return;
        }

        int dest = s.points[move.to];
        if (variant == GameVariant.BACKGAMMON) {
            if (player == BackgammonGame.WHITE && dest == -1) { s.points[move.to] = 0; s.blackBar++; }
            else if (player == BackgammonGame.BLACK && dest == 1) { s.points[move.to] = 0; s.whiteBar++; }
            s.points[move.to] += player;
        } else if (variant == GameVariant.MAHBOUSEH && dest == -player) {
            // Pin rather than hit. The pinner becomes the visible/top checker.
            s.points[move.to] = player;
            if (player == BackgammonGame.WHITE) s.whitePins[move.to] = true; else s.blackPins[move.to] = true;
        } else {
            s.points[move.to] += player;
        }
    }

    private static List<List<Move>> enumerateSequences(GameState s, int player, List<Integer> dice, GameVariant variant) {
        List<List<Move>> output = new ArrayList<>();
        enumerateRec(s, player, new ArrayList<>(dice), new ArrayList<>(), output, variant);
        return output;
    }

    private static void enumerateRec(GameState s, int player, List<Integer> dice, List<Move> prefix,
                                     List<List<Move>> output, GameVariant variant) {
        if (dice.isEmpty()) { output.add(new ArrayList<>(prefix)); return; }
        boolean advanced = false;
        Set<Integer> triedDice = new HashSet<>();
        for (int i = 0; i < dice.size(); i++) {
            int die = dice.get(i);
            if (!triedDice.add(die)) continue;
            List<Move> moves = legalMovesForDie(s, player, die, variant);
            if (moves.isEmpty()) continue;
            advanced = true;
            List<Integer> nextDice = new ArrayList<>(dice);
            nextDice.remove(i);
            for (Move move : moves) {
                GameState next = s.copy();
                apply(next, player, move, variant);
                prefix.add(move);
                enumerateRec(next, player, nextDice, prefix, output, variant);
                prefix.remove(prefix.size() - 1);
            }
        }
        if (!advanced) output.add(new ArrayList<>(prefix));
    }

    private static List<Move> legalMovesForDie(GameState s, int player, int die, GameVariant variant) {
        List<Move> moves = new ArrayList<>();
        if (variant == GameVariant.BACKGAMMON) {
            int barCount = player == BackgammonGame.WHITE ? s.whiteBar : s.blackBar;
            if (barCount > 0) {
                int target = player == BackgammonGame.WHITE ? 25 - die : die;
                if (isOpen(s, player, target, variant)) moves.add(new Move(BackgammonGame.BAR, target, die));
                return moves;
            }
        }

        boolean tawlaRunnerOnly = variant == GameVariant.TAWLA_31 && !tawlaFirstRunnerReleased(s, player);
        int startPoint = player == BackgammonGame.WHITE ? 24 : 1;
        boolean runnerHasLeftStart = player == BackgammonGame.WHITE ? s.points[startPoint] < 15 : s.points[startPoint] > -15;

        for (int point = 1; point <= 24; point++) {
            int value = s.points[point];
            if ((player == BackgammonGame.WHITE && value <= 0) || (player == BackgammonGame.BLACK && value >= 0)) continue;
            if (tawlaRunnerOnly) {
                if (!runnerHasLeftStart && point != startPoint) continue;
                if (runnerHasLeftStart && point == startPoint) continue;
            }
            int target = point + (player == BackgammonGame.WHITE ? -die : die);
            if (target >= 1 && target <= 24) {
                if (isOpen(s, player, target, variant)) moves.add(new Move(point, target, die));
            } else if (canBearOff(s, player, variant) && canBearOffFrom(s, player, point, die)) {
                moves.add(new Move(point, player == BackgammonGame.WHITE ? BackgammonGame.OFF_WHITE : BackgammonGame.OFF_BLACK, die));
            }
        }
        return moves;
    }

    private static boolean isOpen(GameState s, int player, int target, GameVariant variant) {
        int v = s.points[target];
        if (variant == GameVariant.TAWLA_31) return player == BackgammonGame.WHITE ? v >= 0 : v <= 0;
        if (variant == GameVariant.MAHBOUSEH) {
            if (v == 0 || Integer.signum(v) == player) return true;
            // A single opponent checker can be pinned; an opponent anchor cannot.
            return Math.abs(v) == 1 && !(player == BackgammonGame.WHITE ? s.blackPins[target] : s.whitePins[target]);
        }
        return player == BackgammonGame.WHITE ? v >= -1 : v <= 1;
    }

    private static boolean tawlaFirstRunnerReleased(GameState s, int player) {
        if (player == BackgammonGame.WHITE) {
            for (int p = 1; p <= 6; p++) if (s.points[p] > 0) return true;
        } else {
            for (int p = 19; p <= 24; p++) if (s.points[p] < 0) return true;
        }
        return false;
    }

    private static boolean canBearOff(GameState s, int player, GameVariant variant) {
        if (variant == GameVariant.BACKGAMMON && (player == BackgammonGame.WHITE ? s.whiteBar : s.blackBar) > 0) return false;
        if (player == BackgammonGame.WHITE) {
            for (int p = 7; p <= 24; p++) if (s.points[p] > 0) return false;
        } else {
            for (int p = 1; p <= 18; p++) if (s.points[p] < 0) return false;
        }
        return true;
    }

    private static boolean canBearOffFrom(GameState s, int player, int point, int die) {
        if (player == BackgammonGame.WHITE) {
            if (die == point) return true;
            if (die < point) return false;
            for (int p = point + 1; p <= 6; p++) if (s.points[p] > 0) return false;
            return true;
        }
        int distance = 25 - point;
        if (die == distance) return true;
        if (die < distance) return false;
        for (int p = 19; p < point; p++) if (s.points[p] < 0) return false;
        return true;
    }
}
