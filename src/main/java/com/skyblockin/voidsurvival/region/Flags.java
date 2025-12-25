package com.skyblockin.voidsurvival.region;

import com.sk89q.worldguard.WorldGuard;
import com.skyblockin.voidsurvival.VoidSurvival;

public final class Flags {

    public static final RegionStateFlag REGENERATE_BLOCKS = new RegionStateFlag("regenerate", false);
    public static final MobCapFlag MOB_CAPS = new MobCapFlag("mob-caps");

    public static void registerAll() {
        WorldGuard.getInstance().getFlagRegistry().register(MOB_CAPS);
        VoidSurvival.logInfo("Registered all region flags");
    }

}
