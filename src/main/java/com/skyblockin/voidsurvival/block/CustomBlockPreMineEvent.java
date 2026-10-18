package com.skyblockin.voidsurvival.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomBlockPreMineEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Block block;
    private final CustomBlock customBlock;
    private boolean cancelled;

    public CustomBlockPreMineEvent(@NotNull Player player, Block block, CustomBlock customBlock) {
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
        return new CustomBlockPreMineEvent(player, block, customBlock).callEvent();
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
