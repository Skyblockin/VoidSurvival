package com.skyblockin.voidsurvival.entity;

import org.bukkit.Location;

public interface CustomEntity<T> {

    T spawn(Location location);

    boolean isValidForSpawn(Location location);

}
