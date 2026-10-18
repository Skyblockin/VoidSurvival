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
public class Gate implements Triggerable, Toggleable {

    private final String id;
    private final World world;
    private final BlockPosition pos;

    private final String openSchematic;
    private final String closedSchematic;
    private final AffineTransform openTransform;
    private final AffineTransform closedTransform;

    private boolean isClosed = false;

    public Gate(String id, World world, BlockPosition pos, String openSchematic, String closedSchematic, AffineTransform openTransform, AffineTransform closedTransform) {
        this.id = id;
        this.world = world;
        this.pos = pos;
        this.openSchematic = openSchematic;
        this.openTransform = openTransform;
        this.closedSchematic = closedSchematic;
        this.closedTransform = closedTransform;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void trigger(TriggerManager manager, Player player, PlayerData data) {

        if (openSchematic != null && closedSchematic != null) {
            if (isClosed) {
                WorldEditUtil.pasteSchematic(closedSchematic, world, pos, closedTransform);
            } else {
                WorldEditUtil.pasteSchematic(openSchematic, world, pos, openTransform);
            }
        }

    }

    @Override
    public void toggle() {
        isClosed = !isClosed;
    }

    @Override
    public void setState(boolean toggled) {
        isClosed = toggled;
    }

    @Override
    public boolean isToggled() {
        return isClosed;
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
        }
    }

}
