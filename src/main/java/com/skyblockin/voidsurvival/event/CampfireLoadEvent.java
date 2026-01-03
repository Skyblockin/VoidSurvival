package com.skyblockin.voidsurvival.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CampfireLoadEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final String campfireId;
    private final Location campfireLocation;

    public CampfireLoadEvent(Player loader, String campfireId, Location campfireLocation) {
        this.player = loader;
        this.campfireId = campfireId;
        this.campfireLocation = campfireLocation;
    }

    public Player getPlayer() {
        return player;
    }

    public String getId() {
        return campfireId;
    }

    public Location getLocation() {
        return campfireLocation;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static void callEvent(Player player, String campfireId, Location campfireLocation) {
        new CampfireLoadEvent(player, campfireId, campfireLocation).callEvent();
    }
}
