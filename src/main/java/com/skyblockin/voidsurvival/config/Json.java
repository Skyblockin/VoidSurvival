package com.skyblockin.voidsurvival.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.skyblockin.voidsurvival.VoidSurvival;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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

        module.addDeserializer(Material.class, delegate(new RegistryConverter<>("Material", Registry.MATERIAL)));
        module.addDeserializer(Enchantment.class, delegate(new RegistryConverter<>("Enchantment", RegistryKey.ENCHANTMENT)));
        module.addDeserializer(Sound.class, delegate(new RegistryConverter<>("Sound", Registry.SOUNDS)));
        module.addDeserializer(TrimMaterial.class, delegate(new RegistryConverter<>("TrimMaterial", RegistryKey.TRIM_MATERIAL)));
        module.addDeserializer(TrimPattern.class, delegate(new RegistryConverter<>("TrimPattern", RegistryKey.TRIM_PATTERN)));
        module.addDeserializer(PotionEffectType.class, delegate(new RegistryConverter<>("PotionEffectType", Registry.POTION_EFFECT_TYPE)));
        module.addDeserializer(EntityType.class, delegate(new RegistryConverter<>("EntityType", Registry.ENTITY_TYPE)));
        module.addDeserializer(BlockType.class, delegate(new RegistryConverter<>("Block", Registry.BLOCK)));
        module.addDeserializer(ItemType.class, delegate(new RegistryConverter<>("ItemType", Registry.ITEM)));

        module.addDeserializer(ArmorTrim.class, new ArmorTrimDeserializer());
        module.addDeserializer(Component.class, delegate(new ComponentConverter()));
        module.addDeserializer(EquipmentSlot.class, delegate(new EnumConverter<>(EquipmentSlot.class)));
        module.addDeserializer(BookMeta.Generation.class, delegate(new EnumConverter<>(BookMeta.Generation.class)));
        module.addDeserializer(EquipmentSlotGroup.class, delegate(new EquipmentSlotGroupConverter()));
        module.addDeserializer(ItemFlag.class, delegate(new EnumConverter<>(ItemFlag.class)));
        module.addDeserializer(Vector3f.class, new Vector3fDeserializer());
        module.addDeserializer(AttributeModifier.class, new AttributeModifierDeserializer());
        module.addDeserializer(UUID.class, delegate(new UUIDConverter()));
        module.addDeserializer(PotionEffect.class, new PotionEffectDeserializer());

        module.addKeyDeserializer(NamespacedKey.class, new NamespacedKeyKeyDeserializer());
        module.addKeyDeserializer(Attribute.class, new AttributeKeyDeserializer());
        module.addKeyDeserializer(Enchantment.class, new EnchantmentKeyDeserializer());

        module.addSerializer(Keyed.class, new StdDelegatingSerializer(new KeyedTypeConverter()));
        module.addKeySerializer(Keyed.class, new StdDelegatingSerializer(new KeyedTypeConverter()));

        MAPPER.registerModule(module);
    }

    private static <T> StdDelegatingDeserializer<T> delegate(Converter<?, T> converter) {
        return new StdDelegatingDeserializer<>(converter);
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

    public static class EnumConverter<T extends Enum<T>> extends StdConverter<String, T> {

        private final String typeName;
        private final Class<T> declaringClass;

        public EnumConverter(Class<T> enumClass) {
            this.declaringClass = enumClass;
            this.typeName = this.declaringClass.getSimpleName();
        }

        @Override
        public T convert(String value) {

            try {
                return Enum.valueOf(declaringClass, value.trim().toUpperCase());
            } catch (Exception ex) {
                VoidSurvival.logError("Error while parsing %s from %s, is it a valid ?", typeName, value, typeName);
                return null;
            }

        }
    }

    public static class RegistryConverter<T extends Keyed> extends StdConverter<String, T> {

        private final String name;
        private final Registry<@NotNull T> registry;

        public RegistryConverter(String name, Registry<@NotNull T> registry) {
            this.name = name;
            this.registry = registry;
        }

        public RegistryConverter(String name, RegistryKey<@NotNull T> key) {
            this.name = name;
            this.registry = RegistryAccess.registryAccess().getRegistry(key);
        }

        public NamespacedKey convertNamespacedKey(String value) {

            NamespacedKey key = NamespacedKey.fromString(value);

            if (key == null) {
                throw new IllegalArgumentException("Encountered invalid id while converting " + value + " into a NamespacedKey! Is it in the format namespace:key?");
            }

            return key;
        }

        @Override
        public T convert(String value) {

            NamespacedKey key = convertNamespacedKey(value);

            T registryValue = registry.get(key);

            if (registryValue == null) {
                throw new IllegalArgumentException(
                    "Encountered invalid id while converting " + value + " into a " + name + "! Is it a valid " + name.toLowerCase() + "?"
                );
            }

            return registryValue;
        }
    }

    public static class KeyedTypeConverter extends StdConverter<Keyed, String> {

        @Override
        public String convert(Keyed value) {
            return value.getKey().toString();
        }

    }

    public static class NamespacedKeyKeyDeserializer extends KeyDeserializer {

        @Override
        public NamespacedKey deserializeKey(String key, DeserializationContext ctxt) {
            return NamespacedKey.fromString(key);
        }

    }

    public static class EquipmentSlotGroupConverter extends StdConverter<String, EquipmentSlotGroup> {

        @Override
        public EquipmentSlotGroup convert(String value) {

            return switch (value.toLowerCase().replaceAll("_", "")) {
                case "any" -> EquipmentSlotGroup.ANY;
                case "mainhand" -> EquipmentSlotGroup.MAINHAND;
                case "offhand" -> EquipmentSlotGroup.OFFHAND;
                case "hand" -> EquipmentSlotGroup.HAND;
                case "feet" -> EquipmentSlotGroup.FEET;
                case "legs" -> EquipmentSlotGroup.LEGS;
                case "chest" -> EquipmentSlotGroup.CHEST;
                case "head" -> EquipmentSlotGroup.HEAD;
                case "armor" -> EquipmentSlotGroup.ARMOR;
                case "body" -> EquipmentSlotGroup.BODY;
                default -> null;
            };

        }

    }

    public static class ComponentConverter extends StdConverter<String, Component> {

        @Override
        public Component convert(String value) {
            return MiniMessage.miniMessage().deserialize(value);
        }

    }

    public static class UUIDConverter extends StdConverter<String, UUID> {

        @Override
        public UUID convert(String value) {
            return UUID.fromString(value);
        }

    }

    public static class EnchantmentKeyDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return Json.convert(key, Enchantment.class);
        }

    }

    public static class AttributeKeyDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return Json.convert(key, Attribute.class);
        }

    }

    public static class ArmorTrimDeserializer extends StdDeserializer<ArmorTrim> {

        protected ArmorTrimDeserializer() {
            super(ArmorTrim.class);
        }

        @Override
        public ArmorTrim deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            TrimMaterial material = Json.convert(node.get("material"), TrimMaterial.class);
            TrimPattern pattern = Json.convert(node.get("pattern"), TrimPattern.class);

            return new ArmorTrim(material, pattern);
        }

    }

    public static class Vector3fDeserializer extends StdDeserializer<Vector3f> {

        protected Vector3fDeserializer() {
            super(Vector3f.class);
        }

        @Override
        public Vector3f deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            Vector3f vector3f = new Vector3f();

            p.nextToken();
            vector3f.x = p.getFloatValue();
            p.nextToken();
            vector3f.y = p.getFloatValue();
            p.nextToken();
            vector3f.z = p.getFloatValue();
            p.nextToken();

            return vector3f;
        }

    }

    public static class PotionEffectDeserializer extends StdDeserializer<PotionEffect> {

        protected PotionEffectDeserializer() {
            super(PotionEffect.class);
        }

        @Override
        public PotionEffect deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            PotionEffectType type = Json.convert(node.get("type"), PotionEffectType.class);

            int duration = node.path("duration").asInt(-1);
            int level = node.path("level").asInt(1);
            boolean ambient = node.path("ambient").asBoolean(false);
            boolean particles = node.path("particles").asBoolean(true);
            boolean icon = node.path("icon").asBoolean(true);

            return new PotionEffect(type, duration, level - 1, ambient, particles, icon);
        }
    }

    public static class AttributeModifierDeserializer extends StdDeserializer<AttributeModifier> {

        protected AttributeModifierDeserializer() {
            super(AttributeModifier.class);
        }

        @Override
        public AttributeModifier deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            String name = node.get("name").asText();
            double amount = node.get("amount").asDouble();

            AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(node.get("operation").asText().toUpperCase());

            EquipmentSlotGroup slot = EquipmentSlotGroup.ANY;

            if (node.hasNonNull("slot")) {
                slot = Json.convert(node.get("slot").asText(), EquipmentSlotGroup.class);
            }

            if (slot == null) {
                VoidSurvival.logError("EquipmentSlotGroup was defined but had an invalid value: %s, defaulting to 'any'", node.get("slot"));
                VoidSurvival.logError("Possible values are any, mainhand, offhand, hand, feet, legs, chest, head, armor, body");
                slot = EquipmentSlotGroup.ANY;
            }

            return new AttributeModifier(new NamespacedKey("corex", name), amount, operation, slot);
        }

    }

}
