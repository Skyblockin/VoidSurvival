package com.skyblockin.voidsurvival.trigger;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class StatGainTrigger<T extends Event> implements Trigger<T> {

    private final String id;
    private final Requirements requirements;

    public StatGainTrigger(String id, Requirements requirements) {
        this.id = id;
        this.requirements = requirements;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public abstract void activate(TriggerManager manager, T event);

    @Override
    public Requirements getRequirements() {
        return requirements;
    }

    public void onPlayerLeave(PlayerQuitEvent event) {}

    public void onPlayerJoin(PlayerJoinEvent event) {}
}
