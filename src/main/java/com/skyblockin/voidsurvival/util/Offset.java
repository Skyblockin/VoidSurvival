package com.skyblockin.voidsurvival.util;

import org.bukkit.block.Block;

public class Offset {

    public static Offset TOP_NORTH_WEST =    new Offset(-1, +1, +1);
    public static Offset TOP_NORTH =         new Offset(+0, +1, +1);
    public static Offset TOP_NORTH_EAST =    new Offset(+1, +1, +1);
    public static Offset TOP_WEST =          new Offset(-1, +1, +0);
    public static Offset TOP_MIDDLE =        new Offset(+0, +1, +0);
    public static Offset TOP_EAST =          new Offset(+1, +1, +0);
    public static Offset TOP_SOUTH_WEST =    new Offset(-1, +1, -1);
    public static Offset TOP_SOUTH =         new Offset(+0, +1, -1);
    public static Offset TOP_SOUTH_EAST =    new Offset(+1, +1, -1);
    public static Offset MIDDLE_NORTH_WEST = new Offset(-1, +0, +1);
    public static Offset MIDDLE_NORTH =      new Offset(+0, +0, +1);
    public static Offset MIDDLE_NORTH_EAST = new Offset(+1, +0, +1);
    public static Offset MIDDLE_WEST =       new Offset(-1, +0, +0);
    public static Offset MIDDLE_EAST =       new Offset(+1, +0, +0);
    public static Offset MIDDLE_SOUTH_WEST = new Offset(-1, +0, -1);
    public static Offset MIDDLE_SOUTH =      new Offset(+0, +0, -1);
    public static Offset MIDDLE_SOUTH_EAST = new Offset(+1, +0, -1);
    public static Offset BOTTOM_NORTH_WEST = new Offset(-1, -1, +1);
    public static Offset BOTTOM_NORTH =      new Offset(+0, -1, +1);
    public static Offset BOTTOM_NORTH_EAST = new Offset(+1, -1, +1);
    public static Offset BOTTOM_WEST =       new Offset(-1, -1, +0);
    public static Offset BOTTOM_MIDDLE =     new Offset(+0, -1, +0);
    public static Offset BOTTOM_EAST =       new Offset(+1, -1, +0);
    public static Offset BOTTOM_SOUTH_WEST = new Offset(-1, -1, -1);
    public static Offset BOTTOM_SOUTH =      new Offset(+0, -1, -1);
    public static Offset BOTTOM_SOUTH_EAST = new Offset(+1, -1, -1);

    public static final Offset[] values = {
            TOP_NORTH_WEST,    TOP_NORTH,         TOP_NORTH_EAST,
            TOP_WEST,          TOP_MIDDLE,        TOP_EAST,
            TOP_SOUTH_WEST,    TOP_SOUTH,         TOP_SOUTH_EAST,
            MIDDLE_NORTH_WEST, MIDDLE_NORTH,      MIDDLE_NORTH_EAST,
            MIDDLE_WEST,                          MIDDLE_EAST,
            MIDDLE_SOUTH_WEST, MIDDLE_SOUTH,      MIDDLE_SOUTH_EAST,
            BOTTOM_NORTH_WEST, BOTTOM_NORTH,      BOTTOM_NORTH_EAST,
            BOTTOM_WEST,       BOTTOM_MIDDLE,     BOTTOM_EAST,
            BOTTOM_SOUTH_WEST, BOTTOM_SOUTH,      BOTTOM_SOUTH_EAST
    };

    public static Offset[] values() {
        return values;
    }

    private final int x, y, z;

    public Offset(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block getBlock(Block block) {
        return block.getRelative(x, y, z);
    }

    public Offset add(int add) {
        return new Offset(x + add, y + add, z + add);
    }

    public Offset subtract(int subtract) {
        return new Offset(x - subtract, y - subtract, z - subtract);
    }

    public Offset multiply(int multiplier) {
        return new Offset(x * multiplier, y * multiplier, z * multiplier);
    }

}
