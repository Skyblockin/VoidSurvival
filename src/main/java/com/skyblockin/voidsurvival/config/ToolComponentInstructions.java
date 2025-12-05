package com.skyblockin.voidsurvival.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.VoidSurvival;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.meta.components.ToolComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonDeserialize(using = ToolComponentInstructions.Deserializer.class)
public class ToolComponentInstructions implements ComponentInstructions<ToolComponent> {

    public ToolRuleInstructions rules = null;
    public Float defaultMiningSpeed = null;
    public Integer damagePerBlock = null;

    @Override
    public ToolComponent apply(ToolComponent toolComponent) {

        if (damagePerBlock != null) {
            toolComponent.setDamagePerBlock(damagePerBlock);
        }

        if (defaultMiningSpeed != null) {
            toolComponent.setDefaultMiningSpeed(defaultMiningSpeed);
        }

        if (rules != null) {
            rules.apply(toolComponent);
        }

        return toolComponent;
    }

    public static class ToolRuleInstructions {

        public List<Triple<Tag<Material>, Float, Boolean>> tagRules = null;
        public List<Triple<Material, Float, Boolean>> materialRules = null;
        public List<Triple<Collection<Material>, Float, Boolean>> collectionRules = null;

        public void addMaterialRule(Material material, Float speed, Boolean correctToolForDrops) {

            if (this.materialRules == null) {
                this.materialRules = new ArrayList<>();
            }

            this.materialRules.add(new Triple<>(material, speed, correctToolForDrops));
        }

        public void addCollectionRule(Collection<Material> tag, Float speed, Boolean correctToolForDrops) {

            if (this.collectionRules == null) {
                this.collectionRules = new ArrayList<>();
            }

            this.collectionRules.add(new Triple<>(tag, speed, correctToolForDrops));
        }

        public void apply(ToolComponent toolComponent) {

            if (materialRules != null) {
                materialRules.forEach(rule -> toolComponent.addRule(rule.first(), rule.second(), rule.third()));
            }

            if (collectionRules != null) {
                collectionRules.forEach(rule -> toolComponent.addRule(rule.first(), rule.second(), rule.third()));
            }

        }

    }

    public static class Deserializer extends StdDeserializer<ToolComponentInstructions> {

        protected Deserializer() {
            super(ToolComponentInstructions.class);
        }

        @Override
        public ToolComponentInstructions deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            ToolComponentInstructions instructions = new ToolComponentInstructions();

            JsonNode rules = node.get("rules");

            if (rules != null) {

                ToolRuleInstructions toolRuleInstructions = new ToolRuleInstructions();

                for (JsonNode rule : rules) {

                    JsonNode type = rule.get("type");

                    Float defaultMiningSpeed = Json.convert(node.get("default_mining_speed"), Float.class);
                    Boolean correctToolForDrops = Json.convert(node.get("correct_tool"), Boolean.class);

                    if (type.isArray()) {

                        List<Material> materials = Json.convert(type, new TypeReference<>(){});

                        toolRuleInstructions.addCollectionRule(materials, defaultMiningSpeed, correctToolForDrops);

                    } else {

                        String value = type.asText();

                        Material material = Json.convert(value, Material.class);

                        if (material == null) {
                            VoidSurvival.logError("Invalid material %s provided for ToolComponent", value);
                            continue;
                        }

                        toolRuleInstructions.addMaterialRule(material, defaultMiningSpeed, correctToolForDrops);
                    }
                }

                instructions.rules = toolRuleInstructions;
            }

            if (node.has("damage_per_block")) {
                instructions.damagePerBlock = node.path("damage_per_block").asInt(1);
            }

            if (node.has("default_mining_speed")) {
                instructions.defaultMiningSpeed = (float) node.get("default_mining_speed").asDouble();
            }

            return instructions;
        }
    }

}
