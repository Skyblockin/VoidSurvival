package com.skyblockin.voidsurvival.math;

import org.bukkit.Chunk;

import java.util.Objects;

public record ChunkPosition(int x, int z) {

    public static ChunkPosition of(int x, int z) {
        return new ChunkPosition(x, z);
    }

    public static ChunkPosition ofChunk(Chunk chunk) {
        return new ChunkPosition(chunk.getX(), chunk.getZ());
    }

    public static ChunkPosition ofLong(long packed) {

        int x = (int) (packed & 0xFFFFFFFFL);
        int z = (int) (packed >> 32);

        return new ChunkPosition(x, z);
    }

    public static ChunkPosition ofBlock(int x, int z) {
        return new ChunkPosition(x >> 4, z >> 4);
    }

    public long chunkKey() {
        return x & 0xFFFFFFFFL | (z & 0xFFFFFFFFL) << 32;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ChunkPosition(int x1, int z1) && x1 == x && z1 == z;
    }

}
