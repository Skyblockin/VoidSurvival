package com.skyblockin.voidsurvival.block;

import com.skyblockin.voidsurvival.VoidSurvival;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

import static com.skyblockin.voidsurvival.nms.NMSUtil.sendPacket;

public class BlockBreakSession {

    private final int identity;
    private final Block block;
    private final float breakSpeed;
    private float progress;
    private Player player;
    private ItemStack tool;
    private boolean actuallyBreak;

    public BlockBreakSession(Player player, ItemStack tool, Block block, float breakSpeed, boolean actuallyBreak) {
        this.player = player;
        this.tool = tool;
        this.identity = ThreadLocalRandom.current().nextInt(10000);
        this.block = block;
        this.breakSpeed = breakSpeed;
        this.actuallyBreak = actuallyBreak;
    }

    public boolean update() {
        progress += breakSpeed;
        if (progress >= 1) {

            CustomBlock customBlock = CustomBlock.ofBlock(block);

            if (!actuallyBreak) {

                CustomBlockMineEvent.callEvent(player, block, customBlock);

                if (customBlock != null) {
                    player.getInventory().addItem(customBlock.getProperties().breakResult.createItem());
                } else {
                    player.getInventory().addItem(new ItemStack(block.getType()));
                }

                tool.damage(1, player);

            } else {

                CustomBlockMineEvent.callEvent(player, block, customBlock);

                if (customBlock != null) {
                    block.setType(Material.AIR);
                    player.getWorld().playEffect(block.getLocation(), Effect.DESTROY_BLOCK, customBlock.getProperties().data);
                    player.getWorld().dropItem(block.getLocation(), customBlock.getProperties().breakResult.createItem());
                    tool.damage(1, player);
                } else {
                    block.breakNaturally(tool, true, true);
                    tool.damage(1, player);
                }

            }

            reset();
            return true;
        } else {
            broadcast((int) (10.0 * progress));
            return false;
        }
    }

    public void reset() {
        progress = 0.0f;
        broadcast(-1);
    }

    private void broadcast(int stage) {

        BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(identity, pos, stage);

        Collection<Player> players = block.getWorld().getNearbyPlayers(block.getLocation(), 50);

        for (Player player : players) {
            sendPacket(player, packet);
        }

    }

    public Block getBlock() {
        return block;
    }

}
