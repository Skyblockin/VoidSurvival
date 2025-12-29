package com.skyblockin.voidsurvival.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.util.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ItemManager {

    private final HashMap<String, ItemData> items = new HashMap<>();

    public void loadItemsFromFile() {

        this.items.clear();

        for (File itemFile : FileUtil.listFiles("items")) {

            try {

                JsonNode node = Json.readFromFile(itemFile);

                node.fields().forEachRemaining(entry -> {
                    ItemData data = Json.convert(entry.getValue(), ItemData.class);
                    this.items.put(entry.getKey(), data);
                });

            } catch (Exception ex) {
                VoidSurvival.logError("Failed to load items from file '" + itemFile.getName() + "'", ex);
            }

        }

        VoidSurvival.logInfo("Loaded %d custom items", items.size());
    }

    public void reload() {
        loadItemsFromFile();
    }

    public Set<String> getIds() {
        return items.keySet();
    }

    public ItemData getItem(String id) {
        return items.get(id);
    }
}
