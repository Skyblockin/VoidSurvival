package com.skyblockin;

import com.skyblockin.chat.ChatListener;
import com.skyblockin.config.Json;
import com.skyblockin.region.Flags;
import com.skyblockin.storage.Database;
import com.skyblockin.storage.PlayerDataException;
import com.skyblockin.storage.PlayerJoinHandler;
import com.skyblockin.world.IslandGenerator;
import com.skyblockin.world.OreGenerator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.skyblockin.storage.PlayerData.PLAYER_DATA_MAP;

public class VoidSurvival extends JavaPlugin {

    private static Logger LOGGER;
    private static VoidSurvival INSTANCE;

    public static VoidSurvival getInstance() {
        return INSTANCE;
    }

    private IslandGenerator islandGenerator;
    private OreGenerator oreGenerator;

    private boolean allowJoins = false;
    private boolean debug = false;

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
        this.islandGenerator = new IslandGenerator();
        this.oreGenerator = new OreGenerator();

        try {
            this.oreGenerator.loadMissingBlocksFromFile();
        } catch (Exception e) {
            VoidSurvival.logError("Failed to load missing blocks from file", e);
        }

        registerEvents(
            new ChatListener(),
            new PlayerJoinHandler(),
            this.oreGenerator
        );

        try {
            Database.createDataBase();
        } catch (SQLException e) {
            logError("Failed to create database. Disabling plugin...", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.allowJoins = true;
    }

    public void reload() {
        this.islandGenerator.reload();
        this.oreGenerator.reload();
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

        } catch (IOException | PlayerDataException e) {
            throw new RuntimeException(e);
        }

    }

    public IslandGenerator getIslandGenerator() {
        return this.islandGenerator;
    }

    private void setupIslandGeneration(ConfigurationSection section) {

        this.islandGenerator = new IslandGenerator();

        logInfo("Initialized IslandGenerator");
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