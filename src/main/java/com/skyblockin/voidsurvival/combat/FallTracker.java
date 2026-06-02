package com.skyblockin.voidsurvival.combat;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.math.Cuboid;
import com.skyblockin.voidsurvival.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BoundingBox;

import java.util.*;

public class FallTracker implements Listener {

    private final HashMap<Block, Float> blockDamages = new HashMap<>();
    private final HashMap<Block, Integer> lastDamageTicks = new HashMap<>();
    private final HashMap<UUID, Float> fallPowers = new HashMap<>();

    public FallTracker() {

        VoidSurvival.getInstance().runTaskTimer(() -> {

            int currentTick = VoidSurvival.getInstance().getServer().getCurrentTick();

            ArrayList<Block> entriesToRemove = new ArrayList<>();

            lastDamageTicks.forEach((pos, tick) -> {
                if (tick + 300 < currentTick) {
                    entriesToRemove.add(pos);
                }
            });

            for (Block block : entriesToRemove) {
                blockDamages.remove(block);
                lastDamageTicks.remove(block);
                NMSUtil.resetBlockCrack(block.getWorld().getNearbyPlayers(block.getLocation(), 50), BlockPosition.ofBlock(block));
            }

            tickFall();

        }, 0, 1L);

    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL || event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Entity entity = event.getEntity();
        float fallDistance = entity.getFallDistance();

        if (fallDistance > 10) {
            fallPowers.put(entity.getUniqueId(), fallDistance - 10);
        }
    }

    private void tickFall() {

        Iterator<Map.Entry<UUID, Float>> iterator = fallPowers.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<UUID, Float> entry = iterator.next();

            Entity entity = Bukkit.getEntity(entry.getKey());

            // If the entity doesn't exist, it cannot possibly be falling anymore so we may as well remove it
            if (entity == null) {
                iterator.remove();
                continue;
            }

            World world = entity.getWorld();
            BoundingBox entityBox = entity.getBoundingBox();
            float fallPower = entry.getValue();

            BoundingBox box = new BoundingBox(
                entityBox.getMinX(), entityBox.getMinY() - 1, entityBox.getMinZ(),
                entityBox.getMaxX(), entityBox.getMinY() + 1, entityBox.getMaxZ()
            );

            float maxFallPowerReduction = 0.0F;

            for (BlockPosition pos : Cuboid.between(box.getMin(), box.getMax())) {

                Block block = pos.toLocation(world).getBlock();

                if (!block.getType().isBlock()) {
                    continue;
                }

                float damage = blockDamages.getOrDefault(block, 0.0F);
                float hardness = block.getType().getHardness();

                // Less than 0 hardness means infinite
                if (hardness < 0) {
                    maxFallPowerReduction = 1_000_000;
                    continue;
                }

                // I wrote this at like 3 AM so I felt really confused, but basically how it should work is
                // if the block already took 19/20 damage, only 1 damage should be used up for it on this step,
                // and thus, the rest of the "fall power" should carry over to the next blocks
                // The dealt damage should of course never be negative either, with the floor being at 0.
                float damageDealt = Math.clamp(fallPower / 10.0F, 0, hardness - damage);

                if (damageDealt <= 0) {
                    continue;
                }

                damage += damageDealt;

                if (damageDealt > maxFallPowerReduction) {
                    maxFallPowerReduction = damageDealt;
                }

                if (damage >= hardness) {
                    NMSUtil.resetBlockCrack(world.getNearbyPlayers(block.getLocation(), 50), BlockPosition.ofBlock(block));
                    block.breakNaturally();
                    blockDamages.remove(block);
                    lastDamageTicks.remove(block);
                } else {
                    blockDamages.put(block, damage);
                    lastDamageTicks.put(block, VoidSurvival.getInstance().getServer().getCurrentTick());
                    NMSUtil.broadcastBlockCrack(world.getNearbyPlayers(block.getLocation(), 50), BlockPosition.ofBlock(block), damage / hardness);
                }
            }

            fallPower -= maxFallPowerReduction * 10.0F;

            if (fallPower < 0) {
                iterator.remove();
            } else {
                entry.setValue(fallPower);
            }

        }

    }
}
