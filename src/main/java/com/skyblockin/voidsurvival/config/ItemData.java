package com.skyblockin.voidsurvival.config;


import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.enchantment.ItemEnchantmentMap;
import com.skyblockin.voidsurvival.storage.Accessors;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
@JsonDeserialize(using = ItemData.Deserializer.class)
public final class ItemData {

    public NumberValue amount = new NumberValue(1);
    public boolean unbreakable = false;
    public boolean hideToolTip = false;
    public boolean placeable = false;
    public PotionContents potionContents = null;
    public Boolean glintOverride = false;
    public Integer maxStackSize = null;
    public Integer maxDamage = null;
    public NumberValue damage = null;
    public DyedItemColor dyedItemColor = null;
    public Component name = null;
    public ItemLore lore = null;
    public TooltipDisplay tooltipDisplay = null;

    public WritableBookContent writableBookContent = null;
    public WrittenBookContent writtenBookContent = null;

    public ItemType type = ItemType.STONE;
    public Equippable equippable = null;
    public ResolvableProfile profile = null;
    public ArmorTrim trim = null;

    public DamageResistant resistant = null;

    public Tool tool = null;
    public FoodProperties foodProperties;
    public Consumable consumable;

    public ItemAttributeModifiers attributeModifiers = null;
    public ItemEnchantmentMap enchantments = null;
    public ItemEnchantmentMap storedEnchantments = null;

    public ItemAdventurePredicate canPlaceOn = null;

    // Extra JsonNode for storing any "item_specific" data that doesn't make sense to parse here
    public JsonNode customData = null;

    public ItemStack createItem() {

        ItemStack item = type.createItemStack(amount.get());

        // Item appearance things
        setData(item, DataComponentTypes.CUSTOM_NAME, name);
        setData(item, DataComponentTypes.LORE, lore);
        setData(item, DataComponentTypes.MAX_STACK_SIZE, maxStackSize);
        setData(item, DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glintOverride);
        setData(item, DataComponentTypes.DYED_COLOR, dyedItemColor);
        setData(item, DataComponentTypes.PROFILE, profile);
        setData(item, DataComponentTypes.TOOLTIP_DISPLAY, tooltipDisplay);

        // Item consumable things
        setData(item, DataComponentTypes.POTION_CONTENTS, potionContents);
        setData(item, DataComponentTypes.FOOD, foodProperties);
        setData(item, DataComponentTypes.CONSUMABLE, consumable);
        // setData(item, DataComponentTypes.DEATH_PROTECTION, null);

        // Item durability things
        setData(item, DataComponentTypes.DAMAGE_RESISTANT, resistant);
        setData(item, DataComponentTypes.DAMAGE, damage);
        setData(item, DataComponentTypes.MAX_DAMAGE, maxDamage);
        setFlag(item, DataComponentTypes.UNBREAKABLE, unbreakable);

        // Item container things
        setData(item, DataComponentTypes.ENCHANTMENTS, enchantments);
        setData(item, DataComponentTypes.STORED_ENCHANTMENTS, storedEnchantments);
        setData(item, DataComponentTypes.ATTRIBUTE_MODIFIERS, attributeModifiers);
        setData(item, DataComponentTypes.POTION_CONTENTS, potionContents);
        setData(item, DataComponentTypes.WRITABLE_BOOK_CONTENT, writableBookContent);
        setData(item, DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContent);

        // Placeable, tool, etc "rules
        setData(item, DataComponentTypes.CAN_PLACE_ON, canPlaceOn);
        setData(item, DataComponentTypes.TOOL, tool);
        setData(item, DataComponentTypes.EQUIPPABLE, equippable);

        if (!placeable) {
            Accessors.CAN_PLACE.write(item, false);
        }

        setCustomData(item);

        return item;
    }

