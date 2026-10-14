package com.skyblockin.voidsurvival.entity.boss.trait;

import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.Collection;

public class RoyalRevenantTrait extends Trait {

    private static final double RADIUS = 35.0;
    private int tickCounter = 0;
    private static final int SHOOT_INTERVAL_TICKS = 20;

    public RoyalRevenantTrait() {
        super("fighter_trait");
    }

    @Override
    public void run() {

        if (!npc.isSpawned()) return;

        tickCounter++;

        Location npcLocation = npc.getStoredLocation();
        LivingEntity npcEntity = (LivingEntity) npc.getEntity();

        Player target = findTarget();

        if (target == null) {
            if (npc.getNavigator().isNavigating()) {
                npc.getNavigator().cancelNavigation();
            }
            return;
        }

        Navigator nav = npc.getNavigator();

        nav.getLocalParameters().speedModifier(1.4F);

        if (tickCounter >= SHOOT_INTERVAL_TICKS) {
            tickCounter = 0;

            nav.cancelNavigation();
            npc.faceLocation(target.getEyeLocation());

            Location eyeLoc = npcEntity.getEyeLocation();
            Vector direction = target.getEyeLocation().toVector().subtract(eyeLoc.toVector()).normalize();

            eyeLoc.getWorld().spawn(eyeLoc.add(direction.clone().multiply(1.1)), WitherSkull.class, skull -> {
                skull.setShooter(npcEntity);
                skull.setCharged(true);
                skull.setVelocity(direction.clone().multiply(1.5));
            });

        } else {
            nav.setTarget(target, false);
            if (npcLocation.distance(target.getLocation()) <= 3.0) {
                npcEntity.attack(target);
            }
        }
    }

    private Player findTarget() {

        Location npcLocation = npc.getStoredLocation();
        Collection<Player> nearbyPlayers = npcLocation.getNearbyEntitiesByType(Player.class, RADIUS, p ->
            p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || p.isDead()
        );

        Player target = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player p : nearbyPlayers) {
            double dist = npcLocation.distanceSquared(p.getLocation());
            if (dist < closestDistance) {
                closestDistance = dist;
                target = p;
            }

        }

        return target;
    }
}
