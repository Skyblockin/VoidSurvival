package com.skyblockin.voidsurvival.world.town;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.HashMap;

public class TownManager implements Listener {

    private final HashMap<String, Town> townMap = new HashMap<>();

    public Town getTown(String id) {
        return townMap.get(id);
    }

    public void reload(VoidSurvival plugin) {

        try {

            File file = plugin.getFile("towns.json");

            if (!file.exists()) {
                return;
            }

            JsonNode node = Json.readFromFile(file);

            townMap.clear();

            for (JsonNode townNode : node) {
                Town town = Json.convert(townNode, Town.class);
                townMap.put(town.id, town);
            }

            VoidSurvival.logInfo("Loaded %d towns", townMap.size());

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to load towns", ex);
        }

    }

}
