package com.george.backgammon.game;

import java.util.Objects;

public final class Move {
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

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Move)) return false;
        Move move = (Move) other;
        return from == move.from && to == move.to && die == move.die;
    }

    @Override public int hashCode() {
        return Objects.hash(from, to, die);
    }
}
