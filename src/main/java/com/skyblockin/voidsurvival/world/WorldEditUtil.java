package com.skyblockin.voidsurvival.world;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.loot.LootChestManager;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.math.Cuboid;
import com.skyblockin.voidsurvival.math.Position;
import com.skyblockin.voidsurvival.region.Flags;
import org.bukkit.World;
import org.bukkit.block.Chest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class WorldEditUtil {

    public static Clipboard loadSchematic(File file) {

        try {

            ClipboardFormat format = ClipboardFormats.findByFile(file);

            if (format == null) {
                VoidSurvival.logError("Failed to load schematic '%s', does it exist?", file.getName());
                return null;
            }

            Clipboard clipboard = null;

            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                clipboard = reader.read();
            }

            return clipboard;

        } catch (Exception ex) {
            VoidSurvival.logError("Caught error while loading schematic '%s': %s", file.getName(), ex.getMessage());
            return null;
        }
    }

    public static Clipboard saveSchematic(Path path, World world, BlockVector3 origin, BlockVector3 min, BlockVector3 max) throws IOException, WorldEditException {

        CuboidRegion selection = new CuboidRegion(min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(selection);
        clipboard.setOrigin(origin);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
            BukkitAdapter.adapt(world), selection, clipboard, min
        );

        Operations.complete(forwardExtentCopy);

        File schematicFile = VoidSurvival.getInstance().getFile(path);

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(schematicFile))) {
            writer.write(clipboard);
        }

        return clipboard;
    }

    public static void pasteClipboardAt(Clipboard clipboard, World world, int x, int y, int z, boolean ignoreAir) throws WorldEditException {

        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(world);

        EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld);

        Operation operation = new ClipboardHolder(clipboard)
            .createPaste(editSession)
            .to(BlockVector3.at(x, y, z))
            .ignoreAirBlocks(ignoreAir)
            .build();

        Operations.complete(operation);

        editSession.close();
    }

    public static void pasteDungeonAt(String name, String dungeonId, World world, int x, int y, int z) {

        DungeonVariables variables;

        try {
            File file = VoidSurvival.getInstance().getFile("dungeons", dungeonId + ".json");
            variables = Json.readFromFile(file, DungeonVariables.class);
        } catch (Exception ex) {
            VoidSurvival.logError("Failed to read config file from dungeons/" + dungeonId + ".json", ex);
            return;
        }

        Clipboard clipboard = loadSchematic(VoidSurvival.getInstance().getFile("dungeons", variables.schematic));

        if (clipboard == null) {
            return;
        }

        BlockPosition origin = new BlockPosition(x, y, z);

        try {
            pasteClipboardAt(clipboard, world, x, y, z, true);
        } catch (Exception ex) {
            VoidSurvival.logError("Failed to paste dungeon " + dungeonId + " in world " + world.getName() + " at " + x + ", " + y + ", " + z);
            return;
        }

        ProtectedCuboidRegion dungeonRegion = createOriginOffsetRegion(name + "_outer", origin, variables.outerCuboid);
        ProtectedCuboidRegion spawnRegion = createOriginOffsetRegion(name + "_spawn", origin, variables.spawnCuboid);
        ArrayList<ProtectedCuboidRegion> oreRegions = new ArrayList<>(variables.oreCuboids.size());

        for (int i = 0; i < variables.oreCuboids.size(); i++) {
            oreRegions.add(createOriginOffsetRegion(name + "_ore" + i, origin, variables.oreCuboids.get(i)));
        }

        RegionManager manager = WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .get(BukkitAdapter.adapt(world));

        if (manager == null) {
            VoidSurvival.logError("Could not get region manager for world " + world.getName());
            return;
        }

        manager.addRegion(dungeonRegion);
        variables.regionFlags.setFlags(dungeonRegion);
        manager.addRegion(spawnRegion);
        variables.spawnFlags.setFlags(spawnRegion);

        spawnRegion.setFlag(
            com.sk89q.worldguard.protection.flags.Flags.TELE_LOC,
            BukkitAdapter.adapt(origin.add(variables.teleportLocation).toLocation(world))
        );

        for (int i = 0; i < oreRegions.size(); i++) {
            ProtectedCuboidRegion oreRegion = oreRegions.get(i);
            manager.addRegion(oreRegion);
            variables.oreFlags.get(i).setFlags(oreRegion);
        }

        LootChestManager lootManager = VoidSurvival.getInstance().getLootTableManager();

        variables.lootChests.forEach(chest -> {
            lootManager.setChestLoot(origin.add(chest.x(), chest.y(), chest.z()).toLocation(world), chest.id());
        });
    }

    public static void saveDungeon(World world, int x, int y, int z, String outerDungeonRegionId) throws IOException, WorldEditException {

        RegionManager manager = WorldGuardUtil.getRegionManager(world);
        ProtectedRegion region = manager.getRegion(outerDungeonRegionId);

        if (region == null) {
            VoidSurvival.logError("Failed to save dungeon from region " + outerDungeonRegionId + ": region does not exist");
            return;
        }

        Clipboard clipboard = saveSchematic(Path.of("dungeons", outerDungeonRegionId + ".schem"),
            world, BlockVector3.at(x, y, z), region.getMinimumPoint(), region.getMaximumPoint()
        );

        ProtectedRegion spawnRegion = null;
        ArrayList<ProtectedRegion> oreRegions = new ArrayList<>();

        for (ProtectedRegion subRegion : manager.getApplicableRegions(region)) {
            if (subRegion.getFlag(Flags.REGENERATE_BLOCKS) != null) {
                oreRegions.add(subRegion);
            } else if (subRegion.getFlag(com.sk89q.worldguard.protection.flags.Flags.TELE_LOC) != null) {
                spawnRegion = subRegion;
            }
        }

        if (spawnRegion == null) {
            VoidSurvival.logError("Failed to save dungeon from region " + outerDungeonRegionId + ": region must contain a subregion with a teleport location set!");
            return;
        }

        Location tpLocation = spawnRegion.getFlag(com.sk89q.worldguard.protection.flags.Flags.TELE_LOC);

        if (tpLocation == null) {
            VoidSurvival.logError("Failed to save dungeon from region " + outerDungeonRegionId + ": region must contain a subregion with a teleport location set!");
            return;
        }

        BlockVector3 sqOrigin = clipboard.getOrigin();
        BlockPosition origin = new BlockPosition(sqOrigin.x(), sqOrigin.y(), sqOrigin.z());

        DungeonVariables variables = new DungeonVariables();

        variables.schematic = outerDungeonRegionId + ".schem";
        variables.teleportLocation = new Position(
            tpLocation.getX() - origin.x(), tpLocation.getY() - origin.y(), tpLocation.getZ() - origin.z(),
            tpLocation.getYaw(), tpLocation.getPitch()
        );
        variables.regionFlags = DungeonVariables.RegionFlags.ofRegion(spawnRegion);
        variables.outerCuboid = createOffsetCuboid(origin, region.getMinimumPoint(), region.getMaximumPoint());
        variables.spawnCuboid = createOffsetCuboid(origin, spawnRegion.getMinimumPoint(), spawnRegion.getMaximumPoint());
        variables.spawnFlags = DungeonVariables.RegionFlags.ofRegion(spawnRegion);

        for (ProtectedRegion oreRegion : oreRegions) {
            variables.oreCuboids.add(createOffsetCuboid(origin, oreRegion.getMinimumPoint(), oreRegion.getMaximumPoint()));
            variables.oreFlags.add(DungeonVariables.RegionFlags.ofRegion(oreRegion));
        }

        LootChestManager lootManager = VoidSurvival.getInstance().getLootTableManager();

        WorldUtil.getTileEntitiesInRegion(world, new Cuboid(
                BukkitAdapter.adapt(world, region.getMinimumPoint()),
                BukkitAdapter.adapt(world, region.getMaximumPoint())
            ), tile -> tile.getState() instanceof Chest)
            .forEach(state -> {

                BlockPosition point = createOffsetPosition(origin, BlockPosition.ofBlock(state.getBlock()));
                String tableId = lootManager.getTableId(state.getLocation());

                variables.lootChests.add(new DungeonVariables.ChestLocation(tableId, point.x(), point.y(), point.z()));
            });

        try {

            File dungeonFile = VoidSurvival.getInstance().getFile("dungeons", outerDungeonRegionId + ".json");

            Json.writeToFileSafeAndPretty(dungeonFile, variables);

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to save dungeon from region " + outerDungeonRegionId + ": ", ex);
        }

    }

    private static BlockPosition createOffsetPosition(BlockPosition origin, BlockVector3 point) {
        return new BlockPosition(point.x() - origin.x(), point.y() - origin.y(), point.z() - origin.z());
    }

    private static BlockPosition createOffsetPosition(BlockPosition origin, BlockPosition point) {
        return new BlockPosition(point.x() - origin.x(), point.y() - origin.y(), point.z() - origin.z());
    }

    private static Cuboid createOffsetCuboid(BlockPosition origin, BlockVector3 min, BlockVector3 max) {
        return new Cuboid(
            createOffsetPosition(origin, min),
            createOffsetPosition(origin, max)
        );
    }

    private static ProtectedCuboidRegion createOriginOffsetRegion(String id, BlockPosition origin, Cuboid cuboid) {
        return WorldGuardUtil.createRegion(id,
            origin.add(cuboid.getMin()),
            origin.add(cuboid.getMax())
        );
    }




}
