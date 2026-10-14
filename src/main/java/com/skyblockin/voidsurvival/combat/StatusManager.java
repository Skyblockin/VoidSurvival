package com.skyblockin.voidsurvival.combat;

import com.skyblockin.voidsurvival.util.TextUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class StatusManager {

    private final HashMap<UUID, Double> statusMap = new HashMap<>();
    private final HashMap<UUID, BossBar> statusBars = new HashMap<>();
    private final String statusId;
    private final BossBar.Color statusBarColor;
    private final BossBar.Overlay statusBarOverlay;

    public StatusManager(String statusId, BossBar.Color statusBarColor, BossBar.Overlay statusBarOverlay) {
        this.statusId = statusId;
        this.statusBarColor = statusBarColor;
        this.statusBarOverlay = statusBarOverlay;
    }

    public String getStatusId() {
        return statusId;
    }

    protected Component getStatusTitle(double progress) {
        return TextUtil.color("<dark_red><bold>%s</bold> <red>[%d/100]", statusId.toUpperCase(), (int) progress);
    }

    public abstract double calculateStatusDamage(LivingEntity target, LivingEntity damager, EntityDamageByEntityEvent event);

    public abstract void playStatusActivationEffect(LivingEntity target, LivingEntity damager, EntityDamageByEntityEvent event);

    public abstract void playStatusTickEffect(LivingEntity entity, double progress);

    public abstract double calculateResistance(LivingEntity entity);

    protected long getStatusDecayTicks() {
        return 5 * 20;
    }

    public void updateStatus(LivingEntity entity, LivingEntity damager, EntityDamageByEntityEvent event) {

        double statusDamage = calculateStatusDamage(entity, damager, event);
        double currentStatusProgress = statusMap.getOrDefault(entity.getUniqueId(), 0.0) + statusDamage;

        if (entity instanceof Player) {
            statusBars.computeIfAbsent(entity.getUniqueId(), key -> {

                    BossBar bar = BossBar.bossBar(
                        getStatusTitle(currentStatusProgress), (float) (currentStatusProgress / 100),
                        statusBarColor, statusBarOverlay
                    );

                    entity.showBossBar(bar);

                    return bar;

                }).progress(Math.clamp((float) (currentStatusProgress / 100), 0F, 1F))
                .name(getStatusTitle(currentStatusProgress));
        }

        if (currentStatusProgress >= 100) {
            playStatusActivationEffect(entity, damager, event);
            statusMap.remove(entity.getUniqueId());
            if (entity instanceof Player) {
                entity.hideBossBar(statusBars.remove(entity.getUniqueId()));
            }
        } else {
            statusMap.put(entity.getUniqueId(), currentStatusProgress);
        }

    }

    public void tickStatus(HashMap<UUID, Instant> lastDamageTimes) {

        List<UUID> keysToRemove = new ArrayList<>();

        statusMap.replaceAll((uuid, value) -> {

            Entity e = Bukkit.getEntity(uuid);

            if (!(e instanceof LivingEntity entity)) {
                keysToRemove.add(uuid);
                return value;
            }

            // Play effect around the entity
            playStatusTickEffect(entity, value);

            // If the entity was damaged in the past 5 seconds, prevent bleeding from decaying
            if (lastDamageTimes.getOrDefault(uuid, Instant.MIN).plusSeconds(5).isBefore(Instant.now())) {
                return value;
            }

            double newValue = value - 5;

            if (newValue <= 0) {
                keysToRemove.add(uuid);
                // Only manage boss bars for players
            } else if (e instanceof Player) {
                statusBars.computeIfPresent(uuid, (key, bar) ->
                    bar.name(getStatusTitle(newValue))
                        .progress(Math.clamp((float) (newValue / 100), 0F, 1F))
                );
            }

            return newValue;
        });

        // Cleaning up
        keysToRemove.forEach(uuid -> {
            lastDamageTimes.remove(uuid);

            Entity entity = Bukkit.getEntity(uuid);
            BossBar bar = statusBars.remove(uuid);

            if (entity instanceof Player player && bar != null) {
                player.hideBossBar(bar);
            }

        });

    }
}
