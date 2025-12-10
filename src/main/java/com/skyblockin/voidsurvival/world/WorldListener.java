package com.skyblockin.voidsurvival.world;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.Database;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
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
