package com.skyblockin.voidsurvival.loot;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.config.ItemData;
import com.skyblockin.voidsurvival.config.Json;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

@JsonDeserialize(using = LootTable.Deserializer.class)
public class LootTable {

    private final WeightedCollection<ItemData> items = new WeightedCollection<>();

    public void addItem(String id, ItemData item, double weight) {
        this.items.add(new Weighted<>(id, item, weight));
    }

    public ItemStack[] fill(int size, double lootBonus) {

        ItemStack[] items = new ItemStack[size];

        for (int i = 0; i < size; i++) {
            items[i] = this.items.choose(lootBonus).createItem();
        }

        return items;
    }

    public static class Deserializer extends StdDeserializer<LootTable> {

        protected Deserializer() {
            super(LootTable.class);
        }

        @Override
        public LootTable deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            LootTable table = new LootTable();

            node.fields().forEachRemaining(entry -> {

                String id = entry.getKey();
                JsonNode itemNode = entry.getValue();

                double weight = itemNode.get("weight").asDouble();
                ItemData itemData = Json.convert(itemNode.get("item"), ItemData.class);

                table.addItem(id, itemData, weight);
            });

            return table;
        }

    }

}
