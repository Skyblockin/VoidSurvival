package com.skyblockin.voidsurvival.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
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
    private final HashMap<String, Long> chestCooldowns = new HashMap<>();
    public boolean hasGeneratedIsland = false;
    public String lastKnownUserName;
    public int kills = 0;
    public int killStreak = 0;
    public HomeMap homes = new HomeMap();
    public HashMap<Long, Long> lastChestOpenTimes = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public void setLastChestOpenTime(Block block, long time) {
        lastChestOpenTimes.put(BlockPosition.ofBlock(block).asLong(), time);
    }

    public Long getLastChestOpenTime(Block block) {
        return lastChestOpenTimes.get(BlockPosition.ofBlock(block).asLong());
    }

    public void setHome(String name, Location location) {
        this.homes.put(name, location);
    }

    /**
     * Gets the home location of the player.
     * This method will also attempt to find a solid block for the player
     * to stand on if the current location is not supported by a solid block.
     * @param name The name of the home
     * @return The home location of the player, or null if the player has no home
     */
    public Location getHome(String name) throws InvalidHomeException {

        Location home = homes.get(name);

        if (home == null) {
            return null;
        }

        Block block = home.getBlock();

        if (block.getRelative(BlockFace.UP).isSuffocating()) {
            throw new InvalidHomeException("A home was previously set, but the position is no longer valid.");
        }

        while (!block.getRelative(BlockFace.DOWN).isSolid() && block.getY() > block.getWorld().getMinHeight()) {
            block = block.getRelative(BlockFace.DOWN);
        }

        if (block.getRelative(BlockFace.DOWN).isSolid()) {
            homes.put(name, block.getLocation());
            return block.getLocation();
        } else {
            throw new InvalidHomeException("A home was previously set, but the position is no longer valid.");
        }
    }

    @JsonIgnore
    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    public void putCooldown(String id, long cooldown) {
        chestCooldowns.putIfAbsent(id, System.currentTimeMillis() + cooldown);
    }

    public long getChestCooldown(String id) {
        return chestCooldowns.getOrDefault(id, 0L);
    }

    public void resetCooldowns() {
        this.chestCooldowns.clear();
    }

}
