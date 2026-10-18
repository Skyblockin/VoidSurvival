package com.skyblockin.voidsurvival.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    private boolean drop;

    public BlockBreakSession(Player player, ItemStack tool, Block block, float breakSpeed, boolean actuallyBreak, boolean drop) {
        this.player = player;
        this.tool = tool;
        this.identity = ThreadLocalRandom.current().nextInt(10000);
        this.block = block;
        this.breakSpeed = breakSpeed;
        this.actuallyBreak = actuallyBreak;
        this.drop = drop;
    }

    public boolean update() {
        progress += breakSpeed;
        if (progress >= 1) {

            CustomBlock customBlock = CustomBlock.ofBlock(block);

            reset();

            if (!actuallyBreak) {

                CustomBlockMineEvent.callEvent(player, block, customBlock);

                if (customBlock != null) {
                    if (drop) player.getInventory().addItem(customBlock.breakResult.createItem());
                } else {
                    if (drop) player.getInventory().addItem(new ItemStack(block.getType()));
                }

                tool.damage(1, player);

            } else {

                CustomBlockMineEvent.callEvent(player, block, customBlock);

                if (customBlock != null) {
                    player.getWorld().playEffect(block.getLocation(), Effect.DESTROY_BLOCK, customBlock.data);
                    block.setType(Material.AIR);
                    if (drop) player.getWorld().dropItem(block.getLocation(), customBlock.breakResult.createItem());
                    tool.damage(1, player);
                } else {
                    if (drop) {
                        block.breakNaturally(tool, true, true);
                    } else {
                        player.getWorld().playEffect(block.getLocation(), Effect.DESTROY_BLOCK, block.getBlockData());
                        block.setType(Material.AIR);
                    }
                    tool.damage(1, player);
                }

            }

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