    private void setCustomData(ItemStack item) {

        if (customData == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        customData.fields().forEachRemaining(entry -> {

            String key = entry.getKey();
            JsonNode value = entry.getValue();
            NamespacedKey namespacedKey = new NamespacedKey("voidsurvival", key);

            if (value.isInt()) {
                data.set(namespacedKey, PersistentDataType.INTEGER, value.asInt());
            } else if (value.isFloatingPointNumber()) {
                data.set(namespacedKey, PersistentDataType.DOUBLE, value.asDouble());
            } else if (value.isBoolean()) {
                data.set(namespacedKey, PersistentDataType.BOOLEAN, value.asBoolean());
            } else if (value.isTextual()) {
                data.set(namespacedKey, PersistentDataType.STRING, value.asText());
            }

        });

        item.setItemMeta(meta);
    }

    private void setFlag(ItemStack item, DataComponentType.NonValued key, boolean value) {
        if (value) {
            item.setData(key);
        }
    }

    private <T> void setData(ItemStack item, DataComponentType.Valued<@NotNull T> key, @Nullable Gettable<T> value) {
        if (value != null) {
            item.setData(key, value.get());
        }
    }

    private <T> void setData(ItemStack item, DataComponentType.Valued<@NotNull T> key, @Nullable T value) {
        if (value != null) {
            item.setData(key, value);
        }
    }

    public static class Deserializer extends StdDeserializer<ItemData> {

        public Deserializer() {
            super(ItemData.class);
        }

        @Override
        public ItemData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            ItemData data = new ItemData();

            data.amount = Json.convert(node.path("amount"), NumberValue.class, new NumberValue(1));
            data.unbreakable = node.path("unbreakable").asBoolean(false);
            data.hideToolTip = node.path("hide_tooltip").asBoolean(false);
            data.placeable = node.path("placeable").asBoolean(false);
            data.foodProperties = Json.convert(node.get("food"), FoodProperties.class);
            data.tool = Json.convert(node.get("tool"), Tool.class);
            data.consumable = Json.convert(node.get("consumable"), Consumable.class);

            data.name = Json.convert(node.get("name"), Component.class);
            data.type = Json.convert(node.get("id"), ItemType.class);

            data.trim = Json.convert(node.get("trim"), ArmorTrim.class);
            data.maxStackSize = Json.convert(node.get("max_stack_size"), Integer.class);
            data.glintOverride = Json.convert(node.get("glint"), Boolean.class);
            data.damage = Json.convert(node.get("damage"), NumberValue.class);
            data.maxDamage = Json.convert(node.get("max_damage"), Integer.class);

            data.writableBookContent = Json.convert(node.get("writable_book_content"), WritableBookContent.class);
            data.writtenBookContent = Json.convert(node.get("written_book_content"), WrittenBookContent.class);
            data.storedEnchantments = Json.convert(node.get("stored_enchantments"), ItemEnchantmentMap.class);
            data.enchantments = Json.convert(node.get("enchantments"), ItemEnchantmentMap.class);
            data.potionContents = Json.convert(node.get("potion_contents"), PotionContents.class);
            data.attributeModifiers = Json.convert(node.get("attributes"), ItemAttributeModifiers.class);

            data.tooltipDisplay = Json.convert(node.get("hidden_components"), TooltipDisplay.class);
            data.resistant = Json.convert(node.get("damage_resistant"), DamageResistant.class);
            data.equippable = Json.convert(node.get("equippable"), Equippable.class);
            data.lore = Json.convert(node.get("lore"), ItemLore.class);
            data.dyedItemColor = Json.convert(node.get("color"), DyedItemColor.class);
            data.canPlaceOn = Json.convert(node.get("can_place_on"), ItemAdventurePredicate.class);

            data.customData = node.get("custom_data");

            JsonNode texture = node.get("base64");

            if (texture != null) {

                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "ItemTexture");

                profile.setProperty(new ProfileProperty("textures", texture.asText()));

                data.profile = ResolvableProfile.resolvableProfile(profile);
            }

            return data;
        }

    }


}
