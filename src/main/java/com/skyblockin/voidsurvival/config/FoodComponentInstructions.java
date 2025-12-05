package com.skyblockin.voidsurvival.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.inventory.meta.components.FoodComponent;

import java.io.IOException;

@JsonDeserialize(using = FoodComponentInstructions.Deserializer.class)
public class FoodComponentInstructions implements ComponentInstructions<FoodComponent> {

    public Float saturation = null;
    public Integer nutrition = null;
    public Boolean canAlwaysEat = null;

    @Override
    public FoodComponent apply(FoodComponent value) {

        if (saturation != null) {
            value.setSaturation(saturation);
        }

        if (nutrition != null) {
            value.setNutrition(nutrition);
        }

        if (canAlwaysEat != null) {
            value.setCanAlwaysEat(canAlwaysEat);
        }

        return value;
    }

    public static class Deserializer extends StdDeserializer<FoodComponentInstructions> {

        protected Deserializer() {
            super(FoodComponentInstructions.class);
        }

        @Override
        public FoodComponentInstructions deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            FoodComponentInstructions instructions = new FoodComponentInstructions();

            instructions.saturation = Json.convert(node.get("saturation"), Float.class);
            instructions.nutrition = Json.convert(node.get("nutrition"), Integer.class);
            instructions.canAlwaysEat = Json.convert(node.get("can_always_eat"), Boolean.class);

            return instructions;
        }
    }

}
