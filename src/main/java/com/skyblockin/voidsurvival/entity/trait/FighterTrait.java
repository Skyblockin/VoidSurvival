package com.skyblockin.voidsurvival.entity.trait;

import com.skyblockin.voidsurvival.entity.AllySet;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class FighterTrait extends Trait {

    private static final double RADIUS = 35.0;

    public FighterTrait() {
        super("fighter_trait");
    }

    @Override
    public void run() {

        if (!npc.isSpawned()) return;

        Location npcLocation = npc.getStoredLocation();
        LivingEntity npcEntity = (LivingEntity) npc.getEntity();

        Player target = findTarget();

        if (target == null) {
            if (npc.getNavigator().isNavigating()) {
                npc.getNavigator().cancelNavigation();
            }
            return;
        }

        if (npcLocation.distance(target.getLocation()) <= 3.0) {
            npcEntity.attack(target);
        }
    }

    private Player findTarget() {

        AllySet allies = npc.data().get("allies");

        Location npcLocation = npc.getStoredLocation();
        Collection<Player> nearbyPlayers = npcLocation.getNearbyEntitiesByType(Player.class, RADIUS, p -> {

            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || p.isDead()) {
                return true;
            }

            String alliance = CitizensAPI.getTemporaryNPCRegistry().getNPC(p).data().get("faction");

            return allies.contains(alliance);
        });

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
