package com.george.backgammon.game;

/** Mutable internal position. Rendering and AI receive copies/snapshots through BackgammonGame. */
public final class GameState {
    final int[] points = new int[25];
    int whiteBar;
    int blackBar;
    int whiteOff;
    int blackOff;
    final boolean[] whitePins = new boolean[25];
    final boolean[] blackPins = new boolean[25];

    public GameState copy() {
        GameState s = new GameState();
        System.arraycopy(points, 0, s.points, 0, points.length);
        s.whiteBar = whiteBar;
        s.blackBar = blackBar;
        s.whiteOff = whiteOff;
        s.blackOff = blackOff;
        System.arraycopy(whitePins, 0, s.whitePins, 0, whitePins.length);
        System.arraycopy(blackPins, 0, s.blackPins, 0, blackPins.length);
        return s;
    }

    public int getPoint(int point) { return points[point]; }
    public int getWhiteBar() { return whiteBar; }
    public int getBlackBar() { return blackBar; }
    public int getWhiteOff() { return whiteOff; }
    public int getBlackOff() { return blackOff; }
    public boolean isWhitePinningAt(int point) { return whitePins[point]; }
    public boolean isBlackPinningAt(int point) { return blackPins[point]; }
}
