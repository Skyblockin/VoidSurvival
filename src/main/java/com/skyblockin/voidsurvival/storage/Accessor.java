package com.skyblockin.voidsurvival.storage;

import com.skyblockin.voidsurvival.VoidSurvival;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class Accessor<C> {

    private final PersistentDataType<?, C> type;
    private final NamespacedKey key;

    public Accessor(NamespacedKey key, PersistentDataType<?, C> type) {
        this.key = key;
        this.type = type;
    }

    public Accessor(String key, PersistentDataType<?, C> type) {
        this(new NamespacedKey(VoidSurvival.getInstance(), key), type);
    }

    public C read(PersistentDataContainerView container) {
        return container.get(key, type);
    }

    public C read(PersistentDataContainer container) {
        return container.get(key, type);
    }

    public C read(PersistentDataHolder holder) {
        return read(holder.getPersistentDataContainer());
    }

    public C read(ItemStack item) {
        return read(item.getPersistentDataContainer());
    }

    public boolean equals(PersistentDataContainer container, C value) {
        return Objects.equals(read(container), value);
    }

    public boolean equals(PersistentDataContainerView container, C value) {
        return Objects.equals(read(container), value);
    }

    public boolean equals(ItemStack item, C value) {
        return Objects.equals(read(item), value);
    }

    public C read(PersistentDataContainer container, C def) {
        return container.getOrDefault(key, type, def);
    }

    public C read(PersistentDataHolder holder, C def) {
        return read(holder.getPersistentDataContainer(), def);
    }

    public C read(ItemStack item, C def) {
        return read(item.getItemMeta(), def);
    }

    public void write(PersistentDataHolder holder, C value) {
        write(holder.getPersistentDataContainer(), value);
    }

    public void write(PersistentDataContainer container, C value) {
        container.set(key, type, value);
    }

}
