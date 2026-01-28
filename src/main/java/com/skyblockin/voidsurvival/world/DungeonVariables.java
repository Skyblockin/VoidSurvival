package com.skyblockin.voidsurvival.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.math.Cuboid;
import com.skyblockin.voidsurvival.math.Position;
import com.skyblockin.voidsurvival.region.Flags;
import com.skyblockin.voidsurvival.region.MobCapMap;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DungeonVariables {

    public String schematic;
    public Cuboid outerCuboid;
    public Cuboid spawnCuboid;
    public ArrayList<Cuboid> oreCuboids = new ArrayList<>();
    public RegionFlags spawnFlags = new RegionFlags();
    public RegionFlags regionFlags = new RegionFlags();
    public ArrayList<RegionFlags> oreFlags = new ArrayList<>();
    public ArrayList<ChestLocation> lootChests = new ArrayList<>();

    public Position teleportLocation;

    public static class RegionFlags {

        public static RegionFlags ofRegion(ProtectedRegion region) {

            RegionFlags flags = new RegionFlags();

            region.getFlags().forEach((flag, value) -> {
                if (flag instanceof StateFlag stateFlag) {
                    flags.stateFlags.put(stateFlag.getName(), stateToBoolean(value));
                }
            });

            flags.mobCapMap = region.getFlag(Flags.MOB_CAPS);
            flags.blockedCommands = region.getFlag(com.sk89q.worldguard.protection.flags.Flags.BLOCKED_CMDS);

            return flags;
        }

        private static boolean stateToBoolean(Object object) {
            StateFlag.State state = (StateFlag.State) object;
            return switch (state) {
                case ALLOW -> true;
                case DENY -> false;
            };
        }

        public HashMap<String, Boolean> stateFlags = new HashMap<>();
        public Set<String> blockedCommands = null;
        public MobCapMap mobCapMap = null;

        public void setFlags(ProtectedRegion region) {

            Map<Flag<?>, Object> flags = new HashMap<>();

            stateFlags.keySet().forEach(key -> {
                flags.put(WorldGuardUtil.getStateFlag(key), getFlagState(key));
            });

            if (blockedCommands != null) {
                flags.put(com.sk89q.worldguard.protection.flags.Flags.BLOCKED_CMDS, blockedCommands);
            }

            if (mobCapMap != null) {
                flags.put(Flags.MOB_CAPS, mobCapMap);
            }

            region.setFlags(flags);
        }

        @JsonIgnore
        public StateFlag.State getFlagState(String flag) {

            Boolean value = stateFlags.get(flag);

            if (value == null) {
                return null;
            }

            return value ? StateFlag.State.ALLOW : StateFlag.State.DENY;
        }

    }

    public record ChestLocation(String id, int x, int y, int z) {}

}
