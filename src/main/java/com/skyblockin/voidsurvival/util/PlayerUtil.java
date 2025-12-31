package com.skyblockin.voidsurvival.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

    public static void giveItems(Player player, Collection<ItemStack> items) {

        HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(items.toArray(new ItemStack[0]));

        leftOvers.values().forEach(leftOverItem -> {
            player.getWorld().dropItem(player.getLocation(), leftOverItem);
        });

    }

}
