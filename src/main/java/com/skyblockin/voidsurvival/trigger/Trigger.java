package com.skyblockin.voidsurvival.trigger;

import org.bukkit.event.Event;

public interface Trigger<T extends Event> {

    String getId();

    void activate(TriggerManager manager, T event);

    Requirements getRequirements();

}
