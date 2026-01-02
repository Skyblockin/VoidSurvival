package com.skyblockin.voidsurvival.loot;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.message.MessageKeys;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.FileUtil;
import com.skyblockin.voidsurvival.util.Format;
import com.skyblockin.voidsurvival.util.Functions;
import com.skyblockin.voidsurvival.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LootChestManager implements Listener {

    private final HashMap<String, LootTable> tables = new HashMap<>();
    private final HashMap<Location, String> chestLootTables = new HashMap<>();
    private final HashMap<String, Long> cooldowns = new HashMap<>();
    private final HashMap<Block, ArrayList<UUID>> chestViewers = new HashMap<>();

    public LootTable getTable(String id) {
        return tables.get(id);
    }

    public void setChestLoot(Location location, String id) {
        chestLootTables.put(location, id);
    }

    public boolean deleteTable(Location location) {
        return chestLootTables.remove(location) != null;
    }

    public Set<String> getTableIds() {
        return tables.keySet();
    }

    public HashMap<Location, String> getChestLootTables() {
        return chestLootTables;
    }

    public void reload() {
        loadTables();
        // The chest locations are in a database managed by the plugin so uhhh they should probably not be reloaded each time
        // loadChestLocations();
        loadCooldowns();
    }

    public void loadCooldowns() {

        ConfigurationSection section = VoidSurvival.getInstance().getConfig().getConfigurationSection("chest-cooldowns");

        if (section == null) {
            VoidSurvival.logInfo("config.yml is missing section chest-cooldowns, chest cooldowns were not loaded.");
            return;
        }

        this.cooldowns.clear();

        for (String key : section.getKeys(false)) {
            long timeMillis = Functions.parseMillis(section.getString(key));
            this.cooldowns.put(key, timeMillis);
        }

        VoidSurvival.logInfo("Loaded chest cooldowns with %d entries.", this.cooldowns.size());
    }

    public void loadChestLocations() {

        this.chestLootTables.clear();
        this.chestLootTables.putAll(Database.getChestLocations());

        VoidSurvival.logInfo("Loaded %d chest locations.", this.chestLootTables.size());
    }

    public void loadTables() {

        this.tables.clear();

        for (File tableFile : FileUtil.listFiles("loot_tables")) {

            try {
                LootTable table = Json.readFromFile(tableFile, LootTable.class);
                this.tables.put(tableFile.getName().replace(".json", ""), table);
            } catch (Exception ex) {
                VoidSurvival.logError("Failed to load loot table from file '" + tableFile.getName() + "'", ex);
            }

        }

        VoidSurvival.logInfo("Loaded %d loot tables.", tables.size());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (event.getInventory().getHolder() instanceof DummyInventoryHolder holder && holder.getBlock() != null) {

            ArrayList<UUID> viewers = this.chestViewers.get(holder.getBlock());

            if (viewers == null) {
                VoidSurvival.logError("Detected close of DummyInventoryHolder with no viewers?");
                return;
            }

            viewers.remove(holder.getOpener());

            if (viewers.isEmpty()) {
                this.chestViewers.remove(holder.getBlock());
                if (holder.getBlock().getState() instanceof Chest chest) {
                    chest.close();
                }
            }
        }

    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        PlayerData data = PlayerData.of(player);

        if (block == null || !(block.getState() instanceof Chest)) {
            return;
        }

        String tableId = chestLootTables.get(block.getLocation());

        // No loot table set for this chest
        if (tableId == null) {
            return;
        }

        // Cancel the event so the chest does not open and we can do our own chest logic
        event.setCancelled(true);

        Instant lastTime = Instant.ofEpochMilli(data.getLastChestOpenTime(block));
        Long cooldown = cooldowns.get(tableId);
        Instant currentTime = Instant.now();

        if (cooldown == null) {
            VoidSurvival.logError("Table '%s' has no cooldown set! Is this intended? Defaulting to 0.", tableId);
            cooldown = 0L;
        }

        if (currentTime.isAfter(lastTime.plusMillis(cooldown))) {

            LootTable table = tables.get(tableId);

            if (table != null) {

                double lootBonus = Math.clamp(ChronoUnit.HOURS.between(lastTime, currentTime), 0, 72);

                LootUtil.openInventory(block, player, table, Functions.tableIdToName(tableId), lootBonus);

                this.chestViewers.computeIfAbsent(block, k -> new ArrayList<>()).add(player.getUniqueId());

                if (block.getState() instanceof Chest chest && !chest.isOpen()) {
                    chest.open();
                }

                data.setLastChestOpenTime(block, currentTime.toEpochMilli());

            } else {
                VoidSurvival.logError("%s tried to open loot table '%s' for chest at %d %d %d, but no such table exists.",
                    player.getName(), tableId, block.getX(), block.getY(), block.getZ()
                );
            }

        } else {
            long remainingTime = ChronoUnit.SECONDS.between(currentTime, lastTime.plusMillis(cooldown));
            player.sendMessage(TextUtil.message(MessageKeys.CHEST_ON_COOLDOWN, Format.getFormattedTime(remainingTime)));
        }
    }

}
