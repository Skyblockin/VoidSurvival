package com.skyblockin.voidsurvival.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class RegionStateFlag {

    private final StateFlag flag;

    public RegionStateFlag(String name, boolean defaultValue) {
        this.flag = new StateFlag(name, defaultValue);
        WorldGuard.getInstance().getFlagRegistry().register(this.flag);
    }

    public boolean test(Player player, Location location) {
        return WorldGuard.getInstance()
            .getPlatform()
            .getRegionContainer()
            .createQuery()
            .testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), this.flag);
    }

    public boolean test(Player player, Block block) {
        return test(player, block.getLocation());
    }

}
