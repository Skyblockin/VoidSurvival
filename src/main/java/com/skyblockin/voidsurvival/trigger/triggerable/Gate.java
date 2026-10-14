package com.skyblockin.voidsurvival.trigger.triggerable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.math.Cuboid;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.TriggerManager;
import com.skyblockin.voidsurvival.world.WorldEditUtil;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.io.IOException;

@JsonDeserialize(using = Gate.Deserializer.class)
public class Gate implements Triggerable {

    private final String id;
    private final World world;
    private final BlockPosition openStart;
    private final BlockPosition openEnd;
    private final BlockPosition closedStart;
    private final BlockPosition closedEnd;
    private final BlockState blockState;

    private final String openSchematic;
    private final String closedSchematic;
    private final AffineTransform openTransform;
    private final AffineTransform closedTransform;

    private transient boolean isClosed = false;

    public Gate(String id, BlockState state, World world, BlockPosition openStart, BlockPosition openEnd, BlockPosition closedStart, BlockPosition closedEnd) {
        this.id = id;
        this.blockState = state;
        this.world = world;
        this.openStart = openStart;
        this.openEnd = openEnd;
        this.closedStart = closedStart;
        this.closedEnd = closedEnd;

        this.openSchematic = null;
        this.openTransform = null;
        this.closedSchematic = null;
        this.closedTransform = null;
    }

    public Gate(String id, World world, BlockPosition pos, String openSchematic, String closedSchematic, AffineTransform openTransform, AffineTransform closedTransform) {
        this.id = id;
        this.world = world;
        this.openStart = pos;
        this.openSchematic = openSchematic;
        this.openTransform = openTransform;
        this.closedSchematic = closedSchematic;
        this.closedTransform = closedTransform;
        this.openEnd = null;
        this.closedStart = null;
        this.closedEnd = null;
        this.blockState = null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void trigger(TriggerManager manager, Player player, PlayerData data) {

        isClosed = !isClosed;

        if (openSchematic != null && closedSchematic != null) {
            if (isClosed) {
                WorldEditUtil.pasteSchematic(closedSchematic, world, openStart, closedTransform);
            } else {
                WorldEditUtil.pasteSchematic(openSchematic, world, openStart, openTransform);
            }
        } else {

            BlockData air = BlockType.AIR.createBlockData();
            BlockData blockData = blockState.getBlockData();

            new Cuboid(openStart, openEnd).forEach(pos -> world.setBlockData(pos.x(), pos.y(), pos.z(), air));
            new Cuboid(closedStart, closedEnd).forEach(pos -> world.setBlockData(pos.x(), pos.y(), pos.z(), air));

            if (isClosed) {
                new Cuboid(closedStart, closedEnd).forEach(pos -> world.setBlockData(pos.x(), pos.y(), pos.z(), blockData));
            } else {
                new Cuboid(openStart, openEnd).forEach(pos -> world.setBlockData(pos.x(), pos.y(), pos.z(), blockData));
            }
        }

    }

    public static class Deserializer extends StdDeserializer<Gate> {

        protected Deserializer() {
            super(Gate.class);
        }

        @Override
        public Gate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            String id = node.get("id").asText();
            World world = Json.convert(node.get("world"), World.class);

            String openSchematic = node.path("open_schematic").asText(null);
            String closedSchematic = node.path("closed_schematic").asText(null);

            if (openSchematic != null) {

                BlockPosition pos = Json.convert(node.get("pos"), BlockPosition.class);

                double openRotateX = node.path("open_rot_x").asDouble(0);
                double openRotateY = node.path("open_rot_y").asDouble(0);
                double openRotateZ = node.path("open_rot_z").asDouble(0);
                double closedRotateX = node.path("closed_rot_x").asDouble(0);
                double closedRotateY = node.path("closed_rot_y").asDouble(0);
                double closedRotateZ = node.path("closed_rot_z").asDouble(0);

                AffineTransform openTransform = new AffineTransform()
                    .rotateX(openRotateX)
                    .rotateY(openRotateY)
                    .rotateZ(openRotateZ);

                AffineTransform closedTransform = new AffineTransform()
                    .rotateX(closedRotateX)
                    .rotateY(closedRotateY)
                    .rotateZ(closedRotateZ);

                return new Gate(id, world, pos, openSchematic, closedSchematic, openTransform, closedTransform);

            } else {

                BlockPosition openStart = Json.convert(node.get("open_start"), BlockPosition.class);
                BlockPosition openEnd = Json.convert(node.get("open_end"), BlockPosition.class);
                BlockPosition closedStart = Json.convert(node.get("closed_start"), BlockPosition.class);
                BlockPosition closedEnd = Json.convert(node.get("closed_end"), BlockPosition.class);
                BlockState blockState = Json.convert(node.get("block_state"), BlockState.class);

                return new Gate(id, blockState, world, openStart, openEnd, closedStart, closedEnd);

            }
        }
    }

}
