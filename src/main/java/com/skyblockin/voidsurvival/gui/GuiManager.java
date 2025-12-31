package com.skyblockin.voidsurvival.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GuiManager implements Listener {

    private static GuiManager INSTANCE;
    private final Set<Gui> openGuis = new HashSet<>();

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    public static GuiManager init() {
        if (isInitialized()) throw new IllegalStateException("GuiManager already initialized!");
        INSTANCE = new GuiManager();
        return INSTANCE;
    }

    public static GuiManager getInstance() {
        if (!isInitialized()) {
            return init();
        }
        return INSTANCE;
    }

    public void openedGui(Gui gui) {
        this.openGuis.add(gui);
    }

    public void closedGui(Gui gui) {
        this.openGuis.remove(gui);
    }

    public Gui getGui(Inventory inventory) {
        for (Gui gui : openGuis) {
            if (gui.getInventory().equals(inventory)) {
                return gui;
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;

        Gui menu = getGui(inventory);
        if (menu == null) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        GuiItem item = menu.getItem(slot);
        if (item != null) {
            item.handleClick(new GuiClickContext(event));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Gui menu = getGui(event.getInventory());
        if (menu != null && menu.getInventory().equals(event.getInventory())) {
            closedGui(menu);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        openGuis.removeIf(gui -> gui.getViewerId().equals(player.getUniqueId()));
    }
}
