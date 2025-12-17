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

    // private final WeightedCollection<ItemData> items = new WeightedCollection<>();
    private final ProbabilityCollection<ItemData> items = new ProbabilityCollection<>();

    public void addItem(String id, ItemData item, double chance) {
        this.items.add(item, chance);
    }

    public ItemStack[] fill(int size, double lootMultiplier) {

        ItemStack[] items = new ItemStack[size];

        for (int i = 0; i < size; i++) {
            ItemData data = this.items.chooseOne(lootMultiplier);
            if (data != null) {
                items[i] = data.createItem();
            }
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

                double chance = itemNode.get("chance").asDouble() / 100;
                ItemData itemData = Json.convert(itemNode.get("item"), ItemData.class);

                table.addItem(id, itemData, chance);
            });

            table.items.sort();

            return table;
        }

    }

}
