package com.skyblockin.voidsurvival.world;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.constants.ItemIds;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

public class WorldListener implements Listener {

    private long generatedChunkCount = 0;

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            this.generatedChunkCount++;
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        ItemStack item = event.getItemInHand();

        if (Accessors.CAN_PLACE.equals(item, false)) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockClickEvenIfCancelled(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign sign) {

            Player player = event.getPlayer();
            String command = Accessors.SIGN_COMMAND.read(sign);

            if (command != null) {

                if (command.startsWith("message:")) {
                    player.sendMessage(TextUtil.color(command.substring("message:".length()), player.getName()));
                } else if (command.startsWith("console:")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format(command.substring("command:".length()), player.getName()));
                } else {
                    event.getPlayer().performCommand(String.format(command, player.getName()));
                }

                event.setCancelled(true);
            }
        }
    }

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

    public long getGeneratedChunkCount() {
        return this.generatedChunkCount;
    }

    public void loadGeneratedChunkCount() {

        this.generatedChunkCount = Database.getChunkCount();

        if (this.generatedChunkCount != -1) {
            VoidSurvival.logInfo("%d chunks have been generated and tracked so far", this.generatedChunkCount);
        } else {
            VoidSurvival.logError("Something went wrong while loading the tracked chunks. The counter has been set to 0");
            this.generatedChunkCount = 0;
        }

    }


}
