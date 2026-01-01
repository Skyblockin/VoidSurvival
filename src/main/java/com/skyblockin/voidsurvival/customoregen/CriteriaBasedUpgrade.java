package com.skyblockin.voidsurvival.customoregen;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.loot.Weighted;
import com.skyblockin.voidsurvival.loot.WeightedCollection;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;

@JsonDeserialize(using = CriteriaBasedUpgrade.Deserializer.class)
public class CriteriaBasedUpgrade {

    private final String id;
    private final HashMap<Statistic, HashMap<Material, Integer>> requirements;
    private final WeightedCollection<BlockType> rewardList;

    public CriteriaBasedUpgrade(String id, HashMap<Statistic, HashMap<Material, Integer>> requirements, WeightedCollection<BlockType> rewardList) {
        this.id = id;
        this.requirements = requirements;
        this.rewardList = rewardList;
    }

    public boolean test(Player player) {

        for (var entry : requirements.entrySet()) {
            for (var entry2 : entry.getValue().entrySet()) {
                if (player.getStatistic(entry.getKey(), entry2.getKey()) < entry2.getValue()) {
                    return false;
                }
            }

        }

        return true;
    }

    public WeightedCollection<BlockType> getRewardList() {
        return rewardList;
    }

    public String getId() {
        return id;
    }

    public static class Deserializer extends StdDeserializer<CriteriaBasedUpgrade> {

        protected Deserializer() {
            super(CriteriaBasedUpgrade.class);
        }

        @Override
        public CriteriaBasedUpgrade deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            HashMap<Statistic, HashMap<Material, Integer>> requirementMap = new HashMap<>();
            WeightedCollection<BlockType> rewardList = new WeightedCollection<>();

            JsonNode node = p.getCodec().readTree(p);

            node.get("requirements").fields().forEachRemaining(entry -> {

                Statistic statistic = Statistic.valueOf(entry.getKey().toUpperCase());

                HashMap<Material, Integer> requirements = new HashMap<>();

                entry.getValue().fields().forEachRemaining(entry2 -> {

                    Material material = Registry.MATERIAL.get(Key.key(entry2.getKey()));
                    int value = entry2.getValue().intValue();

                    requirements.put(material, value);
                });

                requirementMap.put(statistic, requirements);
            });

            node.get("rewards").fields().forEachRemaining(entry -> {

                BlockType type = Registry.BLOCK.get(Key.key(entry.getKey()));
                double chance = entry.getValue().asDouble();

                if (type == null) {
                    VoidSurvival.logError("Encountered invalid type while deserializing CriteriaBasedUpgrade: " + entry.getKey());
                    return;
                }

                rewardList.add(new Weighted<>(entry.getKey(), type, chance));
            });

            String id = node.get("id").asText();

            return new CriteriaBasedUpgrade(id, requirementMap, rewardList);
        }
    }
}
