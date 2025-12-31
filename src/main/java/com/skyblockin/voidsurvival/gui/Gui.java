package com.skyblockin.voidsurvival.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public interface Gui {

    UUID getViewerId();

    Inventory getInventory();

    Component getTitle();

    int getSlots();

    GuiItem getItem(int slot);

    void addItem(ItemStack item, Consumer<InventoryClickEvent> onClick);

    void addItem(GuiItem element);

    void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick);

    void setItem(int slot, GuiItem element);

    void createInventory();

    void open();

    static Gui create(UUID uuid, Component title, int slots) {
        return new StaticGui(uuid, title, slots);
    }

}
