package com.skyblockin.voidsurvival.storage.chunk;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.math.ChunkPosition;

import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ChunkDataController {

    private static final String DATABASE_NAME = "chunkcontrollerdata.db";

    private final HashMap<ChunkPosition, ChunkData> dataMap = new HashMap<>();
    private int modifiedCount = 0;
    private Connection connection;

    public void initialize() throws SQLException {

        if (connection != null) {
            return;
        }

        String createDataTable =
            """
            CREATE TABLE IF NOT EXISTS chunk_data (
                chunk_pos INTEGER PRIMARY KEY,
                data BINARY
            )
            """;

        String url = "jdbc:sqlite:" + VoidSurvival.getInstance().getDataPath().resolve(DATABASE_NAME);
        this.connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createDataTable);
        }
    }

    public void close() throws SQLException {

        if (connection == null) {
            return;
        }

        try {
            saveChunks();
        } finally {
            connection.close();
            connection = null;
        }
    }

    private Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("ChunkDataController has not been initialized");
        }
        return connection;
    }

    public static int[] unpackPosition(int packed) {

        byte x = (byte) ((packed >> 24) & 0xFF);
        byte z = (byte) ((packed >> 16) & 0xFF);
        short y = (short) (packed & 0xFFFF);

        return new int[]{x, y, z};
    }

    public void setPlacedBy(int x, int y, int z, UUID uuid) {
        ChunkPosition pos = ChunkPosition.ofBlock(x, z);
        ChunkData data = dataMap.computeIfAbsent(pos, key -> new ChunkData());
        if (data.setPlacedBy(x, y, z, uuid)) {
            modifiedCount++;
        }
    }

    public List<BlockPosition> removeBlocksPlacedByInChunk(UUID uuid, int chunkX, int chunkZ) {

        ChunkPosition pos = ChunkPosition.of(chunkX, chunkZ);
        ChunkData data = dataMap.get(pos);

        if (data != null) {

            List<Integer> positions = data.removeBlocksPlacedBy(uuid);

            if (positions == null) {
                return new ArrayList<>();
            }

            modifiedCount++;

            ArrayList<BlockPosition> blockPositions = new ArrayList<>(positions.size());

            for (int position : positions) {
                int[] unpacked = unpackPosition(position);
                blockPositions.add(new BlockPosition(chunkX * 16 + unpacked[0], unpacked[1], chunkZ * 16 + unpacked[2]));
            }

            return blockPositions;
        }

        return new ArrayList<>();
    }

    public List<BlockPosition> getBlocksPlacedByInChunk(UUID uuid, int chunkX, int chunkZ) {

        ChunkPosition pos = ChunkPosition.of(chunkX, chunkZ);
        ChunkData data = dataMap.get(pos);

        if (data != null) {

            List<Integer> positions = data.getBlocksPlacedBy(uuid);

            if (positions == null) {
                return new ArrayList<>();
            }

            ArrayList<BlockPosition> blockPositions = new ArrayList<>(positions.size());

            for (int position : positions) {
                int[] unpacked = unpackPosition(position);
                blockPositions.add(new BlockPosition(chunkX * 16 + unpacked[0], unpacked[1], chunkZ * 16 + unpacked[2]));
            }

            return blockPositions;
        }

        return new ArrayList<>();
    }

    public UUID getPlacedBy(int x, int y, int z) {

        ChunkPosition pos = ChunkPosition.ofBlock(x, z);
        ChunkData data = dataMap.get(pos);

        return data == null ? null : data.getPlacer(x, y, z);
    }

    public boolean remove(int x, int y, int z) {

        ChunkPosition pos = ChunkPosition.ofBlock(x, z);
        ChunkData data = dataMap.get(pos);

        if (data != null) {
            if (data.remove(x, y, z)) {
                modifiedCount++;
            }
        }

        return false;
    }

    public int getModifiedChunkCount() {
        return modifiedCount;
    }

    private static final String UPSERT_CHUNK =
        """
        INSERT INTO chunk_data(chunk_pos, data) VALUES(?, ?)
        ON CONFLICT(chunk_pos) DO UPDATE SET data = excluded.data
        """;

    public void saveChunks(List<ChunkPosition> positions, boolean unload) throws SQLException {

        if (positions.isEmpty()) {
            return;
        }

        try (PreparedStatement statement = getConnection().prepareStatement(UPSERT_CHUNK)) {
            boolean shouldExecute = false;

            for (ChunkPosition pos : positions) {

                ChunkData data = unload ? dataMap.remove(pos) : dataMap.get(pos);

                if (data != null && data.wasModified()) {
                    data.setModified(false);
                    shouldExecute = true;
                    statement.setLong(1, pos.chunkKey());
                    statement.setBytes(2, compress(data.serialize()));
                    statement.addBatch();
                    modifiedCount--;
                }
            }

            if (shouldExecute) {
                statement.executeBatch();
            }
        }

    }

    public void saveChunks() throws SQLException {

        try (PreparedStatement statement = getConnection().prepareStatement(UPSERT_CHUNK)) {

            boolean shouldExecute = false;

            for (var entry : dataMap.entrySet()) {
                if (entry.getValue().wasModified()) {
                    entry.getValue().setModified(false);
                    modifiedCount--;
                    shouldExecute = true;
                    statement.setLong(1, entry.getKey().chunkKey());
                    statement.setBytes(2, compress(entry.getValue().serialize()));
                    statement.addBatch();
                }
            }

            if (shouldExecute) {
                statement.executeBatch();
            }
        }
    }

    public List<ChunkData> loadChunks(List<ChunkPosition> positions) throws SQLException, DataFormatException {

        ArrayList<ChunkData> list = new ArrayList<>(positions.size());

        try (PreparedStatement statement = getConnection().prepareStatement(
            """
                SELECT chunk_pos, data FROM chunk_data WHERE chunk_pos = ?
                """
        )) {

            for (ChunkPosition pos : positions) {

                ChunkData existing = dataMap.get(pos);

                if (existing != null) {
                    list.add(existing);
                    continue;
                }

                statement.setLong(1, pos.chunkKey());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        ChunkData data = new ChunkData(decompress(rs.getBytes(1)));
                        dataMap.put(pos, data);
                        list.add(data);
                    }
                }
            }

        }

        return list;
    }

    private byte[] decompress(byte[] input) throws DataFormatException {

        Inflater inflater = new Inflater();

        try {
            inflater.setInput(input);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            while (!inflater.finished()) {
                int decompressedSize = inflater.inflate(buffer);
                if (decompressedSize == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    throw new DataFormatException("Truncated or corrupt chunk data");
                }
                out.write(buffer, 0, decompressedSize);
            }

            return out.toByteArray();
        } finally {
            inflater.end(); // frees native zlib memory immediately
        }
    }

    private byte[] compress(byte[] input) {

        Deflater deflater = new Deflater();

        try {
            deflater.setInput(input);
            deflater.finish();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            while (!deflater.finished()) {
                int compressedSize = deflater.deflate(buffer);
                out.write(buffer, 0, compressedSize);
            }

            return out.toByteArray();
        } finally {
            deflater.end(); // frees native zlib memory immediately
        }
    }

}
