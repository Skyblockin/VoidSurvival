package com.skyblockin.voidsurvival.util;

import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.item.ItemData;
import com.skyblockin.voidsurvival.item.ItemManager;
import com.skyblockin.voidsurvival.storage.Accessors;
import org.bukkit.inventory.ItemStack;

public class ToolData {

    public static ToolData of(ItemStack item) {

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

    public int toolTier;
    public int toolType;
    public int toolPower;

    public ToolData(int toolTier, int toolType, int toolPower) {
        this.toolTier = toolTier;
        this.toolType = toolType;
        this.toolPower = toolPower;
    }
}
