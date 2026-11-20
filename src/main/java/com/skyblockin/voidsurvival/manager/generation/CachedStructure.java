package com.skyblockin.voidsurvival.manager.generation;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;

public record CachedStructure(Clipboard clipboard, int[][] columnCounts) {

    public static CachedStructure fromClipboard(Clipboard clipboard) {

        BlockVector3 min = clipboard.getMinimumPoint();
        BlockVector3 max = clipboard.getMaximumPoint();

        int sizeX = max.x() - min.x() + 1;
        int sizeZ = max.z() - min.z() + 1;
        int[][] counts = new int[sizeX][sizeZ];

        for (int clipX = min.x(); clipX <= max.x(); clipX++) {
            int arrayX = clipX - min.x();

            for (int clipZ = min.z(); clipZ <= max.z(); clipZ++) {
                int arrayZ = clipZ - min.z();
                int nonAir = 0;

                for (int clipY = min.y(); clipY <= max.y(); clipY++) {
                    if (!clipboard.getBlock(BlockVector3.at(clipX, clipY, clipZ)).getBlockType().getMaterial().isAir()) {
                        nonAir++;
                    }
                }
                counts[arrayX][arrayZ] = nonAir;
            }
        }

        return new CachedStructure(clipboard, counts);
    }
}

