package com.skyblockin.voidsurvival.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.item.*;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);

        MAPPER.setVisibility(
            MAPPER.getDeserializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        );

        SimpleModule module = new SimpleModule();

        module.addDeserializer(Enchantment.class, JsonDeserializers.ENCHANTMENT);
        module.addDeserializer(Attribute.class, JsonDeserializers.ATTRIBUTE);
        module.addDeserializer(Sound.class, JsonDeserializers.SOUND);
        module.addDeserializer(TrimMaterial.class, JsonDeserializers.TRIM_MATERIAL);
        module.addDeserializer(TrimPattern.class, JsonDeserializers.TRIM_PATTERN);
        module.addDeserializer(PotionEffectType.class, JsonDeserializers.POTION_EFFECT_TYPE);
        module.addDeserializer(EntityType.class, JsonDeserializers.ENTITY_TYPE);
        module.addDeserializer(BlockType.class, JsonDeserializers.BLOCK_TYPE);
        module.addDeserializer(ItemType.class, JsonDeserializers.ITEM_TYPE);

        module.addDeserializer(ArmorTrim.class, JsonDeserializers.ARMOR_TRIM);
        module.addDeserializer(Component.class, JsonDeserializers.COMPONENT);
        module.addDeserializer(EquipmentSlot.class, JsonDeserializers.EQUIPMENT_SLOT);
        module.addDeserializer(BookMeta.Generation.class, JsonDeserializers.BOOK_GENERATION);
        module.addDeserializer(EquipmentSlotGroup.class, JsonDeserializers.EQUIPMENT_SLOT_GROUP);
        module.addDeserializer(ItemFlag.class, JsonDeserializers.ITEM_FLAG);
        module.addDeserializer(Vector3f.class, JsonDeserializers.VECTOR_3F);
        module.addDeserializer(AttributeModifier.class, JsonDeserializers.ATTRIBUTE_MODIFIER);
        module.addDeserializer(UUID.class, JsonDeserializers.UUID);
        module.addDeserializer(Color.class, JsonDeserializers.COLOR);
        module.addDeserializer(PotionEffect.class, JsonDeserializers.POTION_EFFECT);
        module.addDeserializer(ItemEnchantments.class, JsonDeserializers.ITEM_ENCHANTMENTS);
        module.addDeserializer(FoodProperties.class, JsonDeserializers.FOOD_PROPERTIES);
        module.addDeserializer(Tool.class, JsonDeserializers.TOOL);
        module.addDeserializer(Consumable.class, JsonDeserializers.CONSUMABLE);
        module.addDeserializer(DyedItemColor.class, JsonDeserializers.DYED_ITEM_COLOR);
        module.addDeserializer(ItemAttributeModifiers.class, JsonDeserializers.ITEM_ATTRIBUTE_MODIFIERS);
        module.addDeserializer(ItemLore.class, JsonDeserializers.ITEM_LORE);
        module.addDeserializer(Equippable.class, JsonDeserializers.EQUIPPABLE);
        module.addDeserializer(BlockPredicate.class, JsonDeserializers.BLOCK_PREDICATE);
        module.addDeserializer(ItemAdventurePredicate.class, JsonDeserializers.ITEM_BLOCK_PREDICATE);
        module.addDeserializer(PotionContents.class, JsonDeserializers.POTION_CONTENTS);
        module.addDeserializer(DamageResistant.class, JsonDeserializers.DAMAGE_RESISTANT);
        module.addDeserializer(TooltipDisplay.class, JsonDeserializers.TOOLTIP_DISPLAY);
        module.addDeserializer(WritableBookContent.class, JsonDeserializers.WRITABLE_BOOK_CONTENT);
        module.addDeserializer(WrittenBookContent.class, JsonDeserializers.WRITTEN_BOOK_CONTENT);

        module.addKeyDeserializer(NamespacedKey.class, JsonDeserializers.NAMESPACED_KEY);
        module.addKeyDeserializer(Attribute.class, JsonDeserializers.ATTRIBUTE_KEY);
        module.addKeyDeserializer(Enchantment.class, JsonDeserializers.ENCHANTMENT_KEY);

        module.addSerializer(Keyed.class, new StdDelegatingSerializer(JsonDeserializers.KEYED_TYPE));
        module.addKeySerializer(Keyed.class, new StdDelegatingSerializer(JsonDeserializers.KEYED_TYPE));

        MAPPER.registerModule(module);
    }

    public static void writeToFile(File file, Object object) throws IOException {
        MAPPER.writer().writeValue(file, object);
    }

    public static JsonNode readFromFile(File file) throws IOException {
        return MAPPER.readTree(file);
    }

    public static <T> T readFromFile(File file, Class<T> reference) throws IOException {
        return MAPPER.reader().readValue(file, reference);
    }

    public static <T> T convert(Object node, Class<T> type) {
        return MAPPER.convertValue(node, type);
    }

    public static <T> T convert(Object node, Class<T> type, T defaultValue) {

        T value = MAPPER.convertValue(node, type);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static <T> T convert(Object node, TypeReference<T> type) {
        return MAPPER.convertValue(node, type);
    }



    public static JsonNode toJson(Object object) {
        return MAPPER.valueToTree(object);
    }

    public static JsonNode readJson(String json) throws JsonProcessingException {
        return MAPPER.readTree(json);
    }

    public static <T> T nodeToValue(JsonNode node, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.treeToValue(node, clazz);
    }

    public static <T> T nodeToValue(JsonNode node, Class<T> clazz, T fallback) {
        try {
            return MAPPER.treeToValue(node, clazz);
        } catch (JsonProcessingException ex) {
            return fallback;
        }
    }

    public static <T> T stringToValue(String json, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.readValue(json, clazz);
    }

    public static Color colorFromJson(JsonNode node) {

        if (node == null) {
            return null;
        }

        if (node.isNumber()) {
            return Color.fromRGB(node.asInt());
        } else if (node.isTextual()) {

            String text = node.asText();

            if (text.startsWith("#")) {
                text = text.substring(1);
            }

            return Color.fromRGB(Integer.parseInt(text, 16));
        } else if (node.isObject()) {
            return Color.fromRGB(node.get("r").asInt(), node.get("g").asInt(), node.get("b").asInt());
        }

        throw new IllegalArgumentException(
            "Unable to parse Color from Json! Make sure the color is either a base 10 integer, a hex string or a Json object with integer fields r, g and b"
        );
    }


}
