package com.skyblockin.voidsurvival.item;

import com.skyblockin.voidsurvival.enchantment.CustomEnchantment;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.inventory.EquipmentSlotGroup;

public final class CustomEnchantments {

    public static final CustomEnchantment REPLENISH = CustomEnchantment.ofName("Replenish")
        .weight(1)
        .maxLevel(1)
        .anvilCost(10)
        .minCost(8, 8).maxCost(16, 8)
        .supportedItems(ItemTypeTagKeys.HOES)
        .activeSlots(EquipmentSlotGroup.HAND);

    public static final CustomEnchantment BLEED = CustomEnchantment.ofName("Bleed")
        .weight(1)
        .maxLevel(255)
        .anvilCost(10)
        .minCost(8, 8).maxCost(16,8)
        .supportedItems(ItemTypeTagKeys.SWORDS)
        .activeSlots(EquipmentSlotGroup.HAND);

}
