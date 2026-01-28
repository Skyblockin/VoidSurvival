package com.skyblockin.voidsurvival.math;

import net.minecraft.core.BlockPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public record BlockPosition(int x, int y, int z) {

    public static BlockPosition ofBlock(Block block) {
        return new BlockPosition(block.getX(), block.getY(), block.getZ());
    }

    public static BlockPosition ofLocation(Location location) {
        return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static long packToLong(Location location) {
        return ((location.getBlockX() & 0x03FFFFFFL) << 38) | ((location.getBlockY() &  0x00000FFFL)) | ((location.getBlockZ() & 0x03FFFFFFL) << 12);
    }

    public static BlockPosition fromLong(long packed) {
        return new BlockPosition((int) (packed >> 38), (int) ((packed << 52) >> 52), (int) ((packed << 26) >> 38));
    }

    public long asLong() {
        return ((x & 0x03FFFFFFL) << 38) | ((y &  0x00000FFFL)) | ((z & 0x03FFFFFFL) << 12);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public BlockPosition add(int x, int y, int z) {
        return new BlockPosition(this.x + x, this.y + y, this.z + z);
    }

    public BlockPosition add(BlockPosition other) {
        return new BlockPosition(other.x() + x, other.y() + y, other.z() + z);
    }

    public Position add(Position other) {
        return new Position(other.x() + x, other.y() + y, other.z() + z, other.yaw(), other.pitch());
    }

    public BlockPosition withY(int y) {
        return new BlockPosition(this.x, y, this.z);
    }

}
