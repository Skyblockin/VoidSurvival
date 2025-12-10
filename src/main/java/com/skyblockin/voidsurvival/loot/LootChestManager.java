package com.skyblockin.voidsurvival.loot;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.Format;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class LootChestManager implements Listener {

    private final HashMap<String, LootTable> tables = new HashMap<>();
    private final HashMap<Location, String> chestLootTables = new HashMap<>();
    private final HashMap<String, Long> cooldowns = new HashMap<>();

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
            this.cooldowns.put(key, section.getLong(key) * 1000L);
        }

        VoidSurvival.logInfo("Loaded chest cooldowns with %d entries.", this.cooldowns.size());
    }

    public void loadChestLocations() {

        this.chestLootTables.clear();
        this.chestLootTables.putAll(Database.getChestLocations());

        VoidSurvival.logInfo("Loaded %d chest locations.", this.chestLootTables.size());
    }

    public void loadTables() {

        File file = new File(VoidSurvival.getInstance().getDataFolder(), "loot_tables");

        if (!file.exists()) {
            if (file.mkdirs()) {
                VoidSurvival.logInfo("The loot tables folder was missing, so it was created.");
            } else {
                VoidSurvival.logError("The loot tables folder was missing, and it could not be created. Is the plugin folder read-only?");
            }
            return;
        }

        File[] tableFiles = file.listFiles();

        if (tableFiles == null) return;

        this.tables.clear();

        for (File tableFile : tableFiles) {

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
    public void onChestOpen(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        PlayerData data = PlayerData.of(player);

        if (block == null || !(BlockType.CHEST.equals(block.getType().asBlockType()) || BlockType.TRAPPED_CHEST.equals(block.getType().asBlockType()))) {
            return;
        }

        String tableId = chestLootTables.get(block.getLocation());

        if (tableId == null) {
            return;
        }

        // Cancel the event so the chest does not open and we can do our own chest logic
        event.setCancelled(true);

        Long lastTime = data.getLastChestOpenTime(block);
        Long cooldown = cooldowns.get(tableId);
        long currentTime = System.currentTimeMillis();

        if (cooldown == null) {
            VoidSurvival.logError("Table '%s' has no cooldown set! Is this intended? Defaulting to 0.", tableId);
            cooldown = 0L;
        }

        if (lastTime == null || lastTime + cooldown <= currentTime) {

            LootTable table = tables.get(tableId);

            if (table != null) {
                LootUtil.openInventory(player, table, tableIdToName(tableId), 0);
                data.setLastChestOpenTime(block, currentTime);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
            } else {
                VoidSurvival.logError("%s to open loot table '%s' for chest at %d %d %d, but no such table exists.",
                    player.getName(), tableId, block.getX(), block.getY(), block.getZ()
                );
            }

        } else {
            long remainingTime = lastTime + cooldown - currentTime;
            player.sendRichMessage("<red>You must wait " + Format.getFormattedTime(remainingTime / 1000) + " before opening this chest again.");
        }
    }

    private String tableIdToName(String tableId) {

        String[] parts = tableId.split("_");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1);
        }

        return String.join(" ", parts);
    }
}
