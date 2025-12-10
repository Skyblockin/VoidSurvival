package com.skyblockin.voidsurvival.combat;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.constants.DamageTypes;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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

        // Funny
        if (source.getDamageType().equals(DamageTypes.GREED)) {
            event.deathMessage(Component.text(event.getPlayer().getName() + " got too greedy.", TextColor.color(0xCC0000)));
            event.deathScreenMessageOverride(Component.text("You got too greedy.", TextColor.color(0xCC0000)));
        }

        if (source.getDamageType().equals(DamageTypes.BLEED)) {
            event.deathMessage(Component.text(event.getPlayer().getName() + " bled to death.", TextColor.color(0xCC0000)));
            event.deathScreenMessageOverride(Component.text("You bled to death.", TextColor.color(0xCC0000)));
        }

        // Don't allow a player to get a killstreak on themselves lol
        if (source.getCausingEntity() instanceof Player player && !player.equals(event.getPlayer())) {

            PlayerData data = PlayerData.of(player);
            data.kills++;
            data.killStreak++;

            if (data.killStreak % 10 == 0) {
                Bukkit.broadcast(TextUtil.color("%s has reached a kill streak of %s!", player.getName(), data.killStreak));
            }
        }

        // But definitely allow them to reset their own killstreak and shame them for it!
        PlayerData data = PlayerData.of(event.getPlayer());

        if (data.killStreak >= 10 && source.getCausingEntity() instanceof Player player) {
            Bukkit.broadcast(TextUtil.color("%s has ended %s's kill streak of %d!", player.getName(), event.getPlayer().getName(), data.killStreak));
        }

        data.killStreak = 0;
    }

}
