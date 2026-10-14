package com.skyblockin.voidsurvival.storage.database;

import com.skyblockin.voidsurvival.storage.Island;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IslandDataTable extends SQLiteTable{

    public IslandDataTable(Database database) {
        super(database, "islands", "id INTEGER PRIMARY KEY", "x INTEGER", "z INTEGER");
    }

    public CompletableFuture<Integer> getIslandCount() {

        return database.execute(connection -> {
            try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(
                "SELECT COUNT(id) FROM islands"
            )) {

                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        });

    }

    public CompletableFuture<List<Island>> getIslandsInRange(int x, int z, long range, int limit) {

        return database.execute(connection -> {

            String sql = """
            WITH const AS (SELECT ? AS x1, ? AS z1)
            SELECT id, x, z FROM const, islands WHERE
                ((const.x1 - x) * (const.x1 - x) + (const.z1 - z) * (const.z1 - z)) <= ?
            ORDER BY ((const.x1 - x) * (const.x1 - x) + (const.z1 - z) * (const.z1 - z))
            LIMIT ?
            """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, x);
                statement.setInt(2, z);
                statement.setLong(3, range * range); // Fixed the syntax error (assuming squared range)
                statement.setInt(4, limit);

                try (ResultSet rs = statement.executeQuery()) {

                    List<Island> islands = new ArrayList<>();

                    while (rs.next()) {
                        islands.add(new Island(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
                    }

                    return islands;
                }
            }
        });

    }

    public CompletableFuture<Void> saveIsland(int x, int z) {

        return database.consume(connection -> {
            String sql = "INSERT INTO islands (x, z) VALUES (" + x + ", " + z + ")";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        });

    }

    public CompletableFuture<List<Island>> getIslands() {

        return database.execute(connection -> {

            try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(
                "SELECT id, x, z FROM islands"
            )) {

                List<Island> islands = new ArrayList<>();

                while (rs.next()) {
                    islands.add(new Island(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
                }

                return islands;
            }
        });

    }

}
