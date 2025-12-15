package com.skyblockin.voidsurvival.leaderboard;

public enum LeaderboardType {

    KILLSTREAK("Kill Streak", "killStreak"),
    KILLS("Kills", "kills");

    private final String displayName;
    private final String columnKey;

    LeaderboardType(String displayName, String columnKey) {
        this.displayName = displayName;
        this.columnKey = columnKey;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getColumnKey() {
        return columnKey;
    }

}
