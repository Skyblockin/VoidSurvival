package com.skyblockin.voidsurvival.config;


import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.enchantment.ItemEnchantmentMap;
import com.skyblockin.voidsurvival.nms.SpawnerData;
import com.skyblockin.voidsurvival.storage.Accessors;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.Repairable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
@JsonDeserialize(using = ItemData.Deserializer.class)
public final class ItemData {

    public transient ItemType id = null;
    public RangedValue amount = null;
    public Integer maxStackSize = null;

    // Durability
    public Integer maxDamage = null;
    public RangedValue damage = null;
    public Boolean unbreakable = null;

    // Uhh idk what to call these
    public ItemRarity rarity = null;
    public Component name = null;
    public ItemLore lore = null;
    public ItemAttributeModifiers attributes = null;
    public ItemEnchantmentMap enchantments = null;
    public ItemEnchantmentMap storedEnchantments = null;
    public TooltipDisplay tooltipDisplay = null;

    public Integer repairCost = null;
    public Boolean glint = null;
    public Boolean intangibleProjectile = null;

    // Consumable things
    public FoodProperties foodProperties;
    public Consumable consumable;
    public UseRemainder useRemainder = null;
    public UseCooldown useCooldown = null;

    // Item behavior/rules
    public DamageResistant resistant = null;
    public Tool tool = null;
    public Enchantable enchantable = null;
    public Equippable equippable = null;
    public Repairable repairable = null;
    public Boolean glider = null;
    public Key tooltipStyle = null;
    public DeathProtection deathProtection = null;
    public BlocksAttacks blocksAttacks = null;
    public DyedItemColor dyedItemColor = null;

    // Storage stuff
    public PotionContents potionContents = null;
    public WritableBookContent writableBookContent = null;
    public WrittenBookContent writtenBookContent = null;
    public ItemArmorTrim trim = null;
    public ResolvableProfile profile = null;
    public ItemContainerContents containerContents = null;

    public Key breakSound = null;

    public Weapon weapon = null;

    public Boolean placeable = null;
    public ItemAdventurePredicate canPlaceOn = null;
    public ItemAdventurePredicate canBreak = null;

    public SpawnerData spawnerData = null;

    // Extra JsonNode for storing any "item_specific" data that doesn't make sense to parse here
    public JsonNode customData = null;
    public transient JsonNode rawData = null;

