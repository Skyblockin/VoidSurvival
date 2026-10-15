package com.skyblockin.voidsurvival.trigger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.math.Cuboid;
import com.skyblockin.voidsurvival.storage.PlayerData;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BoundingBox;

import java.util.Collection;
import java.util.List;

public class InteractTrigger implements Trigger<PlayerInteractEvent> {

    private final String id;
    private final List<String> triggerableIds;
    private final Block block;
    private Cuboid cuboid;
    private final Requirements requirements;

    @JsonCreator
    public InteractTrigger(String id, List<String> triggerableIds, World world, BlockPosition pos, Cuboid cuboid, Requirements requirements) {
        this.id = id;
        this.triggerableIds = triggerableIds;
        this.block = pos.toLocation(world).getBlock();
        this.cuboid = cuboid;
        this.requirements = requirements;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void activate(TriggerManager manager, PlayerInteractEvent event) {

        Block clicked = event.getClickedBlock();
        Player player = event.getPlayer();
        PlayerData data = PlayerData.of(player);

        if (clicked != null && clicked.equals(this.block) && (requirements == null || requirements.test(data))) {

            if (cuboid != null) {

                Collection<Entity> entities = player.getWorld().getNearbyEntities(BoundingBox.of(
                    cuboid.getMin().toLocation(player.getWorld()),
                    cuboid.getMax().toLocation(player.getWorld())
                ), entity -> entity instanceof Player);

                for (Entity entity : entities) {
                    Player p = (Player) entity;
                    PlayerData pData = PlayerData.of(p);
                    TriggerSequence.runSequence(triggerableIds, manager, p, pData);
                }

            } else {
                TriggerSequence.runSequence(triggerableIds, manager, player, data);
            }
        }
    }

    @Override
    public Requirements getRequirements() {
        return requirements;
    }

}
