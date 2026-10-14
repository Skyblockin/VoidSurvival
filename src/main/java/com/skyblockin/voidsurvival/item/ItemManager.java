package com.skyblockin.voidsurvival.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.item.listeners.GrassSeedsListener;
import com.skyblockin.voidsurvival.item.listeners.LavaWalkerListener;
import com.skyblockin.voidsurvival.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class ItemManager {

    public static ItemData getItemById(String id) {
        return VoidSurvival.getInstance().getItemManager().getItem(id);
    }

    private final HashMap<String, ItemData> items = new HashMap<>();

    public ItemManager() {
        VoidSurvival.getInstance().registerEvents(
            new GrassSeedsListener(),
            new LavaWalkerListener()
        );
    }

    public void loadItemsFromFile() {

        this.items.clear();

        for (File itemFile : FileUtil.listFiles("items")) {
            try {
                loadItemsRecursively(itemFile);
            } catch (Exception ex) {
                VoidSurvival.logError("Failed to load items from file '" + itemFile.getName() + "'", ex);
            }
        }

        VoidSurvival.logInfo("Loaded %d custom items", items.size());
    }

    private void loadItemsRecursively(File file) throws IOException {

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File itemFile : files) {
                    loadItemsRecursively(itemFile);
                }
            }
            return;
        }

        JsonNode node = Json.readFromFile(file);
        node.properties().forEach(entry -> {
            ItemData data = Json.convert(entry.getValue(), ItemData.class);
            this.items.put(entry.getKey(), data);
        });
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
