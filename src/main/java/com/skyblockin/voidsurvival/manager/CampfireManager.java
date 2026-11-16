package com.skyblockin.voidsurvival.manager;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.event.CampfireClickEvent;
import com.skyblockin.voidsurvival.event.CampfireLoadEvent;
import com.skyblockin.voidsurvival.math.Position;
import com.skyblockin.voidsurvival.message.MessageKeys;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.PlayerUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.block.Campfire;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;

public class CampfireManager implements Listener {

    private static final String UNLIT_TEXT = "<dark_gray>Unlit Campfire [ʀɪɢʜᴛ-ᴄʟɪᴄᴋ]";
    private static final String LIT_TEXT = "<gradient:#c10529:#f64510>Campfire</gradient>";

    /** The two shared hologram stands of one campfire */
    private record Holograms(UUID unlit, UUID lit) {}

    /** campfireId -> holograms. Only contains campfires in currently loaded chunks. */
    private final HashMap<String, Holograms> holograms = new HashMap<>();

    /**
     * Call once after registering the listener. Chunks that were loaded before
     * the plugin enabled never fire ChunkLoadEvent, so handle them here.
     */
    public void init() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                forEachCampfire(chunk, this::spawnHolograms);
            }
        }
    }

    // ---- Hologram lifecycle: tied to the chunk, never to players ----

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        forEachCampfire(event.getChunk(), this::spawnHolograms);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        forEachCampfire(event.getChunk(), (id, location) -> removeHolograms(id));
    }

    /**
     * Our stands are non-persistent, so any tagged stand loaded from disk
     * is a leftover from the old per-player system. Delete it.
     */
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof ArmorStand stand && Accessors.CAMPFIRE_WARP_ID.read(stand) != null) {
                stand.remove();
            }
        }
    }

    // ---- Per-player visibility ----

    @EventHandler
    public void onCampfireLoad(CampfireLoadEvent event) {

        Player player = event.getPlayer();
        String campfireId = event.getId();
        boolean unlocked = PlayerData.of(player).hasUnlockedCampfire(campfireId);

        updateVisibility(player, campfireId, unlocked);

        if (unlocked) {
            BlockData litCampfire = BlockType.CAMPFIRE.createBlockData(campfire -> campfire.setLit(true));
            VoidSurvival.getInstance().runTaskLater(() -> player.sendBlockChange(event.getLocation(), litCampfire), 1);
        }
    }

    @EventHandler
    public void onCampfireClick(CampfireClickEvent event) {

        Player player = event.getPlayer();
        PlayerData data = PlayerData.of(player);
        String campfireId = event.getId();
        Block block = event.getBlock();
        BlockData litCampfire = BlockType.CAMPFIRE.createBlockData(campfire -> campfire.setLit(true));

        if (!data.hasUnlockedCampfire(campfireId)) {

            if (data.campfires.size() >= data.maxCampfires) {
                player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_TOO_MANY_CAMPFIRES, data.campfires.size(), data.maxCampfires));
                return;
            }

            data.unlockCampfire(campfireId, block.getLocation());
            player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_UNLOCKED));
            updateVisibility(player, campfireId, true);

        } else {
            PlayerUtil.openWarpMenu(player);
        }

        VoidSurvival.getInstance().runTaskLater(() -> player.sendBlockChange(block.getLocation(), litCampfire), 1);
    }

    // ---- Unchanged interaction handling ----

    @EventHandler
    public void onPlayerChunkLoad(PlayerChunkLoadEvent event) {
        Player player = event.getPlayer();
        forEachCampfire(event.getChunk(), (id, location) -> CampfireLoadEvent.callEvent(player, id, location));
    }

    @EventHandler
    public void onCampfireInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (player.isOp() && event.getAction().isLeftClick()) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (block != null && block.getState() instanceof Campfire campfire) {

            String campfireId = Accessors.CAMPFIRE_WARP_ID.read(campfire);
            Position warpPosition = Accessors.CAMPFIRE_WARP_POSITION.read(campfire);

            if (campfireId != null && warpPosition != null) {
                event.setCancelled(true);
                CampfireClickEvent.callEvent(player, block, campfireId);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (event.getBlock().getState() instanceof Campfire campfire) {
            String id = Accessors.CAMPFIRE_WARP_ID.read(campfire);
            if (id != null) {
                removeHolograms(id);
                VoidSurvival.getPlayerTable().deleteCampfireFromEveryoneById(id);
            }
        }
    }

    public static Pair<String, Position> getCampfireData(Location location) {

        if (location.getBlock().getState() instanceof Campfire campfire) {

            String campfireId = Accessors.CAMPFIRE_WARP_ID.read(campfire);
            Position warpPosition = Accessors.CAMPFIRE_WARP_POSITION.read(campfire);

            return new Pair<>(campfireId, warpPosition);
        }

        return null;
    }

    // ---- Helpers ----

    /** Calls the consumer with (campfireId, location) for every warp campfire in the chunk */
    private void forEachCampfire(Chunk chunk, BiConsumer<String, Location> consumer) {

        // Checking the type first is much cheaper than creating a BlockState snapshot for every tile entity
        for (BlockState state : chunk.getTileEntities(block -> Tag.CAMPFIRES.isTagged(block.getType()), false)) {

            if (state instanceof Campfire campfire) {

                String campfireId = Accessors.CAMPFIRE_WARP_ID.read(campfire);
                Position warpPosition = Accessors.CAMPFIRE_WARP_POSITION.read(campfire);

                if (campfireId != null && warpPosition != null) {
                    consumer.accept(campfireId, campfire.getLocation());
                }
            }
        }
    }

    private void spawnHolograms(String campfireId, Location blockLocation) {

        // Already spawned (e.g. init() and ChunkLoadEvent both saw this chunk)
        if (holograms.containsKey(campfireId)) {
            return;
        }

        Location center = blockLocation.clone().add(0.5, 0, 0.5);

        UUID unlit = spawnStand(center, UNLIT_TEXT, campfireId);
        UUID lit = spawnStand(center, LIT_TEXT, campfireId);

        holograms.put(campfireId, new Holograms(unlit, lit));
    }

    private void removeHolograms(String campfireId) {

        Holograms h = holograms.remove(campfireId);

        if (h == null) {
            return;
        }

        removeEntity(h.unlit());
        removeEntity(h.lit());
    }

    private void updateVisibility(Player player, String campfireId, boolean unlocked) {

        Holograms h = holograms.get(campfireId);

        if (h == null) {
            return;
        }

        Entity show = Bukkit.getEntity(unlocked ? h.lit() : h.unlit());
        Entity hide = Bukkit.getEntity(unlocked ? h.unlit() : h.lit());

        if (hide != null) player.hideEntity(VoidSurvival.getInstance(), hide);
        if (show != null) player.showEntity(VoidSurvival.getInstance(), show);
    }

    private UUID spawnStand(Location location, String text, String campfireId) {
        return location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setPersistent(false);      // never saved to disk, so no duplicates across restarts
            stand.setVisibleByDefault(false); // only players we explicitly show it to can see it
            stand.setInvisible(true);
            stand.setMarker(true);           // no hitbox, so it can't block clicks on the campfire
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setCustomNameVisible(true);
            stand.customName(TextUtil.color(text));
            Accessors.CAMPFIRE_WARP_ID.write(stand, campfireId);
        }).getUniqueId();
    }

    private void removeEntity(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity != null) {
            entity.remove();
        }
    }
}