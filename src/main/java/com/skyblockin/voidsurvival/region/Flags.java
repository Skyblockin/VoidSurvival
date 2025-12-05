package com.skyblockin.voidsurvival.region;

import com.skyblockin.voidsurvival.VoidSurvival;

public final class Flags {

    public static final RegionStateFlag REGENERATE_BLOCKS = new RegionStateFlag("regenerate", false);

    public static void registerAll() {
        VoidSurvival.logInfo("Registered all region flags");
    }

}
