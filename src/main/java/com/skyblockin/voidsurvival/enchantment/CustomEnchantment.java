package com.skyblockin.voidsurvival.enchantment;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryComposeEvent;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class CustomEnchantment {

    public static CustomEnchantment ofName(String name) {
        return new CustomEnchantment(name);
    }

    private final String name;
    private int weight;
    private int maxLevel;
    private int anvilCost;
    private int minCost;
    private int minCostIncrease;
    private int maxCost;
    private int maxCostIncrease;
    private TagKey<@NotNull ItemType> primaryItems = null;
    private TagKey<@NotNull ItemType> supportedItems;
    private EquipmentSlotGroup activeSlots;

    public CustomEnchantment(String name) {
        this.name = name;
    }

    public CustomEnchantment weight(int weight) {
        this.weight = weight;
        return this;
    }

    public CustomEnchantment maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    public CustomEnchantment anvilCost(int anvilCost) {
        this.anvilCost = anvilCost;
        return this;
    }

    public CustomEnchantment minCost(int minCost, int increase) {
        this.minCost = minCost;
        this.minCostIncrease = increase;
        return this;
    }

    public CustomEnchantment maxCost(int maxCost, int increase) {
        this.maxCost = maxCost;
        this.maxCostIncrease = increase;
        return this;
    }

    public CustomEnchantment primaryItems(TagKey<@NotNull ItemType> primaryItems) {
        this.primaryItems = primaryItems;
        return this;
    }

    public CustomEnchantment supportedItems(TagKey<@NotNull ItemType> supportedItems) {
        this.supportedItems = supportedItems;
        return this;
    }

    public CustomEnchantment activeSlots(EquipmentSlotGroup activeSlots) {
        this.activeSlots = activeSlots;
        return this;
    }

    public TypedKey<@NotNull Enchantment> getKey() {
        return EnchantmentKeys.create(new NamespacedKey("voidsurvival", name.toLowerCase().replace(" ", "_")));
    }

    public Enchantment get() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(getKey());
    }

    public void register(RegistryComposeEvent<@NotNull Enchantment, EnchantmentRegistryEntry.@NotNull Builder> event) {
        event.registry().register(getKey(), builder -> builder
            .primaryItems(primaryItems == null ? null : event.getOrCreateTag(primaryItems))
            .supportedItems(event.getOrCreateTag(supportedItems))
            .description(Component.text(name))
            .weight(weight)
            .anvilCost(anvilCost)
            .maxLevel(maxLevel)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(minCost, minCostIncrease))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(maxCost, maxCostIncrease))
            .activeSlots(activeSlots));
    }
}
