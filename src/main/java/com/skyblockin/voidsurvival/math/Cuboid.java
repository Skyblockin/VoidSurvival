package com.skyblockin.voidsurvival.math;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Location;

import java.io.IOException;
import java.io.Serial;

@JsonDeserialize(using = Cuboid.Deserializer.class)
@JsonSerialize(using = Cuboid.Serializer.class)
public class Cuboid {

    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;

    public Cuboid(Location start, Location end) {
        this(start.getBlockX(), start.getBlockY(), start.getBlockZ(), end.getBlockX(), end.getBlockY(), end.getBlockZ());
    }

    public Cuboid(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Preconditions.checkArgument(minX <= maxX, "minX must be less than or equal to maxX");
        Preconditions.checkArgument(minY <= maxY, "minY must be less than or equal to maxY");
        Preconditions.checkArgument(minZ <= maxZ, "minZ must be less than or equal to maxZ");
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public Cuboid(BlockPosition min, BlockPosition max) {
        this(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    @JsonIgnore
    public BlockPosition getMin() {
        return new BlockPosition(minX, minY, minZ);
    }

    @JsonIgnore
    public BlockPosition getMax() {
        return new BlockPosition(maxX, maxY, maxZ);
    }

    public boolean isWithinBounds(BlockPosition position) {
        return isWithinBounds(position.x(), position.y(), position.z());
    }

    public boolean isWithinBounds(int x, int y, int z) {
        return x <= this.maxX && x >= this.minX && y <= this.maxY && y >= this.minY && z <= this.maxZ && z >= this.minZ;
    }

    public Cuboid add(int x, int y, int z) {
        return new Cuboid(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public static class Serializer extends StdSerializer<Cuboid> {

        protected Serializer() {
            super(Cuboid.class);
        }

        @Override
        public void serialize(Cuboid cuboid, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {

            gen.writeStartArray();
            gen.writeNumber(cuboid.minX);
            gen.writeNumber(cuboid.minY);
            gen.writeNumber(cuboid.minZ);
            gen.writeNumber(cuboid.maxX);
            gen.writeNumber(cuboid.maxY);
            gen.writeNumber(cuboid.maxZ);
            gen.writeEndArray();

        }
    }

    public static class Deserializer extends StdDeserializer<Cuboid> {

        protected Deserializer() {
            super(Cuboid.class);
        }

        @Override
        public Cuboid deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            if (node.isArray()) {

                return new Cuboid(
                    node.get(0).asInt(), node.get(1).asInt(), node.get(2).asInt(),
                    node.get(3).asInt(), node.get(4).asInt(), node.get(5).asInt()
                );

            } else if (node.isObject()) {

                JsonNode min = node.get("min");
                JsonNode max = node.get("max");

                return new Cuboid(
                    min.get(0).asInt(), min.get(1).asInt(), min.get(2).asInt(),
                    max.get(0).asInt(), max.get(1).asInt(), max.get(2).asInt()
                );
            }

            return null;
        }
    }

}
