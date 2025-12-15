package com.skyblockin.voidsurvival.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.leaderboard.LeaderboardManager;
import com.skyblockin.voidsurvival.leaderboard.LeaderboardType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

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

        String createChestLocationTable =
            """
            CREATE TABLE IF NOT EXISTS chest_locations (
                location INTEGER,
                world TEXT,
                table_id TEXT,
                PRIMARY KEY (location, world)
            )
            """;

        String createGlobalDataTable =
            """
            CREATE TABLE IF NOT EXISTS global_data (
                id INTEGER PRIMARY KEY CHECK (id = 0),
                chunk_count INTEGER
            )
            """;

        Statement statement = getConnection().createStatement();

        statement.execute(createPlayerDataTable);
        statement.execute(createIslandTable);
        statement.execute(createChestLocationTable);
        statement.execute(createGlobalDataTable);

        statement.close();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void saveGlobalData() {

        try {

            PreparedStatement statement = getConnection().prepareStatement(
                """
                    INSERT INTO global_data(id, chunk_count) VALUES(0, ?)
                    ON CONFLICT(id) DO UPDATE
                    SET chunk_count = EXCLUDED.chunk_count
                    """
            );

            statement.setLong(1, VoidSurvival.getInstance().getWorldListener().getGeneratedChunkCount());

            statement.executeUpdate();
            statement.close();

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to save global data to database!", ex);
        }

    }

    public static void saveChestLocations(HashMap<Location, String> locations) {

        try {

            PreparedStatement statement = getConnection().prepareStatement(
            """
                INSERT INTO chest_locations(location, world, table_id) VALUES(?, ?, ?)
                ON CONFLICT(location, world) DO UPDATE SET
                    world = EXCLUDED.world,
                    table_id = EXCLUDED.table_id
                """
            );

            for (Map.Entry<Location, String> entry : locations.entrySet()) {

                Location key = entry.getKey();

                statement.setLong(1, BlockPosition.packToLong(entry.getKey()));
                statement.setString(2, key.getWorld().getName());
                statement.setString(3, entry.getValue());

                statement.addBatch();
            }

            statement.executeBatch();
            statement.close();

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to save chest locations to database!", ex);
        }

    }

    public static HashMap<Location, String> getChestLocations() {

        try {

            HashMap<Location, String> locations = new HashMap<>();

            Statement statement = getConnection().createStatement();

            ResultSet rs = statement.executeQuery("SELECT location, world, table_id FROM chest_locations");

            while (rs.next()) {

                BlockPosition position = BlockPosition.fromLong(rs.getLong(1));
                String worldName = rs.getString(2);
                String tableId = rs.getString(3);

                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    locations.put(position.toLocation(world), tableId);
                } else {
                    VoidSurvival.logError("Failed to load a chest location in world '%s' because the world does not exist!", worldName);
                }

            }

            statement.close();

            return locations;

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to load chest locations from database!", ex);
        }

        return null;
    }

    public static long getChunkCount() {

        try {

            Statement statement = getConnection().createStatement();

            ResultSet rs = statement.executeQuery("SELECT chunk_count FROM global_data WHERE id = 0");

            if (!rs.next()) {
                return 0;
            }

            long count = rs.getLong(1);

            statement.close();

            return count;

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to load chunk count!", ex);
        }

        return -1;
    }

    public static int getIslandCount() {

        try {

            Statement statement = getConnection().createStatement();

            ResultSet rs = statement.executeQuery("SELECT COUNT(id) FROM islands");

            int count = rs.getInt(1);

            statement.close();

            return count;

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to get island count from database!", ex);
            return 0;
        }

    }

    public static <T> T runQuery(Callable<T> callable, Consumer<Exception> exceptionConsumer) {

        try {
            return callable.call();
        } catch (Exception ex) {
            exceptionConsumer.accept(ex);
        }

        return null;
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

    public static Set<PlayerData> getLeaderboard(LeaderboardType type, int limit) {

        try {

            Statement statement = getConnection().createStatement();

            ResultSet rs = statement.executeQuery(String.format(
                "SELECT id, player_data FROM players ORDER BY JSON_EXTRACT(player_data, '$.%s') DESC LIMIT %d", type.getColumnKey(), limit
            ));

            Set<PlayerData> leaderboard = new HashSet<>(limit);

            while (rs.next()) {
                leaderboard.add(playerFromResultSet(rs));
            }

            statement.close();

            return leaderboard;

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to get leaderboard!", ex);
        }

        return null;
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

            PlayerData data = playerFromResultSet(rs);

            statement.close();

            return data;

        } catch (Exception ex) {
            throw new PlayerDataException("Failed to load player data: " + ex.getMessage(), ex);
        }
    }

    private static PlayerData playerFromResultSet(ResultSet rs) throws SQLException, JsonProcessingException {

        JsonNode playerData = Json.readJson(rs.getString("player_data"));

        PlayerData data = new PlayerData(UUID.fromString(rs.getString("id")));

        data.hasGeneratedIsland = playerData.at("/hasGeneratedIsland").asBoolean(false);
        data.lastKnownUserName = playerData.at("/lastKnownUserName").asText();
        data.kills = playerData.at("/kills").asInt(0);
        data.killStreak = playerData.at("/killStreak").asInt(0);

        if (playerData.hasNonNull("homes")) {
            data.homes = Json.nodeToValue(playerData.path("homes"), HomeMap.class);
        } else {
            data.homes = new HomeMap();
        }

        return data;
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
            throw new PlayerDataException("Failed to save player data: " + ex.getMessage(), ex);
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
            throw new PlayerDataException("Failed to save player data: " + ex.getMessage(), ex);
        }

    }

}
