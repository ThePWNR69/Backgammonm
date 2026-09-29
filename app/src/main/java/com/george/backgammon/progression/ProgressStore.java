package com.george.backgammon.progression;

import android.content.Context;
import android.content.SharedPreferences;

/** Small persistent wallet/XP store. Can later be replaced by Room/cloud sync without touching gameplay. */
public final class ProgressStore {
    private static final String PREFS = "backgammon_progression";
    private static final String XP = "xp";
    private static final String GOLD = "gold";
    private ProgressStore() {}

    public static int getXp(Context c) { return prefs(c).getInt(XP, 0); }
    public static int getGold(Context c) { return prefs(c).getInt(GOLD, 0); }
    public static int getLevel(Context c) { return levelForXp(getXp(c)); }

    public static void add(Context c, MatchReward reward) {
        SharedPreferences p = prefs(c);
        p.edit().putInt(XP, Math.max(0, p.getInt(XP, 0) + reward.xp))
                .putInt(GOLD, Math.max(0, p.getInt(GOLD, 0) + reward.gold)).apply();
    }

    public static int levelForXp(int xp) {
        int level = 1;
        int remaining = Math.max(0, xp);
        while (remaining >= xpForNextLevel(level)) { remaining -= xpForNextLevel(level); level++; }
        return level;
    }

    public static int xpForNextLevel(int level) { return 250 + Math.max(0, level - 1) * 75; }
    private static SharedPreferences prefs(Context c) { return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }
}
