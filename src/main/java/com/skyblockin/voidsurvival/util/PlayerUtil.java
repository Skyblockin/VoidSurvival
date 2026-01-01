package com.skyblockin.voidsurvival.util;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.gui.Gui;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.storage.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
            player.sendRichMessage("<red>You have not unlocked any warps!");
            return;
        }

        Gui gui = Gui.create(player.getUniqueId(), TextUtil.color("<!i><#ffa500>Campfire Warps"), 27);

        data.campfires.forEach((name, location) -> {
            gui.addItem(ItemType.CAMPFIRE.createItemStack(meta -> {
                meta.customName(TextUtil.color("<!i><#ffa500>%s", name));
            }), event -> {

                Pair<String, Position> campfireData = VoidSurvival.getInstance().getPlayerBlockManager().getCampfireData(location);

                if (campfireData == null) {
                    return;
                }

                Player clicker = (Player) event.getWhoClicked();

                if (VoidSurvival.getInstance().getCombatTracker().isCombatTagged(clicker)) {
                    clicker.sendRichMessage("<#fc0202>You can't warp in combat!");
                    return;
                }

                Location campfireLocation = campfireData.right().toLocation(location.getWorld());

                if (campfireLocation.distance(player.getLocation()) > 5) {

                    clicker.closeInventory();

                    if (!clicker.hasPermission("voidsurvival.instantwarp")) {

                        clicker.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
                        clicker.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 10 * 20, 0));
                        clicker.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.0f);

                        VoidSurvival.getInstance().runTaskLater(() -> {
                            clicker.teleportAsync(campfireLocation);
                            clicker.sendRichMessage("<#05fcbe>You have been warped to <#ffa500>" + campfireData.left());
                        }, 40);

                    } else {
                        clicker.teleportAsync(campfireLocation);
                        clicker.sendRichMessage("<#05fcbe>You have been warped to <#ffa500>" + campfireData.left());
                    }

                } else {
                    clicker.sendRichMessage("<#fc0202>You are already at this campfire!");
                }

            });
        });

        gui.open();
    }

}
