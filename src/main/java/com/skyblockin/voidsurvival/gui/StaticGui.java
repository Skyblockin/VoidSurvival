package com.skyblockin.voidsurvival.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class StaticGui implements Gui {

    private final UUID uuid;
    private final Component title;
    private final int slots;
    private final Map<Integer, GuiItem> elements = new HashMap<>();

    private Inventory inventory;

    public StaticGui(UUID uuid, Component title, int slots) {
        this.uuid = uuid;
        this.title = title;
        this.slots = slots;
    }

    @Override
    public UUID getViewerId() {
        return uuid;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @Override
    public GuiItem getItem(int slot) {
        return elements.get(slot);
    }

    @Override
    public void addItem(ItemStack item, Consumer<InventoryClickEvent> onClick) {
        this.addItem(new GuiItem(item, context -> onClick.accept(context.getEvent())));
    }

    @Override
    public void addItem(GuiItem element) {
        this.elements.computeIfAbsent(elements.size(), k -> element);
    }

    @Override
    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        this.setItem(slot, new GuiItem(item, context -> onClick.accept(context.getEvent())));
    }

    @Override
    public void setItem(int slot, GuiItem element) {
        this.elements.put(slot, element);
    }

    @Override
    public void createInventory() {
        this.inventory = Bukkit.createInventory(null, slots, title);
    }

    @Override
    public void open() {
        if (inventory == null) {
            createInventory();
        }

        for (Map.Entry<Integer, GuiItem> entry : elements.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItem());
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.openInventory(inventory);
            GuiManager.getInstance().openedGui(this);
        }
    }
}
