package com.skyblockin.voidsurvival.loot;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootUtil {

    public static void openInventory(Player player, LootTable lootTable, String title, double lootBonus) {

        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, Component.text(title));

        ItemStack[] contents = lootTable.fill(inventory.getSize(), lootBonus);

        inventory.setContents(contents);

        player.openInventory(inventory);
    }

    public static void openInventory(Player player, LootTable lootTable, double lootBonus) {

        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST);

        ItemStack[] contents = lootTable.fill(inventory.getSize(), lootBonus);

        inventory.setContents(contents);

        player.openInventory(inventory);
    }

    public static void fillInventory(Inventory inventory, double lootBonus, LootTable lootTable) {
        inventory.setContents(lootTable.fill(inventory.getSize(), lootBonus));
    }

}