    public ItemStack createItem() {

        ItemStack item = id.createItemStack(amount.get());

        if (spawnerData != null) {
            item = spawnerData.applyNmsTag(item);
        }

        // Item appearance things
        setData(item, DataComponentTypes.CUSTOM_NAME, name);
        setData(item, DataComponentTypes.LORE, lore);
        setData(item, DataComponentTypes.MAX_STACK_SIZE, maxStackSize);
        setData(item, DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);
        setData(item, DataComponentTypes.DYED_COLOR, dyedItemColor);
        setData(item, DataComponentTypes.PROFILE, profile);
        setData(item, DataComponentTypes.TOOLTIP_DISPLAY, tooltipDisplay);
        setData(item, DataComponentTypes.TOOLTIP_STYLE, tooltipStyle);
        setData(item, DataComponentTypes.TRIM, trim);

        // Item consumable things
        setData(item, DataComponentTypes.POTION_CONTENTS, potionContents);
        setData(item, DataComponentTypes.FOOD, foodProperties);
        setData(item, DataComponentTypes.CONSUMABLE, consumable);
        setData(item, DataComponentTypes.DEATH_PROTECTION, deathProtection);
        setData(item, DataComponentTypes.USE_REMAINDER, useRemainder);
        setData(item, DataComponentTypes.USE_COOLDOWN, useCooldown);
        setData(item, DataComponentTypes.REPAIRABLE, repairable);
        setData(item, DataComponentTypes.BLOCKS_ATTACKS, blocksAttacks);
        setData(item, DataComponentTypes.CONTAINER, containerContents);
        setData(item, DataComponentTypes.ENCHANTABLE, enchantable);

        setData(item, DataComponentTypes.WEAPON, weapon);
        setData(item, DataComponentTypes.TOOLTIP_STYLE, tooltipStyle);
        setData(item, DataComponentTypes.BREAK_SOUND, breakSound);
        setData(item, DataComponentTypes.RARITY, rarity);
        setData(item, DataComponentTypes.REPAIR_COST, repairCost);

        // Item durability things
        setData(item, DataComponentTypes.DAMAGE_RESISTANT, resistant);
        setData(item, DataComponentTypes.DAMAGE, damage);
        setData(item, DataComponentTypes.MAX_DAMAGE, maxDamage);
        setFlag(item, DataComponentTypes.UNBREAKABLE, unbreakable);

        // Item container things
        setData(item, DataComponentTypes.ENCHANTMENTS, enchantments);
        setData(item, DataComponentTypes.STORED_ENCHANTMENTS, storedEnchantments);
        setData(item, DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);
        setData(item, DataComponentTypes.POTION_CONTENTS, potionContents);
        setData(item, DataComponentTypes.WRITABLE_BOOK_CONTENT, writableBookContent);
        setData(item, DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContent);

        // Placeable, tool, etc "rules
        setData(item, DataComponentTypes.CAN_PLACE_ON, canPlaceOn);
        setData(item, DataComponentTypes.CAN_BREAK, canBreak);
        setData(item, DataComponentTypes.TOOL, tool);
        setData(item, DataComponentTypes.EQUIPPABLE, equippable);
        setFlag(item, DataComponentTypes.GLIDER, glider);
        setFlag(item, DataComponentTypes.INTANGIBLE_PROJECTILE, intangibleProjectile);

        if (!placeable) {
            Accessors.CAN_PLACE.write(item, false);
        }

        setCustomData(item);

        return item;
    }

    public ItemData copy() {
        return Json.convert(rawData, ItemData.class);
    }

    private void setCustomData(ItemStack item) {

        if (customData == null) {
            return;
        }

        item.editPersistentDataContainer(data -> {

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

        });
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

        private static final HashMap<String, Object> DEFAULT_VALUES = new HashMap<>() {{
            put("unbreakable", false);
            put("amount", new RangedValue(1));
            put("intangible_projectile", false);
            put("placeable", true);
            put("id", ItemType.STONE);
            put("glider", false);
        }};

        public Deserializer() {
            super(ItemData.class);
        }

        private String javaFieldNameToJsonKey(String javaFieldName) {
            return javaFieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
        }

        private void setAllFields(ItemData object, JsonNode node) {

            for (Field field : ItemData.class.getDeclaredFields()) {

                if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                try {

                    String jsonKey = javaFieldNameToJsonKey(field.getName());

                    if (node.has(jsonKey)) {
                        field.set(object, Json.convert(node.get(jsonKey), field.getType()));
                    } else if (field.get(object) == null) {
                        // Only set the default value if the field has not been set yet
                        field.set(object, DEFAULT_VALUES.get(jsonKey));
                    }

                } catch (IllegalAccessException ex) {
                    VoidSurvival.logError("Failed to access field '%s' in ItemData!", field.getName());
                }
            }

        }

        @Override
        public ItemData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            // Allow the use of custom item ids for items
            String id = node.get("id").asText();

            ItemData data;

            if (id.startsWith("voidsurvival:")) {
                id = id.substring(13);
                data = VoidSurvival.getInstance().getItemManager().getItem(id);
                if (data == null) {
                    throw new IllegalStateException("Item with id '" + node.get("id").asText() + "' was specified, but no such item exists!");
                }
                data = data.copy();
            } else {
                data = new ItemData();
                data.id = Json.convert(node.get("id"), ItemType.class, ItemType.STONE);
            }

            setAllFields(data, node);
            data.rawData = node;

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
