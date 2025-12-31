package com.skyblockin.voidsurvival.storage;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = NamedLocationMap.Serializer.class)
@JsonDeserialize(using = NamedLocationMap.Deserializer.class)
public class NamedLocationMap extends HashMap<String, Location> {

    public static class Deserializer extends StdDeserializer<NamedLocationMap> {

        protected Deserializer() {
            super(NamedLocationMap.class);
        }

        @Override
        public NamedLocationMap deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {

            JsonNode node = p.getCodec().readTree(p);

            NamedLocationMap map = new NamedLocationMap();

            node.fields().forEachRemaining(entry -> {

                String homeName = entry.getKey();
                JsonNode homeData = entry.getValue();

                String worldName = homeData.get("world").asText();

                World world = Bukkit.getWorld(worldName);

                double x = homeData.get("x").asDouble();
                double y = homeData.get("y").asDouble();
                double z = homeData.get("z").asDouble();
                float pitch = (float) homeData.get("pitch").asDouble();
                float yaw = (float) homeData.get("yaw").asDouble();

                map.put(homeName, new Location(world, x, y, z, yaw, pitch));
            });

            return map;
        }
    }

    public static class Serializer extends StdSerializer<NamedLocationMap> {

        protected Serializer() {
            super(NamedLocationMap.class);
        }

        @Override
        public void serialize(NamedLocationMap value, JsonGenerator gen, SerializerProvider provider) throws IOException {

            gen.writeStartObject();

            for (Map.Entry<String, Location> entry : value.entrySet()) {

                gen.writeFieldName(entry.getKey());

                gen.writeStartObject();

                Location location = entry.getValue();

                gen.writeStringField("world", location.getWorld().getName());
                gen.writeNumberField("x", location.getX());
                gen.writeNumberField("y", location.getY());
                gen.writeNumberField("z", location.getZ());
                gen.writeNumberField("pitch", location.getPitch());
                gen.writeNumberField("yaw", location.getYaw());

                gen.writeEndObject();
            }

            gen.writeEndObject();
        }
    }

}
