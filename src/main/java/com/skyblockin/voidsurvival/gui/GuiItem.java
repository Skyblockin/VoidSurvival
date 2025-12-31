package com.skyblockin.voidsurvival.gui;

import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GuiItem {

    private final ItemStack item;
    private final Consumer<GuiClickContext> onClick;

    public GuiItem(ItemStack item, Consumer<GuiClickContext> onClick) {
        this.item = item;
        this.onClick = onClick;
    }

    public ItemStack getItem() {
        return item;
    }

    public void handleClick(GuiClickContext context) {
        onClick.accept(context);
    }
}
