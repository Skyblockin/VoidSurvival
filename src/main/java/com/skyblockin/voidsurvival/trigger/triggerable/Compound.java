package com.skyblockin.voidsurvival.trigger.triggerable;

import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.TriggerManager;
import com.skyblockin.voidsurvival.trigger.TriggerSequence;
import org.bukkit.entity.Player;

import java.util.List;

public record Compound(
    String id, List<String> triggerableIds
) implements Triggerable {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void trigger(TriggerManager manager, Player player, PlayerData data) {
        TriggerSequence.runSequence(triggerableIds, manager, player, data);
    }

    public void triggerWithCallback(TriggerManager manager, Player player, PlayerData data, Runnable onComplete) {
        TriggerSequence.runSequence(triggerableIds, manager, player, data, onComplete);
    }

}
