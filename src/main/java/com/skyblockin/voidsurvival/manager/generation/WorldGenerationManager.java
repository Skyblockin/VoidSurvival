package com.skyblockin.voidsurvival.manager.generation;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.event.CustomBlockMineEvent;
import com.skyblockin.voidsurvival.loot.LootTable;
import com.skyblockin.voidsurvival.loot.LootUtil;
import com.skyblockin.voidsurvival.manager.Manager;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.storage.pdc.CustomPersistentDataType;
import com.skyblockin.voidsurvival.util.FileUtil;
import com.skyblockin.voidsurvival.util.Functions;
import com.skyblockin.voidsurvival.world.edit.WorldEditUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WorldGenerationManager extends Manager implements Listener {

    private static final NamespacedKey STRUCTURE_KEY = new NamespacedKey(VoidSurvival.getInstance(), "structure");

    private double structureGenerationChancePerChunk = 0.0001;
    private final HashMap<String, CachedStructure> structures = new HashMap<>();
    private final HashMap<String, StructureData> structureData = new HashMap<>();

    public WorldGenerationManager(VoidSurvival plugin) {
        plugin.runTaskTimer(this::tickEntitySpawns, 0L, 20L);
    }

    public void tickEntitySpawns() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            tickEntitySpawnsForPlayer(player);
        }
    }

    private void tickEntitySpawnsForPlayer(Player player) {

        int randomOffsetX = ThreadLocalRandom.current().nextInt(24, 48);
        int randomOffsetZ = ThreadLocalRandom.current().nextInt(24, 48);

        Location playerLocation = player.getLocation();

        Block block = player.getWorld().getHighestBlockAt(
            playerLocation.getBlockX() + randomOffsetX,
            playerLocation.getBlockZ() + randomOffsetZ
        );

        if (!block.getChunk().isLoaded()) {
            return;
        }

        Structure structure = block.getChunk().getPersistentDataContainer().get(STRUCTURE_KEY, CustomPersistentDataType.STRUCTURE);

        if (structure == null) {
            return;
        }

        StructureData data = structureData.get(structure.name);

        CustomEntityOrVanilla entity = data.getEntity();

        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(structure.getBoundingBox(),
            e -> e.hasMetadata("NPC") || e instanceof Monster
        );

        if (nearbyEntities.size() >= data.mobCap) {
            return;
        }

        entity.spawnAt(block.getLocation().add(0.5, 1, 0.5));
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (!event.isNewChunk() || !Functions.randomChance(structureGenerationChancePerChunk)) {
            return;
        }

        if (structures.isEmpty()) {
            return;
        }

        Map.Entry<String, CachedStructure> entry = Functions.randomChoice(structures);
        if (entry == null) {
            return;
        }

        String name = entry.getKey();
        CachedStructure cached = entry.getValue();
        Clipboard structure = cached.clipboard();
        int[][] columnCounts = cached.columnCounts();

        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        // Only spawn structures in the normal world for now
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        BlockVector3 origin = structure.getOrigin();
        BlockVector3 min = structure.getMinimumPoint();
        BlockVector3 max = structure.getMaximumPoint();

        int x = chunk.getX() * 16 + random.nextInt(0, 16);
        int z = chunk.getZ() * 16 + random.nextInt(0, 16);
        int height = max.y() - min.y();
        int y = -64 + random.nextInt(0, Math.max(1, 128 - height));

        int offsetX = x - origin.x();
        int offsetY = y - origin.y();
        int offsetZ = z - origin.z();

        int minX = min.x() + offsetX;
        int maxX = max.x() + offsetX;
        int minY = min.y() + offsetY;
        int maxY = max.y() + offsetY;
        int minZ = min.z() + offsetZ;
        int maxZ = max.z() + offsetZ;

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        if (!canFitStructure(world, minChunkX, minChunkZ, maxChunkX, maxChunkZ)) {
            // Oops, there's already a structure here already abort, abort, abort!
            return;
        }

        BlockPosition minPos = new BlockPosition(minX, minY, minZ);
        BlockPosition maxPos = new BlockPosition(maxX, maxY, maxZ);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            int chunkMinX = chunkX << 4;
            int overlapMinX = Math.max(minX, chunkMinX);
            int overlapMaxX = Math.min(maxX, chunkMinX + 15);

            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                int chunkMinZ = chunkZ << 4;
                int overlapMinZ = Math.max(minZ, chunkMinZ);
                int overlapMaxZ = Math.min(maxZ, chunkMinZ + 15);

                // Sum cached column counts within this chunk's overlap
                int blocks = 0;
                for (int worldX = overlapMinX; worldX <= overlapMaxX; worldX++) {
                    int arrayX = worldX - minX;
                    for (int worldZ = overlapMinZ; worldZ <= overlapMaxZ; worldZ++) {
                        int arrayZ = worldZ - minZ;
                        blocks += columnCounts[arrayX][arrayZ];
                    }
                }

                Chunk targetChunk = world.getChunkAt(chunkX, chunkZ);
                // Marking the territory as claimed hehe
                Structure structureData = new Structure(name, minPos, maxPos, blocks);
                targetChunk.getPersistentDataContainer().set(STRUCTURE_KEY, CustomPersistentDataType.STRUCTURE, structureData);
            }
        }

        try {
            WorldEditUtil.pasteClipboardAt(structure, world, x, y, z, false);

            VoidSurvival.getInstance().runTaskLater(() -> {
                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                        fillChunkChests(world.getChunkAt(chunkX, chunkZ));
                    }
                }
            }, 1L);

        } catch (Exception e) {
            VoidSurvival.logError("Failed to generate structure in new chunk", e);
        }
    }

    private void fillChunkChests(Chunk chunk) {
        for (BlockState tileEntity : chunk.getTileEntities(false)) {
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

    private boolean canFitStructure(World world, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk targetChunk = world.getChunkAt(chunkX, chunkZ);
                if (targetChunk.getPersistentDataContainer().has(STRUCTURE_KEY, CustomPersistentDataType.STRUCTURE)) {
                    // Oops, there's already a structure here already abort, abort, abort!
                    return false;
                }
            }
        }

        return true;
    }

    @EventHandler
    public void onBlockBreak(CustomBlockMineEvent event) {

        Block block = event.getBlock();
        Chunk chunk = block.getChunk();

        PersistentDataContainer pdc = chunk.getPersistentDataContainer();

        Structure structure = pdc.get(STRUCTURE_KEY, CustomPersistentDataType.STRUCTURE);

        if (structure == null) {
            return;
        }

        if (!structure.getBoundingBox().contains(block.getX(), block.getY(), block.getZ())) {
            return;
        }

        if (--structure.blocks <= 0) {
            pdc.remove(STRUCTURE_KEY);
        } else {
            pdc.set(STRUCTURE_KEY, CustomPersistentDataType.STRUCTURE, structure);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        Block block = event.getBlock();
        Chunk chunk = block.getChunk();

        PersistentDataContainer pdc = chunk.getPersistentDataContainer();

        Structure structure = pdc.get(STRUCTURE_KEY, CustomPersistentDataType.STRUCTURE);

        if (structure == null) {
            return;
        }

        if (!structure.getBoundingBox().contains(block.getX(), block.getY(), block.getZ())) {
            return;
        }

        if (--structure.blocks <= 0) {
            pdc.remove(STRUCTURE_KEY);
        } else {
            pdc.set(STRUCTURE_KEY, CustomPersistentDataType.STRUCTURE, structure);
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

                String name = structureFile.getName();
                int index = name.lastIndexOf(".");
                if (index > 0) {
                    name = name.substring(0, index);
                }

                structures.put(name, CachedStructure.fromClipboard(clipboard));
            }
        }

    }
}
