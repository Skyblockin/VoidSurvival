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
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
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

    public static ProtectedCuboidRegion createRegion(String regionId, BlockPosition min, BlockPosition max) {
        return new ProtectedCuboidRegion(regionId, BlockVector3.at(min.x(), min.y(), min.z()), BlockVector3.at(max.x(), max.y(), max.z()));
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

    public static StateFlag getStateFlag(String name) {

        Flag<?> flag = WorldGuard.getInstance().getFlagRegistry().get(name);

        if (flag instanceof StateFlag stateFlag) {
            return stateFlag;
        }

        return null;
    }

    public static void setStateFlag(ProtectedRegion region, String name, boolean value) {

        StateFlag flag = WorldGuardUtil.getStateFlag(name);

        if (flag == null) {
            VoidSurvival.logError("Failed to get state flag with name " + name + " because it is either not a state flag or does not exist");
            return;
        }

        region.setFlag(flag, value ? StateFlag.State.ALLOW : StateFlag.State.DENY);
    }

    public static RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .get(BukkitAdapter.adapt(world));
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
