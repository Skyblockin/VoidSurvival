package com.skyblockin.storage;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    public static final ConcurrentHashMap<UUID, PlayerData> PLAYER_DATA_MAP = new ConcurrentHashMap<>();

    public static PlayerData of(Player player) {
        return PLAYER_DATA_MAP.get(player.getUniqueId());
    }

    public static void put(PlayerData data) {
        PLAYER_DATA_MAP.put(data.uuid, data);
    }

    public final transient UUID uuid;
    public transient boolean generatingIsland = false;
    public int kills = 0;
    public int killStreak = 0;
    public HomeMap homes = new HomeMap();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public void setHome(String name, Location location) {
        this.homes.put(name, location);
    }

    public Location getHome(String name) {
        return homes.get(name);
    }

    public Set<String> getHomeNames() {
        return homes.keySet();
    }



}
