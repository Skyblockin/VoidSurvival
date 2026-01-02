package com.skyblockin.voidsurvival.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class RegionStateFlag extends StateFlag {

    public RegionStateFlag(String name, boolean defaultValue) {
        super(name, defaultValue);
        WorldGuard.getInstance().getFlagRegistry().register(this);
    }

    public boolean test(Player player, Location location) {
        return WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .createQuery()
            .testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), this);
    }

    public boolean test(Player player, Block block) {
        return test(player, block.getLocation());
    }

}
