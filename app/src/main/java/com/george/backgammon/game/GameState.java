package com.george.backgammon.game;

/** Mutable internal position. Rendering and AI receive copies/snapshots through BackgammonGame. */
public final class GameState {
    final int[] points = new int[25];
    int whiteBar;
    int blackBar;
    int whiteOff;
    int blackOff;

    public GameState copy() {
        GameState s = new GameState();
        System.arraycopy(points, 0, s.points, 0, points.length);
        s.whiteBar = whiteBar;
        s.blackBar = blackBar;
        s.whiteOff = whiteOff;
        s.blackOff = blackOff;
        return s;
    }

    public int getPoint(int point) { return points[point]; }
    public int getWhiteBar() { return whiteBar; }
    public int getBlackBar() { return blackBar; }
    public int getWhiteOff() { return whiteOff; }
    public int getBlackOff() { return blackOff; }
}
