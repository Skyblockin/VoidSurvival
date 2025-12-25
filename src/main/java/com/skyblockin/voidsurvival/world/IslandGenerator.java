package com.skyblockin.voidsurvival.world;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.util.Functions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class IslandGenerator {

    private final List<Clipboard> clipboards;

    public IslandGenerator() {
        this.clipboards = new ArrayList<>();
        reload();
    }

    public void reload() {

        this.clipboards.clear();

        for (File schematic : VoidSurvival.getInstance().getIslandFiles()) {

            Clipboard clipboard = WorldEditUtil.loadSchematic(schematic);

            if (clipboard != null) {
                this.clipboards.add(clipboard);
            } else {
                VoidSurvival.logError("Failed to load schematic from file '%s'! Please fix!!!!", schematic.getName());
            }
        }

        VoidSurvival.logInfo("Loaded island generator with %d schematics", this.clipboards.size());
    }

    public CompletableFuture<Chunk> findChunkForIsland(World world) {

        return CompletableFuture.supplyAsync(() -> {

            int chunkRange = getIslandGenerationRange() / 16;

            ThreadLocalRandom random = ThreadLocalRandom.current();

            int x, z;
            Chunk chunk;

            do {
                x = random.nextInt(-chunkRange, chunkRange);
                z = random.nextInt(-chunkRange, chunkRange);
                chunk = world.getChunkAt(x, z, false);
            } while (chunk.isGenerated() && chunk.getInhabitedTime() > (20 * 30));

            return chunk;
        });
    }

    public Location generateIsland(Chunk chunk) {

        try {

            Clipboard clipboard = Functions.randomChoice(this.clipboards);

            int x1 = chunk.getX() << 4;
            int y = 64;
            int z1 = chunk.getZ() << 4;

            WorldEditUtil.pasteClipboardAt(clipboard, chunk.getWorld(), x1, y, z1, true);

            return new Location(chunk.getWorld(), x1, y + 1, z1);

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to generate island at chunk %d, %d, is the schematic configured correctly?", chunk.getX(), chunk.getZ());
            return null;
        }

    }

    // A range of 5000 means a 10000 by 10000 world, containing 100 1000x1000 plots.
    // If more than 50% of this area has been used, the range will be doubled.
    // Example return values of this function
    // 50 islands -> 5000
    // 200 islands -> 10000
    // 400 islands -> 20000
    private int getIslandGenerationRange() {

        int count = Database.getIslandCount();
        int range = (int) Math.sqrt(count * 500000) + 100;

        VoidSurvival.logDebug("Generated island range %d with %d islands", range, count);

        return range;
    }

}
