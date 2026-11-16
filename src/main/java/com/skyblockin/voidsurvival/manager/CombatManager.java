package com.skyblockin.voidsurvival.manager;

import com.skyblockin.voidsurvival.combat.DamageTypes;
import com.skyblockin.voidsurvival.combat.StanceManager;
import com.skyblockin.voidsurvival.combat.StatusManager;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.combat.status.BleedStatusManager;
import com.skyblockin.voidsurvival.item.ItemIds;
import com.skyblockin.voidsurvival.message.MessageKeys;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.TagUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.registry.keys.tags.EntityTypeTagKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CombatManager extends Manager implements Listener {

    private final HashMap<UUID, Instant> lastDamageTimes = new HashMap<>();
    private final HashMap<UUID, Instant> lastEntityHitTimes = new HashMap<>();

    private final List<StatusManager> statusManagers;
    private final StanceManager stanceManager;
    private final VoidSurvival plugin;

    public CombatManager(VoidSurvival plugin) {
        this.plugin = plugin;
        Server server = plugin.getServer();

        this.stanceManager = new StanceManager();
        this.statusManagers = List.of(
            new BleedStatusManager()
        );

        plugin.runTaskTimer(() -> {
            if (!isEnabled()) return;
            server.getOnlinePlayers().forEach(player -> {

                ItemStack item = player.getEquipment().getHelmet();

                if (!item.isEmpty() && Accessors.ITEM_ID.equals(item, ItemIds.AVARITIA)) {
                    player.damage(4, DamageSource.builder(DamageTypes.GREED).build());
                }
            });
        }, 0, 20L);

        // Tick all status effects every half a second
        plugin.runTaskTimer(() -> {
            if (!isEnabled()) return;
            for (StatusManager manager : statusManagers) {
                manager.tickStatus(lastDamageTimes);
            }
        }, 0, 10L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!isEnabled()) return;
        if (TagUtil.isTagged(EntityTypeTagKeys.ARROWS, event.getEntity().getType())) {
            event.getEntity().remove();
        }
    }

    @EventHandler
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {

        if (!isEnabled()) return;

        Player player = event.getPlayer();

        if (player.isOp()) {
            return;
        }

        String command = event.getMessage();

        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        int firstSpace = command.indexOf(' ');
        command = command.substring(0, firstSpace == -1 ? command.length() : firstSpace);

        if (isCombatTagged(player) && isCommandBlockedInCombat(command)) {
            player.sendMessage(TextUtil.message(MessageKeys.COMBAT_COMMAND_BLOCKED_IN_COMBAT));
            event.setCancelled(true);
        }
    }

    public boolean isCommandBlockedInCombat(String command) {
        return plugin.getCombatBlockedCommands().contains(command);
    }

    public boolean isCombatTagged(Player player) {

        int combatTagDurationSeconds = plugin.getCombatTagDurationSeconds();
        Instant lastHitTime = lastEntityHitTimes.get(player.getUniqueId());

        return lastHitTime != null && lastHitTime.plusSeconds(combatTagDurationSeconds).isAfter(Instant.now());
    }

    public boolean isStanceBroken(LivingEntity entity) {
        return this.stanceManager.isStanceBroken(entity);
    }

    public StanceManager getStanceManager() {
        return stanceManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isEnabled()) return;
        lastEntityHitTimes.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (!isEnabled()) return;

        // Don't allow friends to attack each other
        if (event.getDamager() instanceof Player first && event.getEntity() instanceof Player other) {
            if (plugin.getFriendManager().testFriendship(first, other)) {
                event.setCancelled(true);
                return;
            }
        }

        // lastEntityHitTimes is to prevent stuff such as executing commands in combat, so it should only apply to players
        if (event.getEntity() instanceof Player player) {
            this.lastEntityHitTimes.put(player.getUniqueId(), Instant.now());
        }

        if (event.getDamager() instanceof Player player) {
            this.lastEntityHitTimes.put(player.getUniqueId(), Instant.now());
        }

        // Don't allow indirect attacks to build up status effects
        if (event.getDamageSource().isIndirect()
            || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
            || event.getDamageSource().getDamageType().equals(DamageTypes.BLEED)
        ) {
            return;
        }

        if (event.getDamager() instanceof LivingEntity damager && event.getEntity() instanceof LivingEntity target) {

            // Only enemies have a controllable stance for now
            if (!(target instanceof Player)) {
                this.stanceManager.updateStance(damager, target);
            }

            // Also known as the last time the entity was damaged by another entity
            this.lastDamageTimes.put(target.getUniqueId(), Instant.now());

            for (StatusManager manager : this.statusManagers) {
                manager.updateStatus(target, damager, event);
            }
        }

    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {

        if (!isEnabled()) return;

        DamageSource source = event.getDamageSource();

        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            event.deathMessage(deathMessage.color(TextColor.color(0xCC0000)));
        }

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
                Bukkit.broadcast(TextUtil.message(MessageKeys.COMBAT_KILLSTREAK_REACHED, player.getName(), data.killStreak));
            }
        }

        // But definitely allow them to reset their own killstreak and shame them for it!
        PlayerData data = PlayerData.of(event.getPlayer());

        if (data.killStreak >= 10 && source.getCausingEntity() instanceof Player player) {
            Bukkit.broadcast(TextUtil.message(MessageKeys.COMBAT_KILLSTREAK_ENDED, player.getName(), event.getPlayer().getName(), data.killStreak));
        }

        data.killStreak = 0;
    }

    @Override
    public void reload(VoidSurvival plugin) throws IOException {
        stanceManager.reload(plugin);
    }
}
