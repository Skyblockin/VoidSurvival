package com.skyblockin.voidsurvival.trigger.triggerable;

import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.TriggerManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public record Title(
    String id,
    Component title,
    Component subtitle,
    int fadeInTicks,
    int stayTicks,
    int fadeOutTicks
) implements Triggerable {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void trigger(TriggerManager manager, Player player, PlayerData data) {
        player.showTitle(net.kyori.adventure.title.Title.title(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks));
    }
}
