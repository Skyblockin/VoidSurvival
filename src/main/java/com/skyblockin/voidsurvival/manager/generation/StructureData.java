package com.skyblockin.voidsurvival.manager.generation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.loot.Weighted;
import com.skyblockin.voidsurvival.loot.WeightedCollection;

import java.io.IOException;

public class StructureData {

    private final WeightedCollection<CustomEntityOrVanilla> entities = new WeightedCollection<>();
    public int mobCap;

    public CustomEntityOrVanilla getEntity() {
        return entities.choose();
    }

    public static class Deserializer extends StdDeserializer<StructureData> {

        protected Deserializer() {
            super(StructureData.class);
        }

        @Override
        public StructureData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            StructureData data = new StructureData();

            JsonNode potentialMobs = node.get("mobs");
            data.mobCap = node.get("mob_cap").asInt();

            potentialMobs.properties().forEach(entry -> {

                String id = entry.getKey();
                JsonNode itemNode = entry.getValue();

                double chance = itemNode.get("chance").asDouble() / 100;
                CustomEntityOrVanilla itemData = Json.convert(itemNode.get("entity"), CustomEntityOrVanilla.class);

                data.entities.add(new Weighted<>(id, itemData, chance));
            });

            return data;
        }

    }

}
