package com.skyblockin.voidsurvival.trigger.triggerable;

import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.TriggerManager;
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
        for (String triggerableId : triggerableIds) {
            if (this.id.equals(triggerableId)) continue;
            manager.trigger(triggerableId, player, data);
        }
    }
}
