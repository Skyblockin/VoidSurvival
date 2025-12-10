package com.skyblockin.voidsurvival.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.util.Functions;
import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.util.TriState;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class JsonDeserializers {

    // Registry converters
    public static final StdDelegatingDeserializer<Enchantment> ENCHANTMENT = delegate(new RegistryConverter<>("Enchantment", RegistryKey.ENCHANTMENT));
    public static final StdDelegatingDeserializer<Attribute> ATTRIBUTE = delegate(new RegistryConverter<>("Attribute", RegistryKey.ATTRIBUTE));
    public static final StdDelegatingDeserializer<Sound> SOUND = delegate(new RegistryConverter<>("Sound", Registry.SOUNDS));
    public static final StdDelegatingDeserializer<TrimMaterial> TRIM_MATERIAL = delegate(new RegistryConverter<>("TrimMaterial", RegistryKey.TRIM_MATERIAL));
    public static final StdDelegatingDeserializer<TrimPattern> TRIM_PATTERN = delegate(new RegistryConverter<>("TrimPattern", RegistryKey.TRIM_PATTERN));
    public static final StdDelegatingDeserializer<PotionEffectType> POTION_EFFECT_TYPE = delegate(new RegistryConverter<>("PotionEffectType", Registry.POTION_EFFECT_TYPE));
    public static final StdDelegatingDeserializer<EntityType> ENTITY_TYPE = delegate(new RegistryConverter<>("EntityType", Registry.ENTITY_TYPE));
    public static final StdDelegatingDeserializer<BlockType> BLOCK_TYPE = delegate(new RegistryConverter<>("BlockType", Registry.BLOCK));
    public static final StdDelegatingDeserializer<ItemType> ITEM_TYPE = delegate(new RegistryConverter<>("ItemType", Registry.ITEM));

    // Enum converters
    public static final StdDelegatingDeserializer<BookMeta.Generation> BOOK_GENERATION = delegate(new EnumConverter<>(BookMeta.Generation.class));
    public static final StdDelegatingDeserializer<EquipmentSlot> EQUIPMENT_SLOT = delegate(new EnumConverter<>(EquipmentSlot.class));
    public static final StdDelegatingDeserializer<ItemFlag> ITEM_FLAG = delegate(new EnumConverter<>(ItemFlag.class));

    public static final StdDelegatingDeserializer<Color> COLOR = delegate(new ColorConverter());
    public static final ArmorTrimDeserializer ARMOR_TRIM = new ArmorTrimDeserializer();
    public static final Vector3fDeserializer VECTOR_3F = new Vector3fDeserializer();
    public static final PotionEffectDeserializer POTION_EFFECT = new PotionEffectDeserializer();
    public static final AttributeModifierDeserializer ATTRIBUTE_MODIFIER = new AttributeModifierDeserializer();
    public static final ItemEnchantmentsDeserializer ITEM_ENCHANTMENTS = new ItemEnchantmentsDeserializer();
    public static final FoodPropertiesDeserializer FOOD_PROPERTIES = new FoodPropertiesDeserializer();
    public static final ToolDeserializer TOOL = new ToolDeserializer();
    public static final ConsumableDeserializer CONSUMABLE = new ConsumableDeserializer();
    public static final EnchantmentKeyDeserializer ENCHANTMENT_KEY = new EnchantmentKeyDeserializer();
    public static final AttributeKeyDeserializer ATTRIBUTE_KEY = new AttributeKeyDeserializer();
    public static final NamespacedKeyKeyDeserializer NAMESPACED_KEY = new NamespacedKeyKeyDeserializer();
    public static final EquipmentSlotGroupDeserializer EQUIPMENT_SLOT_GROUP = new EquipmentSlotGroupDeserializer();
    public static final ComponentDeserializer COMPONENT = new ComponentDeserializer();
    public static final UUIDDeserializer UUID = new UUIDDeserializer();
    public static final KeyedTypeConverter KEYED_TYPE = new KeyedTypeConverter();
    public static final DyedItemColorDeserializer DYED_ITEM_COLOR = new DyedItemColorDeserializer();
    public static final ItemAttributeModifiersDeserializer ITEM_ATTRIBUTE_MODIFIERS = new ItemAttributeModifiersDeserializer();
    public static final ItemLoreDeserializer ITEM_LORE = new ItemLoreDeserializer();
    public static final EquippableDeserializer EQUIPPABLE = new EquippableDeserializer();
    public static final BlockPredicateDeserializer BLOCK_PREDICATE = new BlockPredicateDeserializer();
    public static final ItemAdventurePredicateDeserializer ITEM_BLOCK_PREDICATE = new ItemAdventurePredicateDeserializer();
    public static final PotionContentsDeserializer POTION_CONTENTS = new PotionContentsDeserializer();
    public static final DamageResistantDeserializer DAMAGE_RESISTANT = new DamageResistantDeserializer();
    public static final TooltipDisplayDeserializer TOOLTIP_DISPLAY = new TooltipDisplayDeserializer();
    public static final WrittenBookContentDeserializer WRITTEN_BOOK_CONTENT = new WrittenBookContentDeserializer();
    public static final WritableBookContentDeserializer WRITABLE_BOOK_CONTENT = new WritableBookContentDeserializer();

    private static <T> StdDelegatingDeserializer<T> delegate(Converter<?, T> converter) {
        return new StdDelegatingDeserializer<>(converter);
    }

    public static class ColorConverter extends StdConverter<JsonNode, Color> {

        @Override
        public Color convert(JsonNode node) {
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
                VoidSurvival.logError("Error while parsing %s from %s, is it a valid %s?", typeName, value, typeName);
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

    public static class EquipmentSlotGroupDeserializer extends StdDeserializer<EquipmentSlotGroup> {

        protected EquipmentSlotGroupDeserializer() {
            super(EquipmentSlotGroup.class);
        }

        @Override
        public EquipmentSlotGroup deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return switch (p.getValueAsString().toLowerCase().replaceAll("_", "")) {
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

    public static class ComponentDeserializer extends StdDeserializer<Component> {

        public Component deserialize(String value) {
            return MiniMessage.miniMessage().deserialize(value);
        }

        protected ComponentDeserializer() {
            super(Component.class);
        }

        @Override
        public Component deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return deserialize(p.getValueAsString());
        }
    }

    public static class UUIDDeserializer extends StdDeserializer<UUID> {

        protected UUIDDeserializer() {
            super(UUID.class);
        }

        @Override
        public UUID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return java.util.UUID.fromString(p.getValueAsString());
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

            return new AttributeModifier(new NamespacedKey("voidsurvival", name), amount, operation, slot);
        }

    }

    public static class ItemAttributeModifiersDeserializer extends StdDeserializer<ItemAttributeModifiers> {

        protected ItemAttributeModifiersDeserializer() {
            super(ItemAttributeModifiers.class);
        }

        @Override
        public ItemAttributeModifiers deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.itemAttributes();

            JsonNode attributes = p.getCodec().readTree(p);

            attributes.fields().forEachRemaining(entry -> {

                Attribute attribute = Json.convert(entry.getKey(), Attribute.class);
                AttributeModifier modifier = Json.convert(entry.getValue(), AttributeModifier.class);

                builder.addModifier(attribute, modifier);
            });

            return builder.build();
        }
    }

    public static class ItemEnchantmentsDeserializer extends StdDeserializer<ItemEnchantments> {

        protected ItemEnchantmentsDeserializer() {
            super(ItemEnchantments.class);
        }

        @Override
        public ItemEnchantments deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            HashMap<Enchantment, Integer> map = p.getCodec().readValue(p, new TypeReference<>(){});

            return ItemEnchantments.itemEnchantments(map);
        }
    }

    public static class FoodPropertiesDeserializer extends StdDeserializer<FoodProperties> {

        protected FoodPropertiesDeserializer() {
            super(FoodProperties.class);
        }

        @Override
        public FoodProperties deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            return FoodProperties.food()
                .nutrition(node.path("nutrition").asInt(0))
                .canAlwaysEat(node.path("can_always_eat").asBoolean(false))
                .saturation((float) node.path("saturation").asDouble(0.0))
                .build();
        }
    }

    public static class ToolDeserializer extends StdDeserializer<Tool> {

        protected ToolDeserializer() {
            super(Tool.class);
        }

        @Override
        public Tool deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            Tool.Builder builder = Tool.tool();

            JsonNode rules = node.get("rules");

            if (rules != null) {

                for (JsonNode rule : rules) {

                    try {

                        JsonNode type = rule.get("type");
                        Float defaultMiningSpeed = Json.convert(rule.get("default_mining_speed"), Float.class);
                        Boolean correctToolForDrops = Json.convert(rule.get("correct_tool"), Boolean.class);

                        builder.addRule(Tool.rule(Functions.parseRegistryKeySet(RegistryKey.BLOCK, type), defaultMiningSpeed, TriState.byBoolean(correctToolForDrops)));

                    } catch (Exception ex) {
                        VoidSurvival.logError("Failed to parse tool rule: " + rule, ex);
                    }

                }
            }

            if (node.has("damage_per_block")) {
                builder.damagePerBlock(node.path("damage_per_block").asInt(1));
            }

            if (node.has("default_mining_speed")) {
                builder.defaultMiningSpeed((float) node.get("default_mining_speed").asDouble());
            }

            return builder.build();
        }

    }

    public static class ConsumableDeserializer extends StdDeserializer<Consumable> {

        protected ConsumableDeserializer() {
            super(Consumable.class);
        }

        @Override
        public Consumable deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            JsonNode effectNode = node.get("effects");

            Consumable.Builder builder = Consumable.consumable();

            // Basic case where it's just simple effects since this will likely be the most used one

            if (effectNode.isArray()) {
                if (node.has("effect_chance")) {
                    List<PotionEffect> effects = Json.convert(effectNode, new TypeReference<>() {});
                    float probability = (float) node.get("effect_chance").asDouble(1.0);
                    builder.addEffect(ConsumeEffect.applyStatusEffects(effects, probability));
                } else {
                    for (JsonNode effect : effectNode) {
                        builder.addEffect(parseEffect(effect));
                    }
                }
            }

            return builder.hasConsumeParticles(node.get("particles").asBoolean(true))
                .consumeSeconds((float) node.get("consume_seconds").asDouble(1.6))
                .animation(node.has("animation") ? ItemUseAnimation.valueOf(node.get("animation").asText("eat").toUpperCase()) : ItemUseAnimation.EAT)
                .build();
        }

        private ConsumeEffect parseEffect(JsonNode node) {

            String type = node.get("type").asText();

            switch (type) {
                case "apply_effects" -> {

                    List<PotionEffect> effects = Json.convert(node.get("effects"), new TypeReference<>() {});
                    float probability = (float) node.get("effect_chance").asDouble(1.0);

                    return ConsumeEffect.applyStatusEffects(effects, probability);
                }
                case "remove_effects" -> {

                    List<TypedKey<@NotNull PotionEffectType>> effects = new ArrayList<>();

                    for (JsonNode value : node.get("effects")) {
                        effects.add(TypedKey.create(RegistryKey.MOB_EFFECT, value.asText()));
                    }

                    return ConsumeEffect.removeEffects(RegistrySet.keySet(RegistryKey.MOB_EFFECT, effects));
                }
                case "teleport" -> {
                    return ConsumeEffect.teleportRandomlyEffect((float) node.get("diameter").asDouble(1.0));
                }
                case "play_sound" -> {
                    return ConsumeEffect.playSoundConsumeEffect(Key.key(node.get("sound").asText()));
                }
                default -> {
                    throw new IllegalArgumentException("Unknown effect type: " + type);
                }
            }

        }

    }

    public static class DyedItemColorDeserializer extends StdDeserializer<DyedItemColor> {

        protected DyedItemColorDeserializer() {
            super(DyedItemColor.class);
        }

        @Override
        public DyedItemColor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            Color color = p.getCodec().readValue(p, Color.class);

            return DyedItemColor.dyedItemColor(color);
        }
    }

    public static class ItemLoreDeserializer extends StdDeserializer<ItemLore> {

        protected ItemLoreDeserializer() {
            super(ItemLore.class);
        }

        @Override
        public ItemLore deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            List<Component> loreLines = Json.convert(p.getCodec().readTree(p), new TypeReference<>(){});

            return ItemLore.lore().lines(loreLines).build();
        }
    }

    public static class EquippableDeserializer extends StdDeserializer<Equippable> {

        protected EquippableDeserializer() {
            super(Equippable.class);
        }

        @Override
        public Equippable deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            EquipmentSlot slots = Json.convert(node.get("valid_slot"), EquipmentSlot.class);
            Key soundKey = Key.key(node.get("equip_sound").asText());

            // TODO: Improve/expand this implementation
            return Equippable.equippable(slots)
                .equipSound(soundKey)
                .build();
        }
    }

    public static class BlockPredicateDeserializer extends StdDeserializer<BlockPredicate> {

        protected BlockPredicateDeserializer() {
            super(BlockPredicate.class);
        }

        @Override
        public BlockPredicate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            RegistryKeySet<@NotNull BlockType> blockSet = Functions.parseRegistryKeySet(RegistryKey.BLOCK, p.getCodec().readTree(p));

            return BlockPredicate.predicate().blocks(blockSet).build();
        }
    }

    public static class ItemAdventurePredicateDeserializer extends StdDeserializer<ItemAdventurePredicate> {

        protected ItemAdventurePredicateDeserializer() {
            super(ItemAdventurePredicate.class);
        }

        @Override
        public ItemAdventurePredicate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            List<BlockPredicate> predicates = p.getCodec().readValue(p, new TypeReference<>() {});

            return ItemAdventurePredicate.itemAdventurePredicate(predicates);
        }
    }

    public static class PotionContentsDeserializer extends StdDeserializer<PotionContents> {

        protected PotionContentsDeserializer() {
            super(PotionContents.class);
        }

        @Override
        public PotionContents deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            List<PotionEffect> effects = p.getCodec().readValue(p, new TypeReference<>() {});

            return PotionContents.potionContents().addCustomEffects(effects).build();
        }
    }

    public static class DamageResistantDeserializer extends StdDeserializer<DamageResistant> {

        protected DamageResistantDeserializer() {
            super(DamageResistant.class);
        }

        @Override
        public DamageResistant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return DamageResistant.damageResistant(TagKey.create(RegistryKey.DAMAGE_TYPE, p.getValueAsString()));
        }
    }

    public static class TooltipDisplayDeserializer extends StdDeserializer<TooltipDisplay> {

        protected TooltipDisplayDeserializer() {
            super(TooltipDisplay.class);
        }

        @Override
        public TooltipDisplay deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);
            DataComponentType[] hiddenComponents = new DataComponentType[node.size()];

            for (int i = 0; i < hiddenComponents.length; i++) {
                hiddenComponents[i] = DataComponentTypeList.getByKey(node.get(i).asText());
            }

            return TooltipDisplay
                .tooltipDisplay()
                .hideTooltip(node.get("hide_tooltip").asBoolean(false))
                .addHiddenComponents(hiddenComponents)
                .build();
        }
    }

    public static class WrittenBookContentDeserializer extends StdDeserializer<WrittenBookContent> {

        protected WrittenBookContentDeserializer() {
            super(WrittenBookContent.class);
        }

        @Override
        public WrittenBookContent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            String title = node.get("title").asText();
            String author = node.get("author").asText();
            int generation = node.get("generation").asInt(0);
            boolean resolved = node.get("resolved").asBoolean(true);

            List<Component> pages = Json.convert(node.get("pages"), new TypeReference<>() {});

            return WrittenBookContent.writtenBookContent(title, author)
                .addPages(pages)
                .resolved(resolved)
                .generation(generation)
                .build();
        }
    }

    public static class WritableBookContentDeserializer extends StdDeserializer<WritableBookContent> {

        protected WritableBookContentDeserializer() {
            super(WritableBookContent.class);
        }

        @Override
        public WritableBookContent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            List<String> pages = Json.convert(p.getCodec().readTree(p), new TypeReference<>() {});

            return WritableBookContent.writeableBookContent()
                .addPages(pages)
                .build();
        }
    }

}
