package com.skyblockin.voidsurvival;

import com.skyblockin.voidsurvival.manager.*;
import com.skyblockin.voidsurvival.block.CustomBlock;
import com.skyblockin.voidsurvival.chat.ChatListener;
import com.skyblockin.voidsurvival.command.BasicCommands;
import com.skyblockin.voidsurvival.command.FriendCommand;
import com.skyblockin.voidsurvival.command.MainCommand;
import com.skyblockin.voidsurvival.item.ItemManager;
import com.skyblockin.voidsurvival.entity.EntityEquipmentHandler;
import com.skyblockin.voidsurvival.entity.PlayerListener;
import com.skyblockin.voidsurvival.gui.GuiManager;
import com.skyblockin.voidsurvival.manager.campfire.CampfireManager;
import com.skyblockin.voidsurvival.manager.generation.WorldGenerationManager;
import com.skyblockin.voidsurvival.region.Flags;
import com.skyblockin.voidsurvival.region.RegionFlagListener;
import com.skyblockin.voidsurvival.social.FriendManager;
import com.skyblockin.voidsurvival.storage.*;
import com.skyblockin.voidsurvival.storage.database.*;
import com.skyblockin.voidsurvival.trigger.TriggerManager;
import com.skyblockin.voidsurvival.world.IslandGenerator;
import com.skyblockin.voidsurvival.world.edit.WorldListener;
import com.skyblockin.voidsurvival.world.town.TownManager;
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

    private SQLiteDatabase database;

    private MessageManager messageManager;
    private MiningManager miningManager;
    private IslandGenerator islandGenerator;
    private OreGeneratorManager oreGeneratorManager;
    private PlayerBlockManager playerBlockManager;
    private LootChestManager lootTableManager;
    private WorldGenerationManager worldGenerationManager;
    private LeaderboardManager leaderboardManager;
    private ItemManager itemManager;
    private RecipeManager recipeManager;
    private FriendManager friendManager;
    private GatheringNodeManager gatheringNodeManager;
    private TriggerManager triggerManager;
    private TownManager townManager;
    private CombatManager combatManager;

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

        this.combatTagDurationSeconds = (int) getTimeFromYaml(getConfig(), "combat-tag-duration-seconds", 10, "s");
        this.combatBlockedCommands = config.getStringList("blocked-commands");
        this.blockDegenerationSeconds = (int) getTimeFromYaml(getConfig(), "block-degeneration-seconds", 20, "s");
        this.debug = config.getBoolean("debug");
        this.infirmaryLocation = config.getLocation("missing-home-backup-location", null);

        this.messageManager = new MessageManager();
        this.islandGenerator = new IslandGenerator();
        this.oreGeneratorManager = new OreGeneratorManager();

        try {
            this.playerBlockManager = new PlayerBlockManager();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.miningManager = new MiningManager();
        this.lootTableManager = new LootChestManager();
        this.worldListener = new WorldListener();
        this.leaderboardManager = new LeaderboardManager();
        this.itemManager = new ItemManager();
        this.worldGenerationManager = new WorldGenerationManager(this);
        this.recipeManager = new RecipeManager();
        this.friendManager = new FriendManager();
        this.gatheringNodeManager = new GatheringNodeManager();
        this.triggerManager = new TriggerManager();
        this.townManager = new TownManager();
        this.combatManager = new CombatManager(this);

        try {

            this.database = new SQLiteDatabase();

            this.database.addTable(PlayerDataTable.class);
            this.database.addTable(IslandDataTable.class);
            this.database.addTable(GlobalDataTable.class);
            this.database.addTable(ChestDataTable.class);

            this.database.initialize();

        } catch (SQLException e) {
            logError("Failed to create database. Disabling plugin...", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.messageManager.reload(this);
        this.itemManager.loadItemsFromFile();
        this.playerBlockManager.reload(this);
        this.worldListener.loadGeneratedChunkCount();
        this.lootTableManager.loadTables(this);
        this.lootTableManager.loadChestLocations(this);
        this.lootTableManager.loadCooldowns(this);
        this.worldGenerationManager.reload(this);
        this.recipeManager.reload(this);
        this.gatheringNodeManager.reload(this);
        this.townManager.reload(this);
        this.oreGeneratorManager.reload(this);
        CustomBlock.load();

        CampfireManager campfireManager = new CampfireManager();

        registerEvents(
            GuiManager.init(),
            new ChatListener(),
            new PlayerJoinHandler(),
            this.playerBlockManager,
            this.worldListener,
            this.lootTableManager,
            this.worldGenerationManager,
            this.combatManager,
            this.gatheringNodeManager,
            this.triggerManager,
            this.townManager,
            this.oreGeneratorManager,
            new RegionFlagListener(),
            new EntityEquipmentHandler(),
            this.recipeManager,
            this.miningManager,
            new PlayerListener(),
            campfireManager
        );

        campfireManager.init();
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
                logError("Failed to register commands", ex);
            }
        });
    }

    public void reload() {
        reloadConfig();
        this.combatTagDurationSeconds = (int) getTimeFromYaml(getConfig(), "combat-tag-duration-seconds", 10, "s");
        this.combatBlockedCommands = getConfig().getStringList("blocked-commands");
        this.infirmaryLocation = getConfig().getLocation("missing-home-backup-location", null);
        this.messageManager.reload(this);
        this.itemManager.reload();
        this.islandGenerator.reload();
        this.playerBlockManager.reload(this);
        this.lootTableManager.reload(this);
        this.worldGenerationManager.reload(this);
        this.recipeManager.reload(this);
        this.gatheringNodeManager.reload(this);
        this.triggerManager.load();
        this.townManager.reload(this);
        this.oreGeneratorManager.reload(this);
        CustomBlock.load();
    }

    public void registerEvents(Listener... listeners) {

        PluginManager manager = getServer().getPluginManager();

        for (Listener listener : listeners) {
            manager.registerEvents(listener, this);
        }
    }

    @Override
    public void onDisable() {

        try {

            saveConfig();

            this.database.getTable(PlayerDataTable.class).savePlayers(PLAYER_DATA_MAP);
            this.database.getTable(ChestDataTable.class).saveChestLocations(this.lootTableManager.getChestLootTables());
            this.database.getTable(GlobalDataTable.class).saveGlobalData();

            // Wait for all tasks before shutting down (waitForAllTasksAndShutdown?), I don't know, weird names
            this.playerBlockManager.shutdown();
            this.database.waitForAllTasksAndShutdown();

            this.gatheringNodeManager.save();
            this.triggerManager.save();

            getServer().getScheduler().cancelTasks(this);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public int getCombatTagDurationSeconds() {
        return combatTagDurationSeconds;
    }

    public List<String> getCombatBlockedCommands() {
        return combatBlockedCommands;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public MiningManager getMiningManager() {
        return miningManager;
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

    public PlayerBlockManager getPlayerBlockManager() {
        return playerBlockManager;
    }

    public CombatManager getCombatTracker() {
        return combatManager;
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

    public GatheringNodeManager getGatheringNodeManager() {
        return gatheringNodeManager;
    }

    public TriggerManager getTriggerManager() {
        return triggerManager;
    }

    public TownManager getTownManager() {
        return townManager;
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

    public File getOrCreateFile(Path path) {

        File file = getFile(path);

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                logError("Failed to create file: " + file.getAbsolutePath(), e);
                return null;
            }
        }

        return file;
    }

    public File getOrCreateFile(String first, String... others) {
        return getOrCreateFile(Path.of(first, others));
    }

    public File getFile(Path path) {
        return getDataPath().resolve(path).toFile();
    }

    public File getFile(String first, String... others) {
        return getFile(Path.of(first, others));
    }

    public static SQLiteDatabase getDatabase() {
        return VoidSurvival.getInstance().database;
    }

    public static <T extends SQLiteTable> T getTable(Class<T> clazz) {
        return getDatabase().getTable(clazz);
    }

    public static PlayerDataTable getPlayerTable() {
        return getDatabase().getTable(PlayerDataTable.class);
    }

    public static IslandDataTable getIslandTable() {
        return getDatabase().getTable(IslandDataTable.class);
    }

    public static ChestDataTable getChestTable() {
        return getDatabase().getTable(ChestDataTable.class);
    }

    public static GlobalDataTable getGlobalTable() {
        return getDatabase().getTable(GlobalDataTable.class);
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

    public static void logError(String message, Throwable throwable, Object... objects) {
        LOGGER.log(Level.SEVERE, String.format(message, objects), throwable);
    }

    public static void logDebug(String message, Object... objects) {
        if (getInstance().debug) {
            LOGGER.log(Level.INFO, "[DEBUG] " + String.format(message, objects));
        }
    }

}