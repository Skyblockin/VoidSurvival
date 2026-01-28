package com.skyblockin.voidsurvival.world;

import com.skyblockin.voidsurvival.math.Cuboid;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class WorldUtil {

    public static List<BlockState> getTileEntitiesInRegion(World world, Cuboid cuboid, Predicate<Block> tileStateFunction) {

        Location min = cuboid.getMin().toLocation(world);
        Location max = cuboid.getMax().toLocation(world);

        Chunk minChunk = min.getChunk();
        Chunk maxChunk = max.getChunk();

        List<BlockState> tileEntities = new ArrayList<>();

        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                tileEntities.addAll(world.getChunkAt(x, z, false).getTileEntities(block -> tileStateFunction.test(block)
                    && cuboid.isWithinBounds(block.getX(), block.getY(), block.getZ()), false
                ));
            }
        }

        return tileEntities;
    }

}
