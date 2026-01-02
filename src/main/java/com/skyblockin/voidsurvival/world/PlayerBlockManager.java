package com.skyblockin.voidsurvival.world;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.math.ChunkPosition;
import com.skyblockin.voidsurvival.math.Position;
import com.skyblockin.voidsurvival.message.MessageKeys;
import com.skyblockin.voidsurvival.region.Flags;
import com.skyblockin.voidsurvival.storage.*;
import com.skyblockin.voidsurvival.util.CustomTimeUnit;
import com.skyblockin.voidsurvival.util.Functions;
import com.skyblockin.voidsurvival.util.PlayerUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.Campfire;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("UnstableApiUsage")
public class PlayerBlockManager implements Listener {

    private final HashMap<BlockType, Pair<Integer, Integer>> regenerationTimes = new HashMap<>();
    private final HashMap<UUID, HashMap<ChunkPosition, HashMap<BlockPosition, Long>>> playerBlockChanges = new HashMap<>();
    private final HashMap<BlockPosition, Pair<String, Position>> campfireCache = new HashMap<>();

    public PlayerBlockManager() {

        VoidSurvival.getInstance().runTaskTimer(() -> {

            for (var playerEntry : playerBlockChanges.entrySet()) {
                
                UUID uuid = playerEntry.getKey();
                HashMap<ChunkPosition, HashMap<BlockPosition, Long>> chunkChanges = playerEntry.getValue();                
                
                for (var chunkEntry : chunkChanges.entrySet()) {

                    List<BlockPosition> blocksToRemove = new ArrayList<>();
                    HashMap<BlockPosition, Long> blockChanges = chunkEntry.getValue();

                    blockChanges.replaceAll((position, ticks) -> {

                        if (ticks == null) {
                            return null;
                        }

                        long remainingTicks = ticks - 1;

                        if (remainingTicks <= 0) {
                            blocksToRemove.add(position);
                        }

                        return remainingTicks;
                    });

                    for (BlockPosition position : blocksToRemove) {

                        // Make sure to reset the block state
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            Block block = position.toLocation(player.getWorld()).getBlock();
                            player.sendBlockChange(block.getLocation(), block.getBlockData());
                        }

                        blockChanges.remove(position);
                    }
                    
                }
                
            }

        }, 0, 1);

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

            int minTime = (int) Functions.getTimeFromYaml(section, key + ".min-time", 1000, CustomTimeUnit.TICKS);
            int maxTime = (int) Functions.getTimeFromYaml(section, key + ".max-time", 1000, CustomTimeUnit.TICKS);

