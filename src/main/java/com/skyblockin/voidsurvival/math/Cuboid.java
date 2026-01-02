package com.skyblockin.voidsurvival.math;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Preconditions;
import org.bukkit.Location;

import java.io.IOException;

@JsonDeserialize(using = Cuboid.Deserializer.class)
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

    public BlockPosition getMin() {
        return new BlockPosition(minX, minY, minZ);
    }

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

    public static class Deserializer extends StdDeserializer<Cuboid> {

        protected Deserializer() {
            super(Cuboid.class);
        }

        @Override
        public Cuboid deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {

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
