package com.skyblockin.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.skyblockin.storage.MissingBlockData;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

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

        module.addSerializer(Keyed.class, new StdDelegatingSerializer(new KeyedTypeConverter()));
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

}
