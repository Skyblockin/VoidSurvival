package com.skyblockin.voidsurvival.storage;

import org.bukkit.block.BlockType;

import java.util.ArrayList;

public class MissingBlockData {

    private final BlockType type;
    private final ArrayList<BlockPosition> blocks = new ArrayList<>();

    public MissingBlockData(BlockType type) {
        this.type = type;
    }

    public BlockType getType() {
        return type;
    }

    public void addBlock(int x, int y, int z) {
        this.blocks.add(new BlockPosition(x, y, z));
    }

    public ArrayList<BlockPosition> getBlocks() {
        return this.blocks;
    }

}
