package com.skyblockin.voidsurvival.trigger.triggerable;

import com.skyblockin.voidsurvival.config.RangedValue;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.TriggerManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;

public record ItemGive(
    String id,
    ItemType type,
    RangedValue range
) implements Triggerable {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void trigger(TriggerManager manager, Player player, PlayerData data) {
        player.getInventory().addItem(type.createItemStack(range.get()));
    }
}
