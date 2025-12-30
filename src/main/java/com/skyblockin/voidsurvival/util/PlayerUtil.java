package com.skyblockin.voidsurvival.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerUtil {

    public static Player getOnlinePlayer(UUID uuid) {

        Player player = Bukkit.getPlayer(uuid);

        if (player != null && player.isVisibleByDefault()) {
            return player;
        }

        return null;
    }

    public static Player getOnlinePlayer(String name) {

        Player player = Bukkit.getPlayer(name);

        if (player != null && player.isVisibleByDefault()) {
            return player;
        }

        return null;
    }

}
