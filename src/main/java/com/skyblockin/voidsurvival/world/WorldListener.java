package com.skyblockin.voidsurvival.world;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.database.Database;
import com.skyblockin.voidsurvival.storage.database.GlobalDataTable;
import com.skyblockin.voidsurvival.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ExecutionException;

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

    public long getGeneratedChunkCount() {
        return this.generatedChunkCount;
    }

    public void loadGeneratedChunkCount() {

        try {
            this.generatedChunkCount = VoidSurvival.getTable(GlobalDataTable.class).getChunkCount().get();
            VoidSurvival.logInfo("%d chunks have been generated and tracked so far", this.generatedChunkCount);
        } catch (Exception ex) {
            VoidSurvival.logError("Something went wrong while loading the tracked chunks. The counter has been set to 0", ex);
            this.generatedChunkCount = 0;
        }

    }


}
