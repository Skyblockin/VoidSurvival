package com.skyblockin.voidsurvival.trigger.triggerable;

import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.TriggerManager;
import org.bukkit.entity.Player;

public interface Triggerable {

    String getId();

    void trigger(TriggerManager manager, Player player, PlayerData data);

}
