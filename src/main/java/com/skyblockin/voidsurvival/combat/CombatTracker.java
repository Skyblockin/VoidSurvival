package com.skyblockin.voidsurvival.combat;

import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CombatTracker implements Listener {

    @EventHandler
    public void onKill(PlayerDeathEvent event) {

        DamageSource source = event.getDamageSource();

        if (source.getCausingEntity() instanceof Player player) {
            PlayerData data = PlayerData.of(player);
            data.kills++;
            data.killStreak++;

            if (data.killStreak % 10 == 0) {
                Bukkit.broadcast(TextUtil.color("%s has reached a kill streak of %s!", player.getName(), data.killStreak));
            }
        }

        PlayerData data = PlayerData.of(event.getPlayer());

        if (data.killStreak >= 10 && source.getCausingEntity() instanceof Player player) {
            Bukkit.broadcast(TextUtil.color("%s has ended %s's kill streak of %d!", player.getName(), event.getPlayer().getName(), data.killStreak));
        }

        data.killStreak = 0;
    }

}
