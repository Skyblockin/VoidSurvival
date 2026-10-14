package com.skyblockin.voidsurvival.trigger.triggerable;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.TriggerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public record Teleport(
    String id,
    Location location,
    int delayTicks
) implements Triggerable {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void trigger(TriggerManager manager, Player player, PlayerData data) {
        VoidSurvival.getInstance().runTaskLater(() -> {
            player.teleportAsync(location);
        }, delayTicks);
    }
}
