package com.skyblockin.voidsurvival.math;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;

public record Position(double x, double y, double z, float yaw, float pitch) {

    public static Position ofLocation(Location location) {
        return new Position(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Position add(double x, double y, double z) {
        return new Position(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch);
    }

    public BlockPosition asBlockPosition() {
        return new BlockPosition(
            NumberConversions.floor(x),
            NumberConversions.floor(y),
            NumberConversions.floor(z)
        );
    }

}
