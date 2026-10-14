package com.skyblockin.voidsurvival.storage.database;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.math.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChestDataTable extends SQLiteTable {

    public ChestDataTable(Database database) {
        super(database, "chest_locations",
            "location INTEGER",
            "world TEXT",
            "table_id TEXT",
            "PRIMARY KEY(location, world)"
        );
    }

    public CompletableFuture<Void> saveChestLocations(HashMap<Location, String> locations) {

        return database.consume(connection -> {

            String sql = """
            INSERT INTO chest_locations(location, world, table_id) VALUES(?, ?, ?)
            ON CONFLICT(location, world) DO UPDATE SET
                world = EXCLUDED.world,
                table_id = EXCLUDED.table_id
            """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                for (Map.Entry<Location, String> entry : locations.entrySet()) {
                    statement.setLong(1, BlockPosition.packToLong(entry.getKey()));
                    statement.setString(2, entry.getKey().getWorld().getName());
                    statement.setString(3, entry.getValue());

                    statement.addBatch();
                }

                statement.executeBatch();
            }
        });

    }

    public CompletableFuture<HashMap<Location, String>> getChestLocations() {

        return database.execute(connection -> {

            HashMap<Location, String> locations = new HashMap<>();

            try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(
                "SELECT location, world, table_id FROM chest_locations"
            )) {

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
            }

            return locations;
        });

    }

}
