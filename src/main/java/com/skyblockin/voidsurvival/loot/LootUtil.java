package com.skyblockin.voidsurvival.loot;

import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.constants.ItemIds;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootUtil {

    public static DummyInventoryHolder openInventory(Block block, Player player, LootTable lootTable, String title, double lootBonus) {

        DummyInventoryHolder holder = new DummyInventoryHolder(block, player.getUniqueId(), title);

        double lootMultiplier = 1.0 + (lootBonus / 100);

        if (Accessors.ITEM_ID.equals(player.getEquipment().getHelmet(), ItemIds.AVARITIA)) {
            lootMultiplier *= 1.5;
        }

        ItemStack[] contents = lootTable.fill(holder.getInventory().getSize(), lootMultiplier);

        holder.getInventory().setContents(contents);

        player.openInventory(holder.getInventory());

        return holder;
    }

    public static DummyInventoryHolder openInventory(Player player, LootTable lootTable, String title, double lootBonus) {
        return openInventory(null, player, lootTable, title, lootBonus);
    }

    public static void fillInventory(Inventory inventory, double lootBonus, LootTable lootTable) {
        inventory.setContents(lootTable.fill(inventory.getSize(), lootBonus));
    }

}
