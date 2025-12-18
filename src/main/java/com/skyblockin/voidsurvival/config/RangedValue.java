package com.skyblockin.voidsurvival.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@JsonDeserialize(using = RangedValue.Deserialize.class)
public class RangedValue implements Gettable<Integer> {

    private final int min;
    private final int max;

    public RangedValue(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public RangedValue(int value) {
        this(value, value);
    }

    @Override
    public Integer get() {
        if (min == max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static class Deserialize extends JsonDeserializer<RangedValue> {

        @Override
        public RangedValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            if (node.isInt()) return new RangedValue(node.asInt());
            else if (node.isArray()) return new RangedValue(node.get(0).asInt(), node.get(1).asInt());

            return null;
        }
    }

}
