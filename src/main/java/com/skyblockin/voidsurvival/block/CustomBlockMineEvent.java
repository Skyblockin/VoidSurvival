package com.skyblockin.voidsurvival.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomBlockMineEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Block block;
    private final CustomBlock customBlock;

    public CustomBlockMineEvent(@NotNull Player player, Block block, CustomBlock customBlock) {
        this.player = player;
        this.block = block;
        this.customBlock = customBlock;
    }

    public Player getPlayer() {
        return player;
    }

    public Block getBlock() {
        return block;
    }

    public @Nullable CustomBlock getCustomBlock() {
        return customBlock;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static boolean callEvent(Player player, Block block, CustomBlock customBlock) {
        return new CustomBlockMineEvent(player, block, customBlock).callEvent();
    }
}
