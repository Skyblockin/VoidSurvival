package com.skyblockin.voidsurvival.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

public class JsonSerializers {

    public static final LocationSerializer LOCATION = new LocationSerializer();
    public static final WorldSerializer WORLD = new WorldSerializer();

    public static class WorldSerializer extends StdSerializer<World> {

        protected WorldSerializer() {
            super(World.class);
        }

        @Override
        public void serialize(World value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.getName());
        }
    }

    public static class LocationSerializer extends StdSerializer<Location> {

        protected LocationSerializer() {
            super(Location.class);
        }

        @Override
        public void serialize(Location value, JsonGenerator gen, SerializerProvider provider) throws IOException {

            gen.writeStartObject();
            gen.writeStringField("world", value.getWorld().getName());
            gen.writeNumberField("x", value.getX());
            gen.writeNumberField("y", value.getY());
            gen.writeNumberField("z", value.getZ());
            gen.writeNumberField("yaw", value.getYaw());
            gen.writeNumberField("pitch", value.getPitch());
            gen.writeEndObject();

        }
    }

}
