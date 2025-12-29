package com.skyblockin.voidsurvival.region;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = MobCapMap.Serializer.class)
@JsonDeserialize(using = MobCapMap.Deserializer.class)
public class MobCapMap extends HashMap<EntityType, Integer> {

    public static class Serializer extends StdSerializer<MobCapMap> {

        protected Serializer() {
            super(MobCapMap.class);
        }

        @Override
        public void serialize(MobCapMap value, JsonGenerator gen, SerializerProvider provider) throws IOException {

            gen.writeStartObject();

            for (Map.Entry<EntityType, Integer> entry : value.entrySet()) {
                gen.writeNumberField(entry.getKey().getKey().toString(), entry.getValue());
            }

            gen.writeEndObject();
        }
    }

    public static class Deserializer extends StdDeserializer<MobCapMap> {

        protected Deserializer() {
            super(MobCapMap.class);
        }

        @Override
        public MobCapMap deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            MobCapMap map = new MobCapMap();

            node.fields().forEachRemaining(entry -> {
                EntityType type = Registry.ENTITY_TYPE.get(Key.key(entry.getKey()));
                map.put(type, entry.getValue().asInt());
            });

            return map;
        }
    }

}
