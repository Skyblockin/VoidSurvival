package com.skyblockin.voidsurvival.storage;

import org.bukkit.Location;
import org.bukkit.World;

public record Position(double x, double y, double z, float yaw, float pitch) {

    public static Position ofLocation(Location location) {
        return new Position(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

}
