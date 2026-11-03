package com.skyblockin.voidsurvival.util;

import com.skyblockin.voidsurvival.block.CustomBlock;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ToolEvaluation {

    public static ToolEvaluation of(ItemStack item, Block block) {

        ToolData toolData = ToolData.of(item);
        CustomBlock customBlock = CustomBlock.ofBlock(block);

        if (toolData == null) {
            if (customBlock != null) {

                float hardness = customBlock.toughness;
                if (hardness < 0) return new ToolEvaluation(0, false);
                if (hardness == 0) return new ToolEvaluation(1, true);

                boolean handIsValid = customBlock.requiredToolType <= 0 && customBlock.requiredToolTier <= 0;

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
            hardness = customBlock.toughness;
            boolean isRequiredTier = customBlock.requiredToolTier <= toolData.toolTier || customBlock.requiredToolTier < 0;
            boolean isRequiredType = customBlock.requiredToolType == toolData.toolType || customBlock.requiredToolType < 0;
            isValidTool = isRequiredTier && isRequiredType;
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
            divider = 30.0F;
        } else {
            speedMultiplier = 1.0F;
            boolean requiresTool = customBlock != null ? customBlock.requiredToolType > 0 : block.getBlockData().requiresCorrectToolForDrops();
            divider = requiresTool ? 100.0F : 30.0F;
        }

        float breakPerTick = speedMultiplier / (hardness * divider);
        boolean shouldDrop = customBlock != null ? isValidTool : (!block.getBlockData().requiresCorrectToolForDrops() || isValidTool);

        return new ToolEvaluation(breakPerTick, shouldDrop);
    }

    public float breakPerTick;
    public boolean shouldDropAnything;

    public ToolEvaluation(float breakPerTick, boolean shouldDropAnything) {
        this.breakPerTick = breakPerTick;
        this.shouldDropAnything = shouldDropAnything;
    }
}
