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
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.math.Cuboid;
import com.skyblockin.voidsurvival.math.Position;
import com.skyblockin.voidsurvival.region.Flags;
import com.skyblockin.voidsurvival.util.FileUtil;
import net.kyori.adventure.text.BlockNBTComponent;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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

            ClipboardReader reader = format.getReader(new FileInputStream(file));

            return reader.read();

        } catch (Exception ex) {
            VoidSurvival.logError("Caught error while loading schematic '%s': %s", file.getName(), ex.getMessage());
            return null;
        }
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

    public static void pasteDungeonAt(String dungeonId, World world, int x, int y, int z) {

        DungeonVariables variables;

        try {
            Path path = FileUtil.createOrGetFile(Path.of("dungeons", dungeonId + ".json"));
            variables = Json.readFromFile(path.toFile(), DungeonVariables.class);
        } catch (Exception ex) {
            VoidSurvival.logError("Failed to read config file from dungeons/" + dungeonId + ".json", ex);
            return;
        }

        Clipboard clipboard = loadSchematic(VoidSurvival.getInstance().getFile("schematics", variables.schematic));

        if (clipboard == null) {
            return;
        }

        try {
            pasteClipboardAt(clipboard, world, x, y, z, true);
        } catch (Exception ex) {
            VoidSurvival.logError("Failed to paste dungeon " + dungeonId + " in world " + world.getName() + " at " + x + ", " + y + ", " + z);
            return;
        }

        BlockVector3 origin = clipboard.getOrigin();
        BlockVector3 min = clipboard.getMinimumPoint();
        BlockVector3 max = clipboard.getMaximumPoint();

        ProtectedCuboidRegion dungeonRegion = new ProtectedCuboidRegion(
            dungeonId + "_outer",
            BlockVector3.at(min.x() + x, Math.clamp(min.y() + y, world.getMinHeight(), world.getMaxHeight()), min.z() + z),
            BlockVector3.at(max.x() + x, Math.clamp(max.y() + y, world.getMinHeight(), world.getMaxHeight()), max.z() + z)
        );

        ProtectedCuboidRegion spawnRegion = createOriginOffsetRegion(dungeonId + "_spawn", origin, variables.spawnCuboid.add(x, y, z));
        ArrayList<ProtectedCuboidRegion> oreRegions = new ArrayList<>(variables.oreCuboids.size());

        for (int i = 0; i < variables.oreCuboids.size(); i++) {
            oreRegions.add(createOriginOffsetRegion(dungeonId + "_ore" + i, origin, variables.oreCuboids.get(i)));
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

        for (int i = 0; i < oreRegions.size(); i++) {
            ProtectedCuboidRegion oreRegion = oreRegions.get(i);
            manager.addRegion(oreRegion);
            variables.oreFlags.get(i).setFlags(oreRegion);
        }
    }

    private static ProtectedCuboidRegion createOriginOffsetRegion(String id, BlockVector3 origin, Cuboid cuboid) {

        BlockPosition min = cuboid.getMin().add(origin.x(), origin.y(), origin.z());
        BlockPosition max = cuboid.getMax().add(origin.x(), origin.y(), origin.z());

        return WorldGuardUtil.createRegion(id, min, max);
    }

    public static void saveSchematic(Path path, World world, BlockVector3 min, BlockVector3 max) throws IOException, WorldEditException {

        CuboidRegion selection = new CuboidRegion(min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(selection);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
            BukkitAdapter.adapt(world), selection, clipboard, min
        );

        Operations.complete(forwardExtentCopy);

        File schematicFile = VoidSurvival.getInstance().getFile(path);

        if (!schematicFile.exists()) {
            Files.createDirectory(schematicFile.getParentFile().toPath());
            Files.createFile(schematicFile.toPath());
        }

        ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(schematicFile));
        writer.write(clipboard);

    }

    public static void saveDungeon(World world, String outerDungeonRegionId) {

        RegionManager manager = WorldGuardUtil.getRegionManager(world);
        ProtectedRegion region = manager.getRegion(outerDungeonRegionId);

        if (region == null) {
            VoidSurvival.logError("Failed to save dungeon from region " + outerDungeonRegionId + ": region does not exist");
            return;
        }

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

        Location location = spawnRegion.getFlag(com.sk89q.worldguard.protection.flags.Flags.TELE_LOC);

        if (location == null) {
            VoidSurvival.logError("Failed to save dungeon from region " + outerDungeonRegionId + ": region must contain a subregion with a teleport location set!");
            return;
        }

        BlockPosition origin = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        DungeonVariables variables = new DungeonVariables();

        variables.teleportLocation = new Position(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        variables.regionFlags = DungeonVariables.RegionFlags.ofRegion(spawnRegion);
        variables.spawnCuboid = createOffsetCuboid(origin, spawnRegion.getMinimumPoint(), spawnRegion.getMaximumPoint());
        variables.spawnFlags = DungeonVariables.RegionFlags.ofRegion(spawnRegion);

        for (ProtectedRegion oreRegion : oreRegions) {
            variables.oreCuboids.add(createOffsetCuboid(origin, oreRegion.getMinimumPoint(), oreRegion.getMaximumPoint()));
            variables.oreFlags.add(DungeonVariables.RegionFlags.ofRegion(oreRegion));
        }

        try {

            saveSchematic(Path.of("dungeons", outerDungeonRegionId + ".schem"), world, region.getMinimumPoint(), region.getMaximumPoint());
            File dungeonFile = VoidSurvival.getInstance().getFile("dungeons", outerDungeonRegionId + ".json");

            Json.writeToFileSafe(dungeonFile, variables);

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to save dungeon from region " + outerDungeonRegionId + ": ", ex);
        }

    }

    private static Cuboid createOffsetCuboid(BlockPosition origin, BlockVector3 min, BlockVector3 max) {
        return new Cuboid(
            origin.x() + min.x(), origin.y() + min.y(), origin.z() + min.z(),
            origin.x() + max.x(), origin.y() + max.y(), origin.z() + max.z()
        );
    }




}
