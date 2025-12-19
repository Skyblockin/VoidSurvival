package com.skyblockin.voidsurvival.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class ItemManager {

    private final HashMap<String, ItemData> items = new HashMap<>();

    public void loadItemsFromFile() {

        File file = new File(VoidSurvival.getInstance().getDataFolder(), "items");

        if (!file.exists()) {
            if (file.mkdirs()) {
                VoidSurvival.logInfo("The loot tables folder was missing, so it was created.");
            } else {
                VoidSurvival.logError("The loot tables folder was missing, and it could not be created. Is the plugin folder read-only?");
            }
            return;
        }

        File[] itemFiles = file.listFiles();

        if (itemFiles == null) return;

        this.items.clear();

        for (File itemFile : itemFiles) {

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
