package com.skyblockin.voidsurvival.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skyblockin.voidsurvival.world.WorldGuardUtil;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.util.BoundingBox;

public class RegionFlagListener implements Listener {

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {

        EntityType type = event.getEntityType();
        Location location = event.getLocation();

        ApplicableRegionSet set = WorldGuardUtil.createRegionQuery()
            .getApplicableRegions(BukkitAdapter.adapt(location));

        int highestCap = Integer.MIN_VALUE;
        ProtectedRegion topRegion = null;

        // Find the highest mob cap for the entity type in the regions
        // that the entity is being spawned in
        for (ProtectedRegion region : set) {

            MobCapMap map = region.getFlag(Flags.MOB_CAPS);

            if (map != null) {
                int value = map.getOrDefault(type, -1);
                if (value > highestCap) {
                    highestCap = value;
                    topRegion = region;
                }
            }
        }

        // Check if any cap was found and if the entity type has hit the cap
        if (highestCap > 0 && countEntitiesInRegion(location, topRegion, type) >= highestCap) {
            event.setCancelled(true);
        }
    }

    private int countEntitiesInRegion(Location location, ProtectedRegion region, EntityType type) {

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        return location.getWorld()
            .getNearbyEntities(new BoundingBox(min.x(), min.y(), min.z(), max.x(), max.y(), max.z()),
                entity -> entity.getType() == type
            )
            .size();
    }

}
