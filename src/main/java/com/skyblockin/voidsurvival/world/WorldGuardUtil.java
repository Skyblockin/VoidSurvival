package com.skyblockin.voidsurvival.world;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Set;

public class WorldGuardUtil {

    public static RegionQuery createRegionQuery() {
        return WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .createQuery();
    }

    public static Set<ProtectedRegion> getTopRegionsAt(Location location) {
        return createRegionQuery()
            .getApplicableRegions(
                BukkitAdapter.adapt(location), RegionQuery.QueryOption.SORT
            ).getRegions();
    }

    public static <T> T getFlagValueAt(Location location, Flag<T> flag) {
        return createRegionQuery()
            .getApplicableRegions(BukkitAdapter.adapt(location))
            .queryValue(null, flag);
    }

    public static ProtectedRegion getTopRegionAt(Location location) {
        return getTopRegionsAt(location)
            .stream()
            .max(Comparator.comparingInt(ProtectedRegion::getPriority))
            .orElse(null);
    }

    public static boolean testFlag(Location location, Player player, StateFlag... flags) {

        LocalPlayer localPlayer = null;

        if (player != null) {
            localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        }

        return createRegionQuery().testState(
            BukkitAdapter.adapt(location), localPlayer, flags
        );
    }

}
