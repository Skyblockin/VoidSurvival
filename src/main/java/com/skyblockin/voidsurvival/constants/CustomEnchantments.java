package com.skyblockin.voidsurvival.constants;

import com.skyblockin.voidsurvival.enchantment.CustomEnchantment;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.inventory.EquipmentSlotGroup;

public final class CustomEnchantments {

    public static final CustomEnchantment BLEED = CustomEnchantment.ofName("Bleed")
        .weight(1).maxLevel(2)
        .anvilCost(10)
        .minCost(8, 8).maxCost(16,8)
        .supportedItems(ItemTypeTagKeys.SWORDS)
        .activeSlots(EquipmentSlotGroup.HAND);

}
