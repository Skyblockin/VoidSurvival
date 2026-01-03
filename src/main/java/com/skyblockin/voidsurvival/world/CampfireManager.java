package com.skyblockin.voidsurvival.world;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.event.CampfireClickEvent;
import com.skyblockin.voidsurvival.event.CampfireLoadEvent;
import com.skyblockin.voidsurvival.event.CampfireUnloadEvent;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.math.Position;
import com.skyblockin.voidsurvival.message.MessageKeys;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.PlayerUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.Campfire;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class CampfireManager implements Listener {

    private final HashMap<UUID, ArrayList<UUID>> campfireHolograms = new HashMap<>();
    public static final HashMap<BlockPosition, Pair<String, Position>> CAMPFIRE_CACHE = new HashMap<>();

    @EventHandler
    public void onCampfireLoad(CampfireLoadEvent event) {

        Player player = event.getPlayer();
        PlayerData data = PlayerData.of(player);
        String campfireId = event.getId();

        Location center = event.getLocation().add(0.5, 0, 0.5);

        if (!data.hasUnlockedCampfire(campfireId)) {
            createCampfireHologram("<gray>Unlit Campfire [ʀɪɢʜᴛ-ᴄʟɪᴄᴋ]", player, center, campfireId);
        } else {
            createCampfireHologram("<gradient:#c10529:#f64510>Campfire</gradient>", player, center, campfireId);
            BlockData litCampFire = BlockType.CAMPFIRE.createBlockData(campfire -> campfire.setLit(true));
            VoidSurvival.getInstance().runTaskLater(() -> player.sendBlockChange(event.getLocation(), litCampFire), 1);
        }

    }

    @EventHandler
    public void onCampfireUnload(CampfireUnloadEvent event) {

        Player player = event.getPlayer();
        PlayerData data = PlayerData.of(player);
        String campfireId = event.getId();

        // No reason to do anything if the campfire hasn't been unlocked
        if (!data.campfires.containsKey(campfireId)) {
            return;
        }

        deleteCampfireHologram(player, campfireId);
    }

    @EventHandler
    public void onCampfireClick(CampfireClickEvent event) {

        Player player = event.getPlayer();
        PlayerData data = PlayerData.of(player);
        String campfireId = event.getId();
        Block block = event.getBlock();
        BlockData blockData = BlockType.CAMPFIRE.createBlockData(unlitCampfire -> unlitCampfire.setLit(true));

        if (!data.hasUnlockedCampfire(campfireId)) {

            if (data.campfires.size() >= data.maxCampfires) {
                player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_TOO_MANY_CAMPFIRES, data.campfires.size(), data.maxCampfires));
            } else {

                PlayerData.of(player).unlockCampfire(campfireId, block.getLocation());
                VoidSurvival.getInstance().runTaskLater(() -> player.sendBlockChange(block.getLocation(), blockData), 1);
                player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_UNLOCKED));

                deleteCampfireHologram(player, campfireId);
                createCampfireHologram("<gradient:#c10529:#f64510>Campfire</gradient>", player, block.getLocation().add(0.5, 0, 0.5), campfireId);
            }

        } else {
            PlayerUtil.openWarpMenu(player);
            VoidSurvival.getInstance().runTaskLater(() -> player.sendBlockChange(block.getLocation(), blockData), 1);
        }

    }

    @EventHandler
    public void onPlayerChunkLoad(PlayerChunkLoadEvent event) {

        Player player = event.getPlayer();
        Chunk chunk = event.getChunk();

        chunk.getTileEntities(block -> block.getState() instanceof Campfire, false)
            .forEach(state -> {
                if (state instanceof Campfire campfire) {

                    String campfireId = Accessors.CAMPFIRE_WARP_ID.read(campfire);
                    Position warpPosition = Accessors.CAMPFIRE_WARP_POSITION.read(campfire);

                    if (campfireId != null && warpPosition != null) {
                        CampfireLoadEvent.callEvent(player, campfireId, campfire.getLocation());
                    }
                }
            });

    }

    @EventHandler
    public void onPlayerChunkUnload(PlayerChunkUnloadEvent event) {

        Player player = event.getPlayer();
        Chunk chunk = event.getChunk();

        chunk.getTileEntities(block -> block.getState() instanceof Campfire, false)
            .forEach(state -> {
                if (state instanceof Campfire campfire) {

                    String campfireId = Accessors.CAMPFIRE_WARP_ID.read(campfire);
                    Position warpPosition = Accessors.CAMPFIRE_WARP_POSITION.read(campfire);

                    if (campfireId != null && warpPosition != null) {
                        CampfireUnloadEvent.callEvent(player, campfireId, campfire.getLocation());
                    }
                }
            });

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
                CAMPFIRE_CACHE.put(BlockPosition.ofBlock(block), new Pair<>(campfireId, warpPosition));
                CampfireClickEvent.callEvent(player, block, campfireId);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getState() instanceof Campfire campfire) {
            String id = Accessors.CAMPFIRE_WARP_ID.read(campfire);
            if (id != null) {
                deleteCampfireHologram(player, id);
                Database.deleteCampfireFromEveryoneById(id);
            }
        }
    }

    public static Pair<String, Position> getCampfireData(Location location) {

        Pair<String, Position> entry = CAMPFIRE_CACHE.get(BlockPosition.ofLocation(location));

        if (entry == null && location.getBlock().getState() instanceof Campfire campfire) {

            String campfireId = Accessors.CAMPFIRE_WARP_ID.read(campfire);
            Position warpPosition = Accessors.CAMPFIRE_WARP_POSITION.read(campfire);

            entry = new Pair<>(campfireId, warpPosition);
        }

        return entry;
    }

    private void deleteCampfireHologram(Player player, String campfireId) {

        ArrayList<UUID> uuids = campfireHolograms.get(player.getUniqueId());

        // Oh, well nothing to remove I guess
        if (uuids == null) {
            return;
        }

        Iterator<UUID> iterator = uuids.iterator();

        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            if (Bukkit.getEntity(uuid) instanceof ArmorStand stand) {
                if (campfireId.equals(Accessors.CAMPFIRE_WARP_ID.read(stand))) {
                    iterator.remove();
                    stand.remove();
                }
            }
        }

        if (uuids.isEmpty()) {
            campfireHolograms.remove(player.getUniqueId());
        }

    }

    private ArmorStand createCampfireHologram(String text, Player player, Location location, String campfireId) {

        ArmorStand stand = player.getWorld().spawn(location, ArmorStand.class, entity -> {
            entity.setVisibleByDefault(false);
            entity.setInvisible(true);
            entity.setSmall(true);
            entity.setCustomNameVisible(true);
            entity.customName(TextUtil.color(text));
            Accessors.CAMPFIRE_WARP_ID.write(entity, campfireId);
        });

        campfireHolograms.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(stand.getUniqueId());

        player.showEntity(VoidSurvival.getInstance(), stand);

        return stand;

    }

}
