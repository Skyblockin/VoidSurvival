package com.skyblockin.voidsurvival.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiClickContext {

    private final InventoryClickEvent event;

    public GuiClickContext(InventoryClickEvent event) {
        this.event = event;
    }

    public InventoryClickEvent getEvent() {
        return event;
    }
}
