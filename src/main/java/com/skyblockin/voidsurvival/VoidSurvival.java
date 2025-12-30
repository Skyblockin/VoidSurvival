package com.skyblockin.voidsurvival;

import com.skyblockin.voidsurvival.ability.Abilities;
import com.skyblockin.voidsurvival.ability.AbilityListener;
import com.skyblockin.voidsurvival.chat.ChatListener;
import com.skyblockin.voidsurvival.combat.CombatTracker;
import com.skyblockin.voidsurvival.command.BasicCommands;
import com.skyblockin.voidsurvival.command.FriendCommand;
import com.skyblockin.voidsurvival.command.MainCommand;
import com.skyblockin.voidsurvival.config.ItemManager;
import com.skyblockin.voidsurvival.entity.EntityEquipmentHandler;
import com.skyblockin.voidsurvival.entity.PlayerListener;
import com.skyblockin.voidsurvival.leaderboard.LeaderboardManager;
import com.skyblockin.voidsurvival.loot.LootChestManager;
import com.skyblockin.voidsurvival.recipe.RecipeManager;
import com.skyblockin.voidsurvival.region.Flags;
import com.skyblockin.voidsurvival.region.RegionFlagListener;
import com.skyblockin.voidsurvival.social.FriendManager;
import com.skyblockin.voidsurvival.storage.*;
import com.skyblockin.voidsurvival.util.CustomTimeUnit;
import com.skyblockin.voidsurvival.util.FileUtil;
import com.skyblockin.voidsurvival.world.IslandGenerator;
import com.skyblockin.voidsurvival.world.OreGenerator;
import com.skyblockin.voidsurvival.world.WorldListener;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.skyblockin.voidsurvival.storage.PlayerData.PLAYER_DATA_MAP;
import static com.skyblockin.voidsurvival.util.Functions.getTimeFromYaml;

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
    private ItemManager itemManager;
    private RecipeManager recipeManager;
    private FriendManager friendManager;

    private WorldListener worldListener;

    private boolean allowJoins = false;
    private boolean debug = false;
    private Location infirmaryLocation;
    private int blockDegenerationSeconds = 20;
    private int combatTagDurationSeconds = 10;
    private List<String> combatBlockedCommands = new ArrayList<>();

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

        registerCommands();

        this.combatTagDurationSeconds = (int) getTimeFromYaml(getConfig(), "combat-tag-duration-seconds", 10, CustomTimeUnit.SECONDS);
        this.combatBlockedCommands = config.getStringList("blocked-commands");
        this.blockDegenerationSeconds = (int) getTimeFromYaml(getConfig(), "block-degeneration-seconds", 20, CustomTimeUnit.SECONDS);
        this.debug = config.getBoolean("debug");
        this.infirmaryLocation = config.getLocation("missing-home-backup-location", null);

        this.islandGenerator = new IslandGenerator();
        this.oreGenerator = new OreGenerator();
        this.lootTableManager = new LootChestManager();
        this.worldListener = new WorldListener();
        this.leaderboardManager = new LeaderboardManager();
        this.itemManager = new ItemManager();
        this.recipeManager = new RecipeManager();
        this.friendManager = new FriendManager();

        try {
            Database.createDataBase();
        } catch (SQLException e) {
            logError("Failed to create database. Disabling plugin...", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.itemManager.loadItemsFromFile();
        this.oreGenerator.loadMissingBlocksFromFile();
        this.worldListener.loadGeneratedChunkCount();
        this.lootTableManager.loadTables();
        this.lootTableManager.loadChestLocations();
        this.lootTableManager.loadCooldowns();
        this.recipeManager.loadRecipes();

        FileUtil.createOrGetFile("mana_abilities.yml");

        AuraSkillsApi.get()
            .useRegistry("voidsurvival", getDataFolder())
            .registerManaAbility(Abilities.TREECAPITATOR);

        registerEvents(
            new ChatListener(),
            new PlayerJoinHandler(),
            this.oreGenerator,
            this.worldListener,
            this.lootTableManager,
            new CombatTracker(),
            new RegionFlagListener(),
            new EntityEquipmentHandler(),
            this.recipeManager,
            new AbilityListener(),
            new PlayerListener()
        );

        this.allowJoins = true;
    }

    public void registerCommands() {

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {

            try {

                File aliasFile = getDataFolder().toPath().resolve("aliases.yml").toFile();

                if (!aliasFile.exists()) {
                    Files.createDirectories(aliasFile.toPath().getParent());
                    Files.createFile(aliasFile.toPath());
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(aliasFile);

                commands.registrar().register(MainCommand.COMMAND);
                commands.registrar().register(FriendCommand.COMMAND);
                BasicCommands.register(commands.registrar(), config);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void reload() {
        reloadConfig();
        this.combatTagDurationSeconds = (int) getTimeFromYaml(getConfig(), "combat-tag-duration-seconds", 10, CustomTimeUnit.SECONDS);
        this.combatBlockedCommands = getConfig().getStringList("blocked-commands");
        this.infirmaryLocation = getConfig().getLocation("missing-home-backup-location", null);
        this.itemManager.reload();
        this.islandGenerator.reload();
        this.oreGenerator.reload();
        this.lootTableManager.reload();
        this.recipeManager.loadRecipes();
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

    public int getCombatTagDurationSeconds() {
        return combatTagDurationSeconds;
    }

    public List<String> getCombatBlockedCommands() {
        return combatBlockedCommands;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public int getBlockDegenerationSeconds() {
        return blockDegenerationSeconds;
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

    public static NamespacedKey createKey(String key) {
        return new NamespacedKey(VoidSurvival.getInstance(), key);
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
            LOGGER.log(Level.INFO, "[DEBUG] " + String.format(message, objects));
        }
    }

}