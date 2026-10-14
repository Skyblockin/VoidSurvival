package com.skyblockin.voidsurvival.item.listeners;

import com.skyblockin.voidsurvival.item.ItemIds;
import com.skyblockin.voidsurvival.storage.Accessors;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GrassSeedsListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockClick(PlayerInteractEvent event) {

        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();

        if (item == null || block == null) {
            return;
        }

        if (Accessors.ITEM_ID.equals(item, ItemIds.GRASS_SEEDS)) {
            if (block.getType() == Material.DIRT && block.getRelative(BlockFace.UP).isEmpty()) {
                block.setType(Material.GRASS_BLOCK);
                if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                    item.subtract();
                }
            } else {
                event.setCancelled(true);
            }
        }

    }

}
