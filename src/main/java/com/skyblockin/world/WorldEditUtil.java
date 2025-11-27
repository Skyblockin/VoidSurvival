package com.skyblockin.world;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.skyblockin.VoidSurvival;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;

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

}
