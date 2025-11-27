package com.skyblockin.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.VoidSurvival;
import com.skyblockin.config.Json;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Database {

    private static final String DATABASE_URL = "jdbc:sqlite:" + VoidSurvival.getInstance().getDataFolder().getAbsolutePath() + "/playerdata.db";

    public static void createDataBase() throws SQLException {

        String createPlayerDataTable =
            """
            CREATE TABLE IF NOT EXISTS players (
                id TEXT PRIMARY KEY,
                player_data TEXT
            )
            """;

        String createIslandTable =
            """
            CREATE TABLE IF NOT EXISTS islands (
                id INTEGER PRIMARY KEY,
                x INTEGER,
                z INTEGER
            )
            """;

        Statement statement = getConnection().createStatement();

        statement.execute(createPlayerDataTable);
        statement.execute(createIslandTable);

        statement.close();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static int getIslandCount() {

        try {

            Statement statement = getConnection().createStatement();

            ResultSet rs = statement.executeQuery("SELECT COUNT(id) FROM islands");

            int count = rs.getInt(1);

            statement.close();

            return count;

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to get islands from database!", ex);
            return 0;
        }

    }

    public static void saveIsland(int x, int z) {

        try {

            Statement statement = getConnection().createStatement();

            statement.executeUpdate("INSERT INTO islands (x, z) VALUES(" + x + ", " + z + ")");

            statement.close();

        } catch (SQLException ex) {
            VoidSurvival.logError("Failed to save island at coordinates " + x + ", " + z, ex);
        }

    }

    public static List<Island> getIslands() {

        try {

            Statement statement = getConnection().createStatement();

            ResultSet rs = statement.executeQuery("SELECT id, x, z FROM islands");

            ArrayList<Island> islands = new ArrayList<>();

            while (rs.next()) {
                islands.add(new Island(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
            }

            statement.close();

            return islands;

        } catch (SQLException ex) {
            VoidSurvival.logError("Failed to get islands from database!", ex);
            return new ArrayList<>();
        }

    }

    private static final String loadPlayerData =
        """
        SELECT id, player_data FROM players WHERE id = ?
        """;

    public static PlayerData loadPlayer(UUID uuid) throws PlayerDataException {

        try {

            PreparedStatement statement = getConnection().prepareStatement(loadPlayerData);

            statement.setString(1, uuid.toString());

            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                VoidSurvival.logDebug(uuid + " did not exist in the database, creating a new profile for them...");
                return new PlayerData(uuid);
            }

            JsonNode playerData = Json.readJson(rs.getString("player_data"));

            PlayerData data = new PlayerData(UUID.fromString(rs.getString("id")));

            data.kills = playerData.at("/kills").asInt(0);
            data.killStreak = playerData.at("/killStreak").asInt(0);

            if (playerData.hasNonNull("homes")) {
                data.homes = Json.nodeToValue(playerData.at("/homes"), HomeMap.class);
            } else {
                data.homes = new HomeMap();
            }

            statement.close();

            return data;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new PlayerDataException("Failed to load player data: " + ex.getMessage());
        }
    }

    private static final String upsertPlayerData =
        """
        INSERT INTO players(id, player_data) VALUES(?, ?)
        ON CONFLICT(id) DO UPDATE
        SET player_data = EXCLUDED.player_data
        """;

    public static void savePlayer(PlayerData data) throws PlayerDataException {

        try {

            PreparedStatement statement = getConnection().prepareStatement(upsertPlayerData);

            String stringData = Json.toJson(data).toString();

            statement.setString(1, data.uuid.toString());
            statement.setString(2, stringData);

            statement.executeUpdate();

            statement.close();

        } catch (Exception ex) {
            throw new PlayerDataException("Failed to save player data: " + ex.getMessage());
        }

    }

    public static void savePlayers(Collection<PlayerData> dataList) throws PlayerDataException {

        try {

            PreparedStatement statement = getConnection().prepareStatement(upsertPlayerData);

            for (PlayerData data : dataList) {

                String stringData = Json.toJson(data).toString();

                statement.setString(1, data.uuid.toString());
                statement.setString(2, stringData);

                statement.addBatch();
            }

            statement.executeBatch();

            statement.close();

        } catch (Exception ex) {
            throw new PlayerDataException("Failed to save player data: " + ex.getMessage());
        }

    }

}
