package com.skyblockin.voidsurvival.storage;

import com.skyblockin.voidsurvival.math.Position;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class PositionPersistentDataType implements PersistentDataType<byte[], Position> {

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<Position> getComplexType() {
        return Position.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull Position complex, @NotNull PersistentDataAdapterContext context) {

        ByteBuffer buffer = ByteBuffer.allocate(3 * 8 + 2 * 4);

        buffer.putDouble(complex.x());
        buffer.putDouble(complex.y());
        buffer.putDouble(complex.z());
        buffer.putFloat(complex.yaw());
        buffer.putFloat(complex.pitch());

        return buffer.array();
    }

    @Override
    public @NotNull Position fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer buffer = ByteBuffer.wrap(primitive);
        return new Position(buffer.getDouble(), buffer.getDouble(), buffer.getDouble(), buffer.getFloat(), buffer.getFloat());
    }

}
