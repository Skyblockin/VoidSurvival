package com.skyblockin.voidsurvival.trigger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.world.WorldGuardUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.List;

public class KillGainTrigger extends StatGainTrigger<EntityDeathEvent> {

    private final List<String> triggerableIds;
    private final int requiredKills;
    private final String regionId;
    private Instant lastActionTime = Instant.now();
    private final List<EntityType> entityTypes;
    private int currentKills = 0;
    private final ProgressionDisplay display;

    @JsonCreator
    public KillGainTrigger(String id, String regionId, ProgressionDisplay display, List<EntityType> entityTypes, int requiredKills, List<String> triggerableIds, Requirements requirements) {
        super(id, requirements);
        this.display = display;
        this.entityTypes = entityTypes;
        this.triggerableIds = triggerableIds;
        this.requiredKills = requiredKills;
        this.regionId = regionId;
        startCheckingTask();
    }

    private void startCheckingTask() {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (lastActionTime.isBefore(Instant.now().plusSeconds(5 * 60))) {
                    currentKills = 0;
                    if (display != null) {
                        display.setStat(0);
                        display.updateEntity();
                    }
                }
            }
        }.runTaskTimer(VoidSurvival.getInstance(), 20, 20);

    }

    @Override
    public void activate(TriggerManager manager, EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();
        EntityDamageEvent damageEvent = entity.getLastDamageCause();

        if (damageEvent == null || (!entityTypes.isEmpty() && !entityTypes.contains(entity.getType()))) {
            return;
        }

        if (damageEvent.getDamageSource().getCausingEntity() instanceof Player player) {

            if (!WorldGuardUtil.isLocationInRegion(entity.getLocation(), regionId) || currentKills >= requiredKills) {
                return;
            }

            PlayerData data = PlayerData.of(player);

            if (display != null) {
                display.incrementStat(1);
            }

            currentKills++;
            lastActionTime = Instant.now();

            boolean meetsRequirements = getRequirements() == null || getRequirements().test(data);

            if (meetsRequirements && currentKills >= requiredKills) {
                TriggerSequence.runSequence(triggerableIds, manager, player, data);
            }
        }

    }

}
