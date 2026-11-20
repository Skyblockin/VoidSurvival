package com.skyblockin.voidsurvival.storage.pdc;

import com.skyblockin.voidsurvival.manager.generation.Structure;
import com.skyblockin.voidsurvival.math.BlockPosition;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class StructurePersistentDataType implements PersistentDataType<byte[], Structure> {

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<Structure> getComplexType() {
        return Structure.class;
    }

    @Override
    public @NonNull byte[] toPrimitive(@NonNull Structure complex, @NotNull PersistentDataAdapterContext context) {

        byte[] nameBytes = complex.name().getBytes();

        // Six ints for the coordinates,
        // Four bytes for each of the ints,
        // One int for the name length to rule them all
        ByteBuffer buffer = ByteBuffer.allocate(4 + nameBytes.length + 4 * 6);

        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        buffer.putInt(complex.min().x());
        buffer.putInt(complex.min().y());
        buffer.putInt(complex.min().z());
        buffer.putInt(complex.max().x());
        buffer.putInt(complex.max().y());
        buffer.putInt(complex.max().z());

        return buffer.array();
    }

    @Override
    public @NonNull Structure fromPrimitive(@NonNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {

        ByteBuffer buffer = ByteBuffer.wrap(primitive);

        int length = buffer.getInt();

        byte[] nameBytes = new byte[length];

        buffer.get(nameBytes);

        String name = new String(nameBytes);
        BlockPosition min = new BlockPosition(buffer.getInt(), buffer.getInt(), buffer.getInt());
        BlockPosition max = new BlockPosition(buffer.getInt(), buffer.getInt(), buffer.getInt());

        return new Structure(name, min, max);
    }

}
