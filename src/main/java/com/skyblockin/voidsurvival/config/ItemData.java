package com.skyblockin.voidsurvival.config;


import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.tag.DamageTypeTags;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@JsonDeserialize(using = ItemData.Deserializer.class)
public final class ItemData {

    // Whether the item should be given a unique identifier (UUID) and an obtain date, this will also cause the item to not stack
    public boolean unique = false;

    public int amount = 1;
    public boolean unbreakable = false;
    public boolean fireResistant = false;
    public boolean hideToolTip = false;
    public boolean placeable = false;
    public List<PotionEffect> effects = null;
    public Boolean glintOverride = false;
    public Integer maxStackSize = null;
    public Integer maxDamage = null;
    public Integer damage = null;
    public Color color = null;
    public Component name = Component.text("Empty");
    public List<Component> lore = null;
    public List<String> rawLore = null;

    // BookMeta start
    public Component bookAuthor = null;
    public Component bookTitle = null;
    public List<Component> bookPages = null;
    public BookMeta.Generation bookGeneration = null;
    // BookMeta end

    public ItemType type = ItemType.STONE;
    public List<EquipmentSlot> validSlots = null;
    public ItemFlag[] itemFlags = null;
    public PlayerProfile profile = null;
    public ArmorTrim trim = null;
    public FoodComponentInstructions foodComponent = null;
    public ToolComponentInstructions toolComponent = null;

    public Multimap<Attribute, AttributeModifier> attributeModifiers = MultimapBuilder.hashKeys().arrayListValues().build();
    public HashMap<Enchantment, Integer> enchantments = null;
    public HashMap<Enchantment, Integer> storedEnchantments = null;

    // Extra JsonNode for storing any "item_specific" data that doesn't make sense to parse here
    public JsonNode special = null;

    public ItemStack createItem() {

        ItemStack item = type.createItemStack(amount);

        item.editMeta(this::editMeta);

        return item;
    }

    public void editMeta(ItemMeta meta) {

        meta.displayName(name);

        if (maxStackSize != null) meta.setMaxStackSize(maxStackSize);
        if (glintOverride != null) meta.setEnchantmentGlintOverride(glintOverride);
        if (toolComponent != null) meta.setTool(toolComponent.apply(meta.getTool()));
        if (foodComponent != null) meta.setFood(foodComponent.apply(meta.getFood()));
        if (lore != null) meta.lore(lore);
        if (profile != null && meta instanceof SkullMeta skullMeta) skullMeta.setPlayerProfile(profile);
        if (trim != null && meta instanceof ArmorMeta armorMeta) armorMeta.setTrim(trim);

        if (meta instanceof Damageable damageable) {
            if (maxDamage != null) damageable.setMaxDamage(maxDamage);
            if (damage != null) damageable.setDamage(damage);
        }

        if (storedEnchantments != null && meta instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
            storedEnchantments.forEach((k, v) -> enchantmentStorageMeta.addStoredEnchant(k, v, true));
        }

        if (meta instanceof PotionMeta potionMeta) {
            for (PotionEffect effect : effects) {
                potionMeta.addCustomEffect(effect, true);
            }
        }

        if (meta instanceof BookMeta bookMeta) {
            bookMeta.title(bookTitle);
            bookMeta.author(bookAuthor);
            bookMeta.setGeneration(bookGeneration);
            if (bookPages != null) bookMeta.addPages(bookPages.toArray(new Component[0]));
        }

        if (color != null && meta instanceof LeatherArmorMeta leatherArmorMeta) leatherArmorMeta.setColor(color);

        if (enchantments != null && !enchantments.isEmpty()) {
            enchantments.forEach((k, v) -> meta.addEnchant(k, v, true));
        }

        if (itemFlags != null) meta.addItemFlags(itemFlags);
        if (attributeModifiers != null) meta.setAttributeModifiers(attributeModifiers);

        meta.setUnbreakable(unbreakable);
        meta.setDamageResistant(DamageTypeTags.IS_FIRE);
        meta.setHideTooltip(hideToolTip);
    }

    public static class Deserializer extends StdDeserializer<ItemData> {

        public Deserializer() {
            super(ItemData.class);
        }

        @Override
        public ItemData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            ItemData data = new ItemData();

            data.unique = node.path("unique").asBoolean(false);
            data.amount = node.path("amount").asInt(1);
            data.unbreakable = node.path("unbreakable").asBoolean(false);
            data.fireResistant = node.path("fire_resistant").asBoolean(false);
            data.hideToolTip = node.path("hide_tooltip").asBoolean(false);
            data.placeable = node.path("placeable").asBoolean(false);
            data.foodComponent = Json.convert(node.get("food"), FoodComponentInstructions.class);
            data.toolComponent = Json.convert(node.get("tool"), ToolComponentInstructions.class);
            data.color = Json.colorFromJson(node.get("color"));
            data.name = Json.convert(node.get("name"), Component.class);
            data.type = Json.convert(node.get("id"), ItemType.class);
            data.rawLore = Json.convert(node.get("lore"), new TypeReference<>(){});
            data.lore = Json.convert(node.get("lore"), new TypeReference<>(){});
            data.storedEnchantments = Json.convert(node.get("stored_enchantments"), new TypeReference<>(){});
            data.enchantments = Json.convert(node.get("enchantments"), new TypeReference<>(){});
            data.validSlots = Json.convert(node.get("slots"), new TypeReference<>(){});
            data.itemFlags = Json.convert(node.get("item_flags"), new TypeReference<>(){});
            data.trim = Json.convert(node.get("trim"), ArmorTrim.class);
            data.maxStackSize = Json.convert(node.get("max_stack_size"), Integer.class);
            data.glintOverride = Json.convert(node.get("glint"), Boolean.class);
            data.damage = Json.convert(node.get("damage"), Integer.class);
            data.maxDamage = Json.convert(node.get("max_damage"), Integer.class);
            data.effects = Json.convert(node.get("potion_effects"), new TypeReference<>(){});
            data.special = node.get("item_specific");

            data.bookAuthor = Json.convert(node.get("book_author"), Component.class);
            data.bookPages = Json.convert(node.get("book_pages"), new TypeReference<>(){});
            data.bookTitle = Json.convert(node.get("book_title"), Component.class);
            data.bookGeneration = Json.convert(node.get("book_generation"), BookMeta.Generation.class);

            JsonNode attributes = node.get("attributes");

            if (attributes != null) {

                data.attributeModifiers.clear();

                attributes.fields().forEachRemaining(entry -> {

                    Attribute attribute = Json.convert(entry.getKey(), Attribute.class);
                    AttributeModifier modifier = Json.convert(entry.getValue(), AttributeModifier.class);

                    data.attributeModifiers.put(attribute, modifier);
                });

            }

            JsonNode texture = node.get("base64");

            if (texture != null) {

                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "ItemTexture");

                profile.setProperty(new ProfileProperty("textures", texture.asText()));

                data.profile = profile;
            }

            return data;
        }

    }


}
