package com.skyblockin.voidsurvival.world;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.Database;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class WorldListener implements Listener {

    private long generatedChunkCount = 0;

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            this.generatedChunkCount++;
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
