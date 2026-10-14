package com.skyblockin.voidsurvival.storage.chunk;

import java.nio.ByteBuffer;
import java.util.*;

public class ChunkData {

    private final HashMap<UUID, HashSet<Integer>> entries;
    private boolean modified = false;

    public ChunkData(int initialCapacity) {
        int exponent = initialCapacity == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(initialCapacity - 1);
        this.entries = new HashMap<>(1 << exponent);
    }

    public ChunkData(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    public ChunkData(ByteBuffer buffer) {

        if (!buffer.hasRemaining()) {
            this.entries = new HashMap<>();
            return;
        }

        // Read the number of entries to allocate enough space in the ChunkData
        int entryCount = buffer.getInt();

        if (entryCount == 0) {
            this.entries = new HashMap<>();
            return;
        }

        int exponent = 32 - Integer.numberOfLeadingZeros(entryCount - 1);
        this.entries = new HashMap<>(1 << exponent);

        while (buffer.hasRemaining()) {

            long mostSignificantBits = buffer.getLong();
            long leastSignificantBits = buffer.getLong();

            UUID uuid = new UUID(mostSignificantBits, leastSignificantBits);

            // Read the number of coordinates to know how many coordinates to read
            int count = buffer.getInt();

            for (int i = 0; i < count; i++) {
                setPlacedBy(buffer.getInt(), uuid);
            }
        }
    }

    public ChunkData() {
        this.entries = new HashMap<>();
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean wasModified() {
        return modified;
    }

    public int getPlacedCount(UUID uuid) {
        Set<Integer> set = entries.get(uuid);
        return set == null ? 0 : set.size();
    }

    /**
     *
     * @param uuid the UUID of the person placing the block
     * @return list of removed blocks, or null if none were removed
     */
    public List<Integer> removeBlocksPlacedBy(UUID uuid) {

        HashSet<Integer> set = entries.remove(uuid);

        if (set == null || set.isEmpty()) {
            return null;
        }

        this.modified = true;

        return new ArrayList<>(set);
    }

    public List<Integer> getBlocksPlacedBy(UUID uuid) {

        HashSet<Integer> set = entries.get(uuid);

        if (set == null) {
            return null;
        }

        return new ArrayList<>(set);
    }

    /**
     *
     * @param x the X-coordinate of the placed block
     * @param y the Y-coordinate of the placed block
     * @param z the Z-coordinate of the placed block
     * @return whether the entries were modified or not
     */
    public boolean remove(int x, int y, int z) {

        x = toChunkCoordinate(x);
        z = toChunkCoordinate(z);

        int pos = packToInt(x, y, z);

        return remove(pos);
    }

    /**
     *
     * @param pos the block position packed into an int
     * @return whether the entries were modified or not
     */
    public boolean remove(int pos) {

        for (var entry : entries.entrySet()) {
            if (entry.getValue().remove(pos)) {
                this.modified = true;
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param pos the block position packed into an int
     * @param uuid the UUID of the person placing the block
     * @return whether the entries were modified or not
     */
    public boolean setPlacedBy(int pos, UUID uuid) {
        if (entries.computeIfAbsent(uuid, key -> new HashSet<>()).add(pos)) {
            this.modified = true;
            return true;
        }
        return false;
    }

    /**
     *
     * @param x the X-coordinate of the placed block
     * @param y the Y-coordinate of the placed block
     * @param z the Z-coordinate of the placed block
     * @param uuid the UUID of the person placing the block
     * @return whether the entries were modified or not
     */
    public boolean setPlacedBy(int x, int y, int z, UUID uuid) {

        x = toChunkCoordinate(x);
        z = toChunkCoordinate(z);

        int pos = packToInt(x, y, z);

        return setPlacedBy(pos, uuid);
    }

    public UUID getPlacer(int x, int y, int z) {

        x = toChunkCoordinate(x);
        z = toChunkCoordinate(z);

        int pos = packToInt(x, y, z);

        return getPlacer(pos);
    }

    public UUID getPlacer(int pos) {

        for (var entry : entries.entrySet()) {
            if (entry.getValue().contains(pos)) {
                return entry.getKey();
            }
        }

        return null;
    }

    private int packToInt(int x, int y, int z) {
        return ((x & 0xFF) << 24) |
            ((z & 0xFF) << 16) |
            ((y & 0xFFFF));
    }

    public int[] unpack(int packed) {

        byte x = (byte) ((packed >> 24) & 0xFF);
        byte z = (byte) ((packed >> 16) & 0xFF);
        short y = (short) (packed & 0xFFFF);

        return new int[]{x, y, z};
    }

    private int toChunkCoordinate(int worldCoordinate) {

        if (worldCoordinate < 0) {
            worldCoordinate = 16 + (worldCoordinate % 16);
        }

        return worldCoordinate % 16;
    }

    public byte[] serialize() {

        int size = 4;

        for (var entry : entries.entrySet()) {
            size += 20 + (entry.getValue().size() * 4);
        }

        ByteBuffer buffer = ByteBuffer.wrap(new byte[size]);

        buffer.putInt(entries.size());

        for (var entry : entries.entrySet()) {

            UUID uuid = entry.getKey();

            // UUID bytes
            buffer.putLong(uuid.getMostSignificantBits());
            buffer.putLong(uuid.getLeastSignificantBits());

            // Position list size
            buffer.putInt(entry.getValue().size());

            // Positions !!!!
            for (int pos : entry.getValue()) {
                buffer.putInt(pos);
            }
        }

        return buffer.array();
    }

}
