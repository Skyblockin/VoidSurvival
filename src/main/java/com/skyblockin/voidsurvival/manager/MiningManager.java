package com.skyblockin.voidsurvival.manager;

import com.skyblockin.voidsurvival.block.BlockBreakSession;
import com.skyblockin.voidsurvival.block.CustomBlock;
import com.skyblockin.voidsurvival.block.CustomBlockPreMineEvent;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.region.Flags;
import com.skyblockin.voidsurvival.util.ToolEvaluation;
import com.skyblockin.voidsurvival.world.edit.WorldGuardUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiningManager extends Manager implements Listener {

    private static final Map<UUID, BlockBreakSession> blockBreakProgress = new HashMap<>();
    private static final Map<UUID, BukkitTask> activeMiningTasks = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
        if (!isEnabled()) return;
        clearSession(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!isEnabled()) return;
        clearSession(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0);
    }

    @Override
    public void onDisable(VoidSurvival plugin) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            clearSession(player.getUniqueId());
            player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(Attribute.BLOCK_BREAK_SPEED.getDefaultValue());
        }
    }

    @Override
    public void onEnable(VoidSurvival plugin) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0);
        }
    }

    @EventHandler
    public void onClickBlock(PlayerInteractEvent event) {

        if (!isEnabled()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        // TODO: Figure out how to make this not fucked
        // Cancel events for note blocks as we use them for custom blocks and don't want their states modified
        if (block.getType() == Material.NOTE_BLOCK) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE || block.getType().getHardness() < 0) {
            clearSession(uuid);
            return;
        }

        // If already mining this exact block, let the repeating task handle it
        BlockBreakSession existingSession = blockBreakProgress.get(uuid);

        if (existingSession != null && block.equals(existingSession.getBlock())) {
            return;
        }

        CustomBlock customBlock = CustomBlock.ofBlock(block);

        // Allow for other classes to have a say in the flow of this
        if (!CustomBlockPreMineEvent.callEvent(player, block, customBlock)) {
            clearSession(uuid);
            return;
        }

        boolean isProtected = !WorldGuardUtil.testBuild(block.getLocation(), player);
        boolean isCustomBreakable = VoidSurvival.getInstance().getPlayerBlockManager().isBreakableBlock(block);
        boolean hasRegenFlag = Flags.REGENERATE_BLOCKS.test(player, block);

        if (isProtected) {
            if (hasRegenFlag) {
                if (!isCustomBreakable) {
                    clearSession(uuid);
                    return;
                }
            } else {
                if (isCustomBreakable) {
                    clearSession(uuid);
                    return;
                }
            }
        }

        boolean actuallyBreak = !hasRegenFlag;
        ItemStack item = player.getInventory().getItemInMainHand();

        ToolEvaluation evaluationResult = ToolEvaluation.of(item, block);

        // Reset any old session and start fresh
        clearSession(uuid);

        BlockBreakSession session = new BlockBreakSession(player, item, block, evaluationResult.breakPerTick, actuallyBreak, evaluationResult.shouldDropAnything);
        blockBreakProgress.put(uuid, session);

        // Start a continuous tick task to handle the mining progression smoothly
        BukkitTask task = new BukkitRunnable() {

            private int delayTicks = 0;
            private final boolean isInstantBreak = evaluationResult.breakPerTick >= 1;

            @Override
            public void run() {
                if (!player.isOnline() || player.getGameMode() == GameMode.CREATIVE) {
                    clearSession(uuid);
                    cancel();
                    return;
                }

                double reach = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getValue(); // Default 4.5 in Survival
                Block target = player.getTargetBlockExact((int) Math.ceil(reach));
                if (target == null || !target.getLocation().equals(block.getLocation())) {
                    clearSession(uuid);
                    cancel();
                    return;
                }

                if (delayTicks > 0) {
                    delayTicks--;
                    return;
                }

                if (session.update()) {
                    delayTicks = isInstantBreak ? 0 : 5;
                    clearSession(uuid);
                    cancel();
                }
            }
        }.runTaskTimer(VoidSurvival.getInstance(), 0L, 1L);

        activeMiningTasks.put(uuid, task);
    }

    private void clearSession(UUID uuid) {
        BukkitTask task = activeMiningTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        BlockBreakSession session = blockBreakProgress.remove(uuid);
        if (session != null) {
            session.reset();
        }
    }
}
