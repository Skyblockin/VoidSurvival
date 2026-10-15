package com.skyblockin.voidsurvival.block;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.gathering.GatheringNode;
import com.skyblockin.voidsurvival.item.ItemData;
import com.skyblockin.voidsurvival.item.ItemManager;
import com.skyblockin.voidsurvival.region.Flags;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.world.WorldGuardUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockListener implements Listener {

    @EventHandler
    public void onNoteBlockInteract(PlayerInteractEvent event) {

        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) {
            return;
        }

        if (clickedBlock.getType() == Material.NOTE_BLOCK) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onNoteBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.NOTE_BLOCK) {
            event.setCancelled(true);
        }
    }

    private static final Map<UUID, BlockBreakSession> blockBreakProgress = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
        clearSession(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearSession(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAnimation(PlayerAnimationEvent event) {

        Player player = event.getPlayer();

        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            clearSession(player.getUniqueId());
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        Block block = player.getTargetBlockExact(4);

        if (block == null) {
            clearSession(player.getUniqueId());
            return;
        }

        UUID uuid = player.getUniqueId();
        BlockBreakSession session = blockBreakProgress.get(uuid);

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

        if (session == null || !block.equals(session.getBlock())) {

            ToolEvaluation evaluationResult = getMiningSpeedForBlock(item, block);

            if (!evaluationResult.shouldDropAnything()) {
                clearSession(uuid);
                return;
            }

            if (session != null) {
                session.reset();
            }
            session = new BlockBreakSession(player, item, block, evaluationResult.breakPerTick, actuallyBreak);
            blockBreakProgress.put(uuid, session);
        }

        if (session.update()) {
            clearSession(uuid);
        }
    }

    private record ToolEvaluation(float breakPerTick, boolean shouldDropAnything) {}

    private ToolEvaluation getMiningSpeedForBlock(ItemStack item, Block block) {

        ToolData toolData = getToolTierAndTypeAndPower(item);
        CustomBlock customBlock = CustomBlock.ofBlock(block);

        if (toolData == null) {
            if (customBlock != null) {

                float hardness = customBlock.getProperties().toughness;
                if (hardness < 0) return new ToolEvaluation(0, false);
                if (hardness == 0) return new ToolEvaluation(1, true);

                boolean handIsValid = customBlock.getProperties().requiredToolType <= 0 && customBlock.getProperties().requiredToolTier <= 0;

                float divider = handIsValid ? 30.0F : 100.0F;
                float breakPerTick = 1.0F / (hardness * divider);

                return new ToolEvaluation(breakPerTick, handIsValid);
            }

            float hardness = block.getType().getHardness();
            if (hardness < 0) return new ToolEvaluation(0, false);
            if (hardness == 0) return new ToolEvaluation(1, true);

            boolean isPreferred = block.isPreferredTool(item);
            float divider = block.getBlockData().requiresCorrectToolForDrops() ? 100.0F : 30.0F;

            float breakPerTick = 1.0F / (hardness * divider);
            boolean shouldDrop = !block.getBlockData().requiresCorrectToolForDrops() || isPreferred;

            return new ToolEvaluation(breakPerTick, shouldDrop);
        }

        float hardness;
        boolean isValidTool;

        if (customBlock != null) {
            hardness = customBlock.getProperties().toughness;
            int requiredToolTier = customBlock.getProperties().requiredToolTier;
            int requiredToolType = customBlock.getProperties().requiredToolType;
            isValidTool = toolData.toolTier >= requiredToolTier && (toolData.toolType == requiredToolType || requiredToolType < 0);
        } else {
            hardness = block.getType().getHardness();
            boolean matchesTag = false;
            if (toolData.toolType == 1 && Tag.MINEABLE_PICKAXE.isTagged(block.getType())) matchesTag = true;
            else if (toolData.toolType == 2 && Tag.MINEABLE_AXE.isTagged(block.getType())) matchesTag = true;
            else if (toolData.toolType == 3 && Tag.MINEABLE_SHOVEL.isTagged(block.getType())) matchesTag = true;
            else if (toolData.toolType == 4 && Tag.MINEABLE_HOE.isTagged(block.getType())) matchesTag = true;
            isValidTool = matchesTag;
        }

        if (hardness < 0) return new ToolEvaluation(0, false);
        if (hardness == 0) return new ToolEvaluation(1, true);

        float speedMultiplier;
        float divider;

        if (isValidTool) {
            int efficiencyLevel = item.getEnchantmentLevel(Enchantment.EFFICIENCY);
            int totalToolPower = toolData.toolPower + (efficiencyLevel > 0 ? (efficiencyLevel * efficiencyLevel + 1) : 0);
            speedMultiplier = (float) totalToolPower;
            divider = 150.0F;
        } else {
            speedMultiplier = 1.0F;
            boolean requiresTool = customBlock != null ? customBlock.getProperties().requiredToolType > 0 : block.getBlockData().requiresCorrectToolForDrops();
            divider = requiresTool ? 100.0F : 30.0F;
        }

        float breakPerTick = speedMultiplier / (hardness * divider);
        boolean shouldDrop = customBlock != null ? isValidTool : (!block.getBlockData().requiresCorrectToolForDrops() || isValidTool);

        return new ToolEvaluation(breakPerTick, shouldDrop);
    }

    private record ToolData(int toolTier, int toolType, int toolPower) {}

    private ToolData getToolTierAndTypeAndPower(ItemStack item) {

        String id = Accessors.ITEM_ID.read(item);

        if (id != null) {
            ItemData data = ItemManager.getItemById(id);
            if (data == null || data.toolTier == -1 || data.toolType == -1 || data.toolPower == -1) {
                return null;
            }
            return new ToolData(data.toolTier, data.toolType, data.toolPower);
        }

        String[] itemTierAndName = item.getType().name().split("_");

        if (itemTierAndName.length != 2) {
            return null;
        }

        Pair<Integer, Integer> itemTierAndPower = switch (itemTierAndName[0].toLowerCase()) {
            case "wooden" -> new Pair<>(1, 2);
            case "stone" -> new Pair<>(2, 4);
            case "bronze" -> new Pair<>(3, 5);
            case "gold" -> new Pair<>(3, 12);
            case "iron" -> new Pair<>(4, 6);
            case "diamond" -> new Pair<>(5, 8);
            case "netherite" -> new Pair<>(6, 9);
            default -> null;
        };

        int itemType = switch(itemTierAndName[1].toLowerCase()) {
            case "pickaxe" -> 1;
            case "axe" -> 2;
            case "shovel" -> 3;
            case "hoe" -> 4;
            default -> -1;
        };

        if (itemTierAndPower == null || itemType == -1) {
            return null;
        }

        return new ToolData(itemTierAndPower.left, itemType, itemTierAndPower.right);
    }

    private void clearSession(UUID uuid) {
        BlockBreakSession session = blockBreakProgress.remove(uuid);
        if (session != null) {
            session.reset();
        }
    }
}
