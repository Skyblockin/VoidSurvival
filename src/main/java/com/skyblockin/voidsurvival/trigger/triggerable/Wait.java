package com.skyblockin.voidsurvival.trigger.triggerable;

import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.TriggerManager;
import org.bukkit.entity.Player;

public record Wait(
    String id,
    int ticks
) implements Triggerable {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void trigger(TriggerManager manager, Player player, PlayerData data) {
    }
}
