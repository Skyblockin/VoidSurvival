package com.skyblockin.voidsurvival.storage.database;

import com.skyblockin.voidsurvival.VoidSurvival;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class Database {

    private final HashMap<Class<? extends SQLiteTable>, SQLiteTable> tables = new HashMap<>();
    private ExecutorService executor;
    private BlockingQueue<Connection> connections = null;

    private static final int CONNECTION_POOL_SIZE = 10;
    private static final String DATABASE_URL = "jdbc:sqlite:" + VoidSurvival.getInstance().getDataFolder().getAbsolutePath() + "/playerdata.db";

    public void initialize() throws SQLException {

        if (connections != null) {
            return;
        }

        this.executor = Executors.newFixedThreadPool(CONNECTION_POOL_SIZE);

        Statement statement = DriverManager.getConnection(DATABASE_URL).createStatement();

        for (SQLiteTable table : this.tables.values()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + table.getTableName() +
                "(" + String.join(",", table.getColumns()) + ")";
            statement.executeUpdate(sql);
        }

        statement.close();

        this.connections = new LinkedBlockingQueue<>(CONNECTION_POOL_SIZE);

        for (int i = 0; i < CONNECTION_POOL_SIZE; i++) {

            Connection connection = DriverManager.getConnection(DATABASE_URL);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA busy_timeout = 10000");
            }

            this.connections.add(connection);
        }
    }

    public <T extends SQLiteTable> void addTable(Class<T> clazz) {
        try {
            T table = clazz.getDeclaredConstructor(Database.class).newInstance(this);
            tables.put(clazz, table);
        } catch (Exception ex) {
            VoidSurvival.logError("Failed to add table", ex);
        }
    }

    public <T extends SQLiteTable> T getTable(Class<T> clazz) {
        return clazz.cast(tables.get(clazz));
    }

    public void waitForAllTasksAndShutdown() {
        this.executor.shutdown();
        try {
            VoidSurvival.logInfo("Waiting for database tasks to finish...");
            if (!this.executor.awaitTermination(60, TimeUnit.SECONDS)) {
                this.executor.shutdownNow();
            }
            VoidSurvival.logInfo("Database tasks finished.");
        } catch (InterruptedException e) {
            VoidSurvival.logInfo("Shutting database down forcibly due to interrupt.");
            this.executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public Connection acquireConnection() throws InterruptedException {
        if (this.connections == null) throw new IllegalStateException("Database is not initialized");
        return this.connections.take();
    }

    public void releaseConnection(Connection connection) throws InterruptedException {
        if (this.connections == null) throw new IllegalStateException("Database is not initialized");
        this.connections.put(connection);
    }

    public CompletableFuture<Void> consume(DatabaseConsumer consumer) {
        return CompletableFuture.runAsync(() -> {
            try {
                Connection connection = acquireConnection();
                try {
                    consumer.consume(connection);
                } finally {
                    releaseConnection(connection);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new CompletionException(ex);
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        }, executor);
    }

    public <T> CompletableFuture<T> execute(DatabaseFunction<T> function) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = acquireConnection();
                try {
                    return function.execute(connection);
                } finally {
                    releaseConnection(connection);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new CompletionException(ex);
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        }, executor);
    }

}
