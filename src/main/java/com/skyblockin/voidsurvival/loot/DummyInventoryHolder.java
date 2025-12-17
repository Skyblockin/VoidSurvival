package com.skyblockin.voidsurvival.loot;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DummyInventoryHolder implements InventoryHolder {

    private final Block associatedBlock;
    private final UUID opener;
    private final Inventory inventory;

    public DummyInventoryHolder(Block associatedBlock, UUID opener, String title) {
        this.associatedBlock = associatedBlock;
        this.opener = opener;
        this.inventory = Bukkit.createInventory(this, InventoryType.CHEST, Component.text(title));;
    }

    public @Nullable Block getBlock() {
        return associatedBlock;
    }

    public UUID getOpener() {
        return this.opener;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

}
