package com.skyblockin.voidsurvival.item.listeners;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.math.Cuboid;
import com.skyblockin.voidsurvival.nms.NMSUtil;
import com.skyblockin.voidsurvival.storage.Accessors;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class LavaWalkerListener implements Listener {

    @EventHandler
    public void onMove(EntityMoveEvent event) {

        Location location = event.getTo().clone().subtract(0, 1, 0);
        EntityEquipment equipment = event.getEntity().getEquipment();

        if (equipment == null) {
            return;
        }

        ItemStack boots = equipment.getBoots();

        if (boots.isEmpty()) {
            return;
        }

        if (Accessors.ITEM_ID.equals(boots, "pignite_boots")) {

            int size = 4;
            int sizeSquared = size * size;

            BlockData obsidianBlockData = BlockType.OBSIDIAN.createBlockData();
            World world = location.getWorld();

            Cuboid.of(location, size, 0, size).forEach(pos -> {

                if (pos.distanceSquared(location) > sizeSquared) {
                    return;
                }

                Block block = world.getBlockAt(pos.x(), pos.y(), pos.z());

                if (block.getType().equals(Material.LAVA)) {

                    BlockDecayTask task = new BlockDecayTask(block, obsidianBlockData, block.getBlockData(), 4, 0.1);

                    block.setType(Material.OBSIDIAN, false);

                    task.runTaskTimer(VoidSurvival.getInstance(), 0, 1);
                }

            });

        }
    }

    private static class BlockDecayTask extends BukkitRunnable {

        private final Block block;
        private final BlockData newData;
        private final BlockData oldData;
        private final int decayTicks;
        private final double decayChance;

        private int stage = 0;
        private int tick = 0;

        public BlockDecayTask(Block block, BlockData newData, BlockData oldData, int decayTicks, double decayChance) {
            this.block = block;
            this.newData = newData;
            this.oldData = oldData;
            this.decayTicks = decayTicks;
            this.decayChance = decayChance;
        }

        @Override
        public void run() {

            if (!block.getType().equals(newData.getMaterial())) {
                cancel();
                return;
            }

            tick++;

            if (tick % decayTicks != 0) {
                return;
            }

            if (ThreadLocalRandom.current().nextDouble() < decayChance) {
                stage++;
            }

            if (stage > 10) {
                block.setBlockData(oldData);
                block.getState().update();
                NMSUtil.broadcastBlockCrack(block, 0);
                cancel();
            } else {
                NMSUtil.broadcastBlockCrack(block, stage);
            }
        }
    }

}
