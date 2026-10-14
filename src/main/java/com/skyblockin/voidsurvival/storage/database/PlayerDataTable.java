package com.skyblockin.voidsurvival.storage.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.leaderboard.LeaderboardType;
import com.skyblockin.voidsurvival.social.FriendList;
import com.skyblockin.voidsurvival.storage.NamedLocationMap;
import com.skyblockin.voidsurvival.storage.PlayerData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayerDataTable extends SQLiteTable {

    public PlayerDataTable(Database database) {
        super(database, "players",
            "id TEXT PRIMARY KEY", "player_data TEXT"
        );
    }

    public CompletableFuture<PlayerData> loadPlayer(String uuid) {
        return database.execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id, player_data FROM players WHERE id = ?")) {

                statement.setString(1, uuid);

                try (ResultSet rs = statement.executeQuery()) {

                    if (rs.next()) {
                        return fromResultSet(rs);
                    }

                    return null;
                }
            }
        });
    }

    public CompletableFuture<PlayerData> loadPlayerByName(String name) {

        return database.execute(connection -> {

            try (PreparedStatement statement = connection.prepareStatement(
                """ 
                SELECT id, player_data FROM players
                WHERE JSON_EXTRACT(player_data, '$.lastKnownUserName') = ?
                """
            )) {

                statement.setString(1, name);

                try (ResultSet rs = statement.executeQuery()) {

                    if (!rs.next()) {
                        return null;
                    }

                    return fromResultSet(rs);
                }
            }

        });

    }

    public CompletableFuture<Void> savePlayers(List<PlayerData> dataList) {

        return database.consume(connection -> {

            String sql = """
             INSERT INTO players (id, player_data) VALUES (?, ?)
             ON CONFLICT(id) DO UPDATE
             SET player_data = excluded.player_data
            """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                for (var entry : dataList) {
                    statement.setString(1, entry.uuid.toString());
                    statement.setString(2, Json.toString(entry));
                    statement.addBatch();
                }

                statement.executeBatch();
            }
        });

    }

    public CompletableFuture<Void> savePlayers(Map<UUID, PlayerData> dataMap) {

        return database.consume(connection -> {

            String sql = """
             INSERT INTO players (id, player_data) VALUES (?, ?)
             ON CONFLICT(id) DO UPDATE
             SET player_data = excluded.player_data
            """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                for (var entry : dataMap.entrySet()) {
                    statement.setString(1, entry.getKey().toString());
                    statement.setString(2, Json.toString(entry.getValue()));
                    statement.addBatch();
                }

                statement.executeBatch();
            }
        });

    }

    public CompletableFuture<Void> deleteCampfireFromEveryoneById(String campfireId) {

        PlayerData.PLAYER_DATA_MAP.forEach((id, data) ->
            data.campfires.remove(campfireId)
        );

        return database.consume(connection -> {
            // Removed the trailing ')' from CONCAT so the JSON path formats correctly (e.g., $.campfires.campfireId)
            String sql = "UPDATE players SET player_data = JSON_REMOVE(player_data, CONCAT('$.campfires.', ?))";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, campfireId);
                statement.executeUpdate();
            }
        });

    }

    public CompletableFuture<Set<PlayerData>> getLeaderboard(LeaderboardType type, int limit) {

        return database.execute(connection -> {

            String sql = """
            SELECT id, player_data FROM players
            ORDER BY JSON_EXTRACT(player_data, CONCAT('$.', ?)) DESC
            LIMIT ?
            """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, type.getColumnKey());
                statement.setInt(2, limit);

                try (ResultSet rs = statement.executeQuery()) {

                    Set<PlayerData> leaderboard = new LinkedHashSet<>(limit);

                    while (rs.next()) {
                        leaderboard.add(fromResultSet(rs));
                    }

                    return leaderboard;
                }
            }
        });

    }

    private PlayerData fromResultSet(ResultSet rs) throws SQLException, JsonProcessingException {

        JsonNode playerData = Json.readJson(rs.getString("player_data"));

        PlayerData data = new PlayerData(UUID.fromString(rs.getString("id")));

        data.hasGeneratedIsland = playerData.path("hasGeneratedIsland").asBoolean(false);
        data.lastKnownUserName = playerData.path("lastKnownUserName").asText();
        data.kills = playerData.path("kills").asInt(0);
        data.killStreak = playerData.path("killStreak").asInt(0);
        data.chestsLooted = playerData.path("chestsLooted").asInt(0);
        data.friendList = Json.convert(playerData.path("friendList"), FriendList.class, new FriendList());
        data.lastChestOpenTimes = Json.convert(playerData.path("lastChestOpenTimes"), new TypeReference<>() {}, new HashMap<>());
        data.homes = Json.convert(playerData.path("homes"), NamedLocationMap.class, new NamedLocationMap());
        data.campfires = Json.convert(playerData.path("campfires"), NamedLocationMap.class, new NamedLocationMap());

        return data;
    }

}
