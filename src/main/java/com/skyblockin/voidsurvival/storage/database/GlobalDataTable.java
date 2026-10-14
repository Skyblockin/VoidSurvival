package com.skyblockin.voidsurvival.storage.database;

import com.skyblockin.voidsurvival.VoidSurvival;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class GlobalDataTable extends SQLiteTable {

    public GlobalDataTable(Database database) {
        super(database, "global_data",
            "id INTEGER PRIMARY KEY CHECK (id = 0)",
            "chunk_count INTEGER"
        );
    }

    public CompletableFuture<Void> saveGlobalData() {

        String sql = """
        INSERT INTO global_data(id, chunk_count) VALUES(0, ?)
        ON CONFLICT(id) DO UPDATE
        SET chunk_count = EXCLUDED.chunk_count
        """;

        return database.consume(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, VoidSurvival.getInstance().getWorldListener().getGeneratedChunkCount());
                statement.executeUpdate();
            }
        });

    }

    public CompletableFuture<Long> getChunkCount() {

        return database.execute(connection -> {
            try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(
                "SELECT chunk_count FROM global_data WHERE id = 0"
            )) {

                if (!rs.next()) {
                    return 0L;
                }

                return rs.getLong(1);
            }
        });

    }
}
