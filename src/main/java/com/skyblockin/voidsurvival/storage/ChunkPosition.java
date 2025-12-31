package com.skyblockin.voidsurvival.storage;

import org.bukkit.Chunk;

import java.util.Objects;

public record ChunkPosition(int x, int z) {

    public static ChunkPosition ofChunk(Chunk chunk) {
        return new ChunkPosition(chunk.getX(), chunk.getZ());
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
