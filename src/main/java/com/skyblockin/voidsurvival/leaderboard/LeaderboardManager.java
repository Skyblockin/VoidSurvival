package com.skyblockin.voidsurvival.leaderboard;

import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.PlayerData;

import java.util.*;

public class LeaderboardManager {

    public List<PlayerData> getLeaderboard(LeaderboardType type, int limit) {

        Set<PlayerData> leaderboard = Database.getLeaderboard(type, limit);

        if (leaderboard == null) {
            return null;
        }

        // Join the database leaderboard with the online players
        Set<PlayerData> currentlyOnline = switch (type) {
            case KILLSTREAK -> PlayerData.getTop(limit, Comparator.comparingInt(data -> data.killStreak));
            case KILLS -> PlayerData.getTop(limit, Comparator.comparingInt(data -> data.kills));
        };

        // Join the two sets together.
        // If a player appears in both the list of currently online players
        // and the list returned by the database, their more recent data
        // from the list of currently online players will be used.
        // This is because the set entry will not be overwritten on collision
        currentlyOnline.addAll(leaderboard);

        ArrayList<PlayerData> list = new ArrayList<>(currentlyOnline);

        // Sort the list based on the type of leaderboard
        switch (type) {
            case KILLSTREAK -> {
                list.sort(Comparator.comparingInt(data -> data.killStreak));
                list.removeIf(data -> data.killStreak == 0);
            }
            case KILLS -> {
                list.sort(Comparator.comparingInt(data -> data.kills));
                list.removeIf(data -> data.kills == 0);
            }
        }

        // Finally, limit the list to the desired size
        return list.subList(0, Math.min(list.size(), limit));
    }

}
