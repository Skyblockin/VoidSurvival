package com.skyblockin.voidsurvival.manager;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.loot.LootTable;
import com.skyblockin.voidsurvival.loot.LootUtil;
import com.skyblockin.voidsurvival.util.FileUtil;
import com.skyblockin.voidsurvival.util.Functions;
import com.skyblockin.voidsurvival.world.edit.WorldEditUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class WorldGenerationManager extends Manager implements Listener {

    private static final NamespacedKey STRUCTURE_KEY = new NamespacedKey(VoidSurvival.getInstance(), "has_structure");

    private double structureGenerationChancePerChunk = 0.0001;
    private final ArrayList<Clipboard> structures = new ArrayList<>();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (!event.isNewChunk() || Functions.randomChance(structureGenerationChancePerChunk)) {
            return;
        }

        if (structures.isEmpty()) {
            return;
        }

        Clipboard structure = Functions.randomChoice(structures);

        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        BlockVector3 origin = structure.getOrigin();
        BlockVector3 min = structure.getMinimumPoint();
        BlockVector3 max = structure.getMaximumPoint();

        int x = chunk.getX() * 16 + random.nextInt(0, 16);
        int z = chunk.getZ() * 16 + random.nextInt(0, 16);
        int height = max.y() - min.y();
        int y = -64 + random.nextInt(0, Math.max(0, 128 - height));

        int minX = x + (min.x() - origin.x());
        int maxX = x + (max.x() - origin.x());
        int minZ = z + (min.z() - origin.z());
        int maxZ = z + (max.z() - origin.z());

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk targetChunk = world.getChunkAt(chunkX, chunkZ);
                if (targetChunk.getPersistentDataContainer().has(STRUCTURE_KEY, PersistentDataType.BOOLEAN)) {
                    // Oops, there's already a structure here already abort, abort, abort!
                    return;
                }
            }
        }

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk targetChunk = world.getChunkAt(chunkX, chunkZ);
                // Marking the territory as claimed hehe
                targetChunk.getPersistentDataContainer().set(STRUCTURE_KEY, PersistentDataType.BOOLEAN, true);
            }
        }

        try {

            WorldEditUtil.pasteClipboardAt(structure, world, x, y, z, false);

            VoidSurvival.getInstance().runTaskLater(() -> {

                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {

                        Chunk targetChunk = world.getChunkAt(chunkX, chunkZ);

                        for (BlockState tileEntity : targetChunk.getTileEntities(false)) {
                            if (tileEntity instanceof Container chest && chest.customName() != null) {

                                String plainName = PlainTextComponentSerializer.plainText()
                                    .serialize(chest.customName());

                                LootTable table = VoidSurvival.getInstance()
                                    .getLootTableManager()
                                    .getTable(Functions.nameToTableId(plainName));

                                if (table == null) {
                                    VoidSurvival.logError("Failed to find loot table for structure chest: " + plainName);
                                    continue;
                                }

                                LootUtil.fillInventory(chest.getInventory(), 0, table);
                            }
                        }
                    }
                }

            }, 1L);

        } catch (Exception e) {
            VoidSurvival.logError("Failed to generate structure in new chunk", e);
        }
    }

    @Override
    public void reload(VoidSurvival plugin) {
        structures.clear();
        File file = plugin.getFile("structures");

        this.structureGenerationChancePerChunk = plugin.getConfig()
            .getDouble("structure-generation-chance-per-chunk", 0.0001);

        loadStructures(file);
    }

    private void loadStructures(File file) {

        if (file == null || !file.isDirectory()) {
            return;
        }

        for (File structureFile : FileUtil.listFiles(file)) {
            if (structureFile.isDirectory()) {
                loadStructures(structureFile);
            } else {
                Clipboard clipboard = WorldEditUtil.loadSchematic(structureFile);
                structures.add(clipboard);
            }
        }

    }
}
