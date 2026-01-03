package com.skyblockin.voidsurvival.event;

import com.skyblockin.voidsurvival.VoidSurvival;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class CampfireClickEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Block block;
    private final String campfireId;

    public CampfireClickEvent(@NotNull Player player, Block block, String campfireId) {
        super(player);
        this.block = block;
        this.campfireId = campfireId;
    }

    public Block getBlock() {
        return block;
    }

    public String getId() {
        return campfireId;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static void callEvent(Player player, Block block, String campfireId) {
        new CampfireClickEvent(player, block, campfireId).callEvent();
    }
}
