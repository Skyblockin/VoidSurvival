package com.skyblockin.voidsurvival.manager.generation;

import com.skyblockin.voidsurvival.math.BlockPosition;
import org.bukkit.util.BoundingBox;

public class Structure {

    public final String name;
    public final BlockPosition min;
    public final BlockPosition max;
    public int blocks;

    public Structure(String name, BlockPosition min, BlockPosition max, int blocks) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.blocks = blocks;
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(min.x(), min.y(), min.z(), max.x() + 1, max.y() + 1, max.z() + 1);
    }

}
