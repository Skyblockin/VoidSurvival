package com.skyblockin.voidsurvival.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.social.Friend;
import com.skyblockin.voidsurvival.social.FriendList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    public static final ConcurrentHashMap<UUID, PlayerData> PLAYER_DATA_MAP = new ConcurrentHashMap<>();

    public static PlayerData getByNameorUuid(String text) throws PlayerDataException {

        // By name
        if (text.length() <= 16) {

            Player player = Bukkit.getPlayer(text);

            // Return current data for online players
            if (player != null) {
                return PLAYER_DATA_MAP.get(player.getUniqueId());
            } else {
                return Database.loadPlayerByName(text);
            }

        // By uuid
        } else {

            UUID uuid = UUID.fromString(text);
            PlayerData data = PLAYER_DATA_MAP.get(uuid);

            if (data != null) {
                return data;
            // Maybe the player is not online, let's try the database
            } else {
                return Database.loadPlayer(uuid);
            }

        }

    }

    public static PlayerData of(Player player) {
        return PLAYER_DATA_MAP.get(player.getUniqueId());
    }

    public static void put(PlayerData data) {
        PLAYER_DATA_MAP.put(data.uuid, data);
    }

    public static Set<PlayerData> getTop(int limit, Comparator<PlayerData> comparator) {
        return new HashSet<>(PLAYER_DATA_MAP.values().stream().sorted(comparator).limit(limit).toList());
    }

    public final transient UUID uuid;
    public transient boolean generatingIsland = false;
    public boolean hasGeneratedIsland = false;
    public String lastKnownUserName;
    public int maxCampfires = 9;
    public int kills = 0;
    public int killStreak = 0;
    public int chestsLooted = 0;
    public NamedLocationMap campfires = new NamedLocationMap();
    public NamedLocationMap homes = new NamedLocationMap();
    public HashMap<Long, Long> lastChestOpenTimes = new HashMap<>();
    public FriendList friendList = new FriendList();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public void setLastChestOpenTime(Block block, long time) {
        lastChestOpenTimes.put(BlockPosition.ofBlock(block).asLong(), time);
    }

    public long getLastChestOpenTime(Block block) {
        return lastChestOpenTimes.getOrDefault(BlockPosition.ofBlock(block).asLong(), 0L);
    }

    public void setHome(String name, Location location) {
        this.homes.put(name, location);
    }

    public void unlockCampfire(String name, Location location) {
        campfires.put(name, location);
    }

    public boolean hasUnlockedCampfire(String name) {
        return campfires.containsKey(name);
    }

    /**
     * Gets the home location of the player.
     * This method will also attempt to find a solid block for the player
     * to stand on if the current location is not supported by a solid block.
     * @param name The name of the home
     * @return The home location of the player, or null if the player has no home
     */
    public Location getHome(String name) {
        return homes.get(name);
    }

    @JsonIgnore
    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    @JsonIgnore
    public ArrayList<Friend> getMutualFriends(PlayerData other) {

        ArrayList<Friend> mutuals = new ArrayList<>();

        other.friendList.forEach(friend -> {
            if (friendList.contains(friend)) {
                mutuals.add(friend);
            }
        });

        return mutuals;
    }

    @JsonIgnore
    public FriendList getFriends() {
        return friendList;
    }

    public Friend asFriend() {
        return new Friend(uuid.toString(), lastKnownUserName);
    }

    public void resetCooldowns() {
        this.lastChestOpenTimes.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlayerData data && Objects.equals(uuid, data.uuid);
    }
}
