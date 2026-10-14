package com.skyblockin.voidsurvival.gathering;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.item.ItemData;
import com.skyblockin.voidsurvival.item.ItemManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.io.IOException;
import java.util.function.Supplier;

@JsonDeserialize(using = GatheringNodeType.Deserializer.class)
public class GatheringNodeType {

    public static GatheringNodeType getById(String id) {
        return VoidSurvival.getInstance().getGatheringNodeManager().getGatheringNodeType(id);
    }

    private final String id;
    private final float toughness;
    public final int requiredToolTier;
    public final int requiredToolType;
    private final int regenerationTimeTicks;
    private final Supplier<ItemStack> reward;

    public GatheringNodeType(String id, float toughness, int regenerationTimeTicks, int toolTier, int toolType, Supplier<ItemStack> reward) {
        this.id = id;
        this.toughness = toughness;
        this.regenerationTimeTicks = regenerationTimeTicks;
        this.requiredToolTier = toolTier;
        this.requiredToolType = toolType;
        this.reward = reward;
    }

    public String getId() {
        return id;
    }

    public float getToughness() {
        return toughness;
    }

    public int getRegenerationTimeTicks() {
        return regenerationTimeTicks;
    }

    public ItemStack getReward() {
        return reward.get();
    }

    public static class Deserializer extends StdDeserializer<GatheringNodeType> {

        protected Deserializer() {
            super(GatheringNodeType.class);
        }

        @Override
        public GatheringNodeType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            String id = node.get("id").asText();
            float toughness = (float) node.get("toughness").asDouble();
            int regenerationTimeTicks = node.get("regeneration_time_ticks").asInt();

            int toolTier = node.path("tool_tier").asInt(-1);
            int toolType = node.path("tool_type").asInt(-1);

            String reward = node.get("reward").asText();

            Supplier<ItemStack> supplier;

            if (reward.startsWith("minecraft:")) {
                ItemType type = Json.convert(node.get("reward"), ItemType.class);
                supplier = type::createItemStack;
            } else {
                ItemData data = Json.convert(node.get("reward"), ItemData.class);
                if (data == null) {
                    return null;
                }
                supplier = data::createItem;
            }

            return new GatheringNodeType(id, toughness, regenerationTimeTicks, toolTier, toolType, supplier);
        }
    }
}
