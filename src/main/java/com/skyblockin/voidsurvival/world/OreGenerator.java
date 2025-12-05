package com.skyblockin.voidsurvival.world;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mojang.datafixers.util.Pair;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.region.Flags;
import com.skyblockin.voidsurvival.storage.BlockPosition;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class OreGenerator implements Listener {

    private final HashMap<Location, BlockType> missingBlocks = new HashMap<>();
    private final HashMap<BlockType, Pair<Integer, Integer>> regenerationTimes = new HashMap<>();

    public OreGenerator() {
        reload();
    }

    public void reload() {

        ConfigurationSection section = VoidSurvival.getInstance().getConfig().getConfigurationSection("ore-generation-times");

        if (section == null) {
            VoidSurvival.logInfo("config.yml is missing section ore-generation-times, ore generation was not loaded.");
            return;
        }

        this.regenerationTimes.clear();

        for (String key : section.getKeys(false)) {

            BlockType blockType = Registry.BLOCK.get(NamespacedKey.minecraft(key));

            int minTime = section.getInt(key + ".min-time", 1);
            int maxTime = section.getInt(key + ".max-time", 1);

            this.regenerationTimes.put(blockType, new Pair<>(minTime, maxTime));
        }

        VoidSurvival.logInfo("Loaded ore generation with %d entries", this.regenerationTimes.size());
    }

    public void writeMissingBlocksToFile() throws IOException {

        if (this.missingBlocks.isEmpty()) {
            // Nothing to write
            return;
        }

        File file = new File(VoidSurvival.getInstance().getDataFolder(), "missing_blocks.json");

        if (!file.exists()) {
            if (file.getParentFile().mkdirs()) {
                VoidSurvival.logInfo("The config folder was missing, so it was created.");
            } else if (!file.getParentFile().exists()) {
                VoidSurvival.logError("The config folder was missing, and it could not be created. Is the plugin folder read-only?");
            }
        }

        HashMap<String, HashMap<String, ArrayList<BlockPosition>>> blockDataMap = new HashMap<>();

        this.missingBlocks.forEach((location, blockType) -> {
            blockDataMap.computeIfAbsent(location.getWorld().getName(), key -> new HashMap<>())
                .computeIfAbsent(blockType.getKey().toString(), key -> new ArrayList<>())
                .add(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        });

        Json.writeToFile(file, blockDataMap);
    }

    public void loadMissingBlocksFromFile() {

        try {

            File file = new File(VoidSurvival.getInstance().getDataFolder(), "missing_blocks.json");

            if (!file.exists()) {
                return;
            }

            JsonNode node = Json.readFromFile(file);

            node.fields().forEachRemaining(entry -> {

                World world = Bukkit.getWorld(entry.getKey());

                if (world == null) {
                    VoidSurvival.logError("Could not find world with name %s", entry.getKey());
                    return;
                }

                entry.getValue().fields().forEachRemaining(blockTypeEntry -> {

                    NamespacedKey key = NamespacedKey.fromString(blockTypeEntry.getKey());

                    if (key == null) {
                        VoidSurvival.logError("Skipped block type with key %s, is it a valid block type?", blockTypeEntry.getKey());
                        return;
                    }

                    BlockType type = Registry.BLOCK.get(key);
                    ArrayNode array = (ArrayNode) blockTypeEntry.getValue();

                    for (JsonNode blockPosition : array) {

                        int x = blockPosition.get("x").asInt();
                        int y = blockPosition.get("y").asInt();
                        int z = blockPosition.get("z").asInt();

                        Pair<Integer, Integer> regenerationTime = regenerationTimes.get(type);

                        if (regenerationTime != null && type != null) {
                            VoidSurvival.getInstance()
                                .runTaskLater(() -> {
                                        world.getBlockAt(x, y, z).setBlockData(type.createBlockData());
                                        missingBlocks.remove(new Location(world, x, y, z));
                                    },
                                    ThreadLocalRandom.current().nextInt(regenerationTime.getFirst(), regenerationTime.getSecond() + 1)
                                );

                            missingBlocks.put(new Location(world, x, y, z), type);
                        }
                    }
                });
            });

        } catch (Exception e) {
            VoidSurvival.logError("Failed to load missing blocks from file", e);
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (Flags.REGENERATE_BLOCKS.test(player, block)) {

            BlockData data = block.getBlockData();

            Pair<Integer, Integer> regenerationTime = regenerationTimes.get(data.getMaterial().asBlockType());

            if (regenerationTime != null) {
                VoidSurvival.getInstance()
                    .runTaskLater(() -> {
                            block.setBlockData(data);
                            missingBlocks.remove(block.getLocation());
                        },
                        ThreadLocalRandom.current().nextInt(regenerationTime.getFirst(), regenerationTime.getSecond() + 1)
                    );

                missingBlocks.put(block.getLocation(), block.getType().asBlockType());
            } else if (!player.isOp()) {
                event.setCancelled(true);
            }
        }
    }



}
