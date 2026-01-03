package com.skyblockin.voidsurvival.util;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.gui.Gui;
import com.skyblockin.voidsurvival.gui.GuiItem;
import com.skyblockin.voidsurvival.message.MessageKeys;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.math.Position;
import com.skyblockin.voidsurvival.world.CampfireManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
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

    public static void openWarpMenu(Player player) {

        PlayerData data = PlayerData.of(player);

        if (data.campfires.isEmpty()) {
            player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_NONE_UNLOCKED));
            return;
        }

        // Evil
        int guiRows = Math.clamp(data.campfires.size() / 9 + (data.campfires.size() % 9 == 0 ? 2 : 3), 3, 6);

        Gui gui = Gui.create(player.getUniqueId(), TextUtil.color("<!i><#ff8c00>Campfire Warps"), guiRows * 9);

        for (int i = 0; i < 9; i++) {
            gui.addItem(ItemType.GRAY_STAINED_GLASS_PANE.createItemStack(), event -> {});
        }

        for (var entry : data.campfires.entrySet()) {

            String campfireName = entry.getKey();
            Location campfireLocation = entry.getValue();

            gui.addItem(ItemType.CAMPFIRE.createItemStack(meta -> {
                meta.customName(TextUtil.color("<!i><#ffa500>%s", campfireName));
            }), event -> {

                Pair<String, Position> campfireData = CampfireManager.getCampfireData(campfireLocation);
                Player clicker = (Player) event.getWhoClicked();

                if (campfireData == null) {
                    data.campfires.remove(campfireName);
                    clicker.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_INVALID, campfireName));
                    return;
                }

                if (VoidSurvival.getInstance().getCombatTracker().isCombatTagged(clicker)) {
                    clicker.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_NO_COMBAT_WARP));
                    return;
                }

                String warpName = campfireData.left();
                Location warpLocation = campfireData.right().toLocation(campfireLocation.getWorld());

                warpToCampfire(warpName, warpLocation, clicker);
            });
        }

        for (int i = (guiRows - 1) * 9; i < guiRows * 9; i++) {
            gui.setItem(i, new GuiItem(ItemType.GRAY_STAINED_GLASS_PANE.createItemStack(), event -> {}));
        }

        gui.open();
    }

    public static void warpToCampfire(String campfireName, Location campfireLocation, Player player) {

        if (campfireLocation.distance(player.getLocation()) > 5) {

            player.closeInventory();

            if (!player.hasPermission("voidsurvival.instantwarp")) {

                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 10 * 20, 0));
                player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.0f);

                VoidSurvival.getInstance().runTaskLater(() -> {
                    player.teleportAsync(campfireLocation);
                    player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_WARPED, campfireName));
                }, 40);

            } else {
                player.teleportAsync(campfireLocation);
                player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_WARPED, campfireName));
            }

        } else {
            player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_ALREADY_AT_CAMPFIRE));
        }

    }

}