            this.regenerationTimes.put(blockType, new Pair<>(minTime, maxTime));
        }

        VoidSurvival.logInfo("Loaded ore generation with %d entries", this.regenerationTimes.size());
    }

    private boolean isLocationWithinChunk(Location location, Chunk chunk) {
        return location.getChunk().equals(chunk);
    }

    private void sendLitCampfires(Player player, Chunk chunk) {

        BlockData litCampFire = BlockType.CAMPFIRE.createBlockData(campfire -> campfire.setLit(true));

        List<String> invalidCampFires = new ArrayList<>();

        PlayerData data = PlayerData.of(player);

        data.campfires.forEach((name, location) -> {
            if (isLocationWithinChunk(location, chunk)) {
                if (location.getBlock().getType() == Material.CAMPFIRE) {
                    player.sendBlockChange(location, litCampFire);
                } else {
                    invalidCampFires.add(name);
                }
            }
        });

        for (String name : invalidCampFires) {
            data.campfires.remove(name);
        }
    }

    @EventHandler
    public void onCampfireInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // Don't do anything if the opped player breaks the campfire
        if (player.isOp() && event.getAction().isLeftClick()) {
            return;
        }

        // Only allow clicking with main hand
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (block != null && block.getState() instanceof Campfire campfire) {

            // The block is loaded here anyway so it's fine to do this
            String campfireId = Accessors.CAMPFIRE_WARP_ID.read(campfire);
            Position warpPosition = Accessors.CAMPFIRE_WARP_POSITION.read(campfire);

            if (campfireId != null && warpPosition != null) {

                event.setCancelled(true);

                PlayerData data = PlayerData.of(player);
                BlockData blockData = BlockType.CAMPFIRE.createBlockData(unlitCampfire -> unlitCampfire.setLit(true));

                campfireCache.put(BlockPosition.ofBlock(block), new Pair<>(campfireId, warpPosition));

                if (!data.hasUnlockedCampfire(campfireId)) {

                    if (data.campfires.size() >= data.maxCampfires) {
                        player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_TOO_MANY_CAMPFIRES, data.campfires.size(), data.maxCampfires));
                    } else {
                        PlayerData.of(player).unlockCampfire(campfireId, block.getLocation());
                        VoidSurvival.getInstance().runTaskLater(() -> player.sendBlockChange(block.getLocation(), blockData), 1);
                        player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_UNLOCKED));
                    }

                } else {
                    PlayerUtil.openWarpMenu(player);
                    VoidSurvival.getInstance().runTaskLater(() -> player.sendBlockChange(block.getLocation(), blockData), 1);
                }
            }
        }
    }

    public Pair<String, Position> getCampfireData(Location location) {

        Pair<String, Position> entry = campfireCache.get(BlockPosition.ofLocation(location));

        if (entry == null && location.getBlock().getState() instanceof Campfire campfire) {

            String campfireId = Accessors.CAMPFIRE_WARP_ID.read(campfire);
            Position warpPosition = Accessors.CAMPFIRE_WARP_POSITION.read(campfire);

            entry = new Pair<>(campfireId, warpPosition);
        }

        return entry;
    }

    @EventHandler
    public void onPlayerChunkLoad(PlayerChunkLoadEvent event) {

        Player player = event.getPlayer();
        Chunk chunk = event.getChunk();

        VoidSurvival.getInstance().runTaskLater(() -> sendLitCampfires(player, chunk), 1);

        HashMap<ChunkPosition, HashMap<BlockPosition, Long>> changes = playerBlockChanges.get(event.getPlayer().getUniqueId());

        if (changes == null) {
            return;
        }

        HashMap<BlockPosition, Long> chunkChanges = changes.get(ChunkPosition.ofChunk(chunk));

        if (chunkChanges == null) {
            return;
        }

        // The method returns false if no block changes were sent
        // meaning we can just remove the entry altogether
        if (!sendBlockChanges(player, chunk, chunkChanges)) {
            changes.remove(ChunkPosition.ofChunk(chunk));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Allow ops to just modify the world as they wish
        if (player.isOp() || !Flags.REGENERATE_BLOCKS.test(player, block)) {
            return;
        }

        Long currentTicks = getBlockChangeTicks(player, block);

        // Don't do stuff if the block is already on cooldown
        if (currentTicks != null) {
            event.setCancelled(true);
            return;
        }

        Pair<Integer, Integer> regenerationTime = regenerationTimes.get(block.getType().asBlockType());

        if (regenerationTime != null) {

            ItemStack tool = player.getEquipment().getItemInMainHand();

            long ticks = regenerationTime.left();
            if (regenerationTime.left().compareTo(regenerationTime.right()) < 0) {
                ticks = ThreadLocalRandom.current().nextLong(regenerationTime.left(), regenerationTime.right());
            }

            markBlockAsDifferent(player, block, ticks);
            VoidSurvival.getInstance().runTaskLater(() -> changeBlockForPlayer(player, block), 1);
            PlayerUtil.giveItems(player, block.getDrops(tool, player));

            event.setCancelled(true);
        }
    }

    public Long getBlockChangeTicks(Player player, Block block) {

        var chunkChanges = playerBlockChanges.get(player.getUniqueId());
        if (chunkChanges == null) return null;
        var blockChanges = chunkChanges.get(ChunkPosition.ofChunk(block.getChunk()));
        if (blockChanges == null) return null;

        return blockChanges.get(BlockPosition.ofBlock(block));
    }

    public void changeBlockForPlayer(Player player, Block block) {

        BlockData differentData = createDifferentBlockData(block);

        if (differentData != null) {
            player.sendBlockChange(block.getLocation(), differentData);
        }
    }

    public void markBlockAsDifferent(Player player, Block block, @Nullable Long expirationTime) {
        playerBlockChanges.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .computeIfAbsent(ChunkPosition.ofChunk(block.getChunk()), k -> new HashMap<>())
            .put(BlockPosition.ofBlock(block), expirationTime);
    }

    /**
     *
     * @param player the player to send the block changes to
     * @param chunk the chunk the block changes will be in
     * @param changes the map of positions and how many ticks the change will last
     * @return {@code true} if some changes were sent, {@code false} if not.
     */
    private boolean sendBlockChanges(Player player, Chunk chunk, HashMap<BlockPosition, Long> changes) {

        World world = chunk.getWorld();
        Iterator<Map.Entry<BlockPosition, Long>> iterator = changes.entrySet().iterator();

        HashMap<Location, BlockData> blockChanges = new HashMap<>();

        while (iterator.hasNext()) {

            Map.Entry<BlockPosition, Long> entry = iterator.next();

            BlockPosition position = entry.getKey();
            Long remainingTicks = entry.getValue();

            if (remainingTicks != null && remainingTicks <= 0) {
                iterator.remove();
            } else {

                Location location = position.toLocation(world);
                BlockData data = createDifferentBlockData(location.getBlock());

                if (data != null) {
                    blockChanges.put(location, data);
                }
            }
        }

        if (!blockChanges.isEmpty()) {
            player.sendMultiBlockChange(blockChanges);
            return true;
        } else {
            return false;
        }
    }

    private BlockData createDifferentBlockData(Block block) {

        Material type = block.getType();

        if (type.name().toLowerCase().endsWith("ore")) {
            return BlockType.BEDROCK.createBlockData();
        } else if (type == Material.CAMPFIRE) {
            return BlockType.CAMPFIRE.createBlockData(campfire -> campfire.setLit(true));
        }

        return null;
    }
}
