package com.skyblockin.voidsurvival.constants;

import com.skyblockin.voidsurvival.util.UnitParser;

public class Units {

    public static final UnitParser TIME = new UnitParser("ms")
        .ofPrevious("s", 1000)
        .ofPrevious("m", 60)
        .ofPrevious("h", 60)
        .ofPrevious("d", 24)
        .ofUnit("mo", 30, "d")
        .ofUnit("y", 365, "d");

}
