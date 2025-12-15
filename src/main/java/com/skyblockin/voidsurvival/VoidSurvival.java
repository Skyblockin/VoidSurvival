package com.skyblockin.voidsurvival;

import com.skyblockin.voidsurvival.chat.ChatListener;
import com.skyblockin.voidsurvival.combat.CombatTracker;
import com.skyblockin.voidsurvival.constants.DamageTypes;
import com.skyblockin.voidsurvival.constants.ItemIds;
import com.skyblockin.voidsurvival.leaderboard.LeaderboardManager;
import com.skyblockin.voidsurvival.loot.LootChestManager;
import com.skyblockin.voidsurvival.region.Flags;
import com.skyblockin.voidsurvival.storage.*;
import com.skyblockin.voidsurvival.world.IslandGenerator;
import com.skyblockin.voidsurvival.world.OreGenerator;
import com.skyblockin.voidsurvival.world.WorldListener;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.skyblockin.voidsurvival.storage.PlayerData.PLAYER_DATA_MAP;

public class VoidSurvival extends JavaPlugin {

    private static Logger LOGGER;
    private static VoidSurvival INSTANCE;

    public static VoidSurvival getInstance() {
        return INSTANCE;
    }

    private IslandGenerator islandGenerator;
    private OreGenerator oreGenerator;
    private LootChestManager lootTableManager;
    private LeaderboardManager leaderboardManager;

    private WorldListener worldListener;

    private boolean allowJoins = false;
    private boolean debug = false;
    private Location infirmaryLocation;

    @Override
    public void onLoad() {
        LOGGER = getLogger();
        INSTANCE = this;

        Flags.registerAll();
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        FileConfiguration config = getConfig();

        this.debug = config.getBoolean("debug");
        this.infirmaryLocation = config.getLocation("missing-home-backup-location", null);

        this.islandGenerator = new IslandGenerator();
        this.oreGenerator = new OreGenerator();
        this.lootTableManager = new LootChestManager();
        this.worldListener = new WorldListener();
        this.leaderboardManager = new LeaderboardManager();

        try {
            Database.createDataBase();
        } catch (SQLException e) {
            logError("Failed to create database. Disabling plugin...", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.oreGenerator.loadMissingBlocksFromFile();
        this.worldListener.loadGeneratedChunkCount();
        this.lootTableManager.loadTables();
        this.lootTableManager.loadChestLocations();
        this.lootTableManager.loadCooldowns();

        registerEvents(
            new ChatListener(),
            new PlayerJoinHandler(),
            this.oreGenerator,
            this.worldListener,
            this.lootTableManager,
            new CombatTracker()
        );

        this.allowJoins = true;

        getServer().getScheduler().runTaskTimer(this, () -> {

            getServer().getOnlinePlayers().forEach(player -> {

                ItemStack item = player.getEquipment().getHelmet();

                if (item != null && !item.isEmpty() && Accessors.ITEM_ID.equals(item, ItemIds.AVARITIA)) {
                    player.damage(4, DamageSource.builder(DamageTypes.GREED).build());
                }
            });
        }, 0, 20L);
    }

    public void reload() {
        reloadConfig();
        this.infirmaryLocation = getConfig().getLocation("missing-home-backup-location", null);
        this.islandGenerator.reload();
        this.oreGenerator.reload();
        this.lootTableManager.reload();
    }

    private void registerEvents(Listener... listeners) {

        PluginManager manager = getServer().getPluginManager();

        for (Listener listener : listeners) {
            manager.registerEvents(listener, this);
        }
    }

    @Override
    public void onDisable() {

        try {

            saveConfig();

            this.oreGenerator.writeMissingBlocksToFile();

            Database.savePlayers(PLAYER_DATA_MAP.values());
            Database.saveChestLocations(this.lootTableManager.getChestLootTables());
            Database.saveGlobalData();

            getServer().getScheduler().cancelTasks(this);

        } catch (IOException | PlayerDataException e) {
            throw new RuntimeException(e);
        }

    }

    public @Nullable Location getInfirmaryLocation() {
        return infirmaryLocation;
    }

    public LootChestManager getLootTableManager() {
        return this.lootTableManager;
    }

    public WorldListener getWorldListener() {
        return this.worldListener;
    }

    public IslandGenerator getIslandGenerator() {
        return this.islandGenerator;
    }

    public LeaderboardManager getLeaderboardManager() {
        return this.leaderboardManager;
    }

    public boolean isAllowingJoins() {
        return allowJoins;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public BukkitTask runTask(Runnable runnable) {
        return getServer().getScheduler().runTask(this, runnable);
    }

    public BukkitTask runTaskTimer(Runnable runnable, long delay, long period) {
        return getServer().getScheduler().runTaskTimer(this, runnable, delay, period);
    }

    public BukkitTask runTaskLater(Runnable runnable, long delay) {
        return getServer().getScheduler().runTaskLater(this, runnable, delay);
    }

    public File[] getIslandFiles() {

        File islandDirectory = Path.of(getDataFolder().getAbsolutePath(), "islands").toFile();

        if (!islandDirectory.exists()) {
            if (!islandDirectory.mkdirs()) {
                throw new RuntimeException("Failed to create islands directory, is the folder read only?");
            }
        }

        return islandDirectory.listFiles();
    }

    public static void logInfo(String message, Object... objects) {
        LOGGER.log(Level.INFO, String.format(message, objects));
    }

    public static void logError(String message, Object... objects) {
        LOGGER.log(Level.SEVERE, String.format(message, objects));
    }

    public static void logError(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void logDebug(String message, Object... objects) {
        if (getInstance().debug) {
            LOGGER.log(Level.INFO, "[DEBUG]" + String.format(message, objects));
        }
    }

}