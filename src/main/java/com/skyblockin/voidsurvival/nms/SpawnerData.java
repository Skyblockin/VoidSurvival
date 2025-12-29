package com.skyblockin.voidsurvival.nms;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.util.Functions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

@JsonDeserialize(using = SpawnerData.Deserializer.class)
public class SpawnerData {

    public short minSpawnDelay;
    public short maxSpawnDelay;
    public short spawnCount;
    public short maxNearbyEntities;
    public short requiredPlayerRange;
    public short spawnRange;
    public short spawnDelay;

    public EntityData entityData;

    public static class Deserializer extends StdDeserializer<SpawnerData> {

        protected Deserializer() {
            super(SpawnerData.class);
        }

        @Override
        public SpawnerData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);
            SpawnerData data = new SpawnerData();

            data.minSpawnDelay = parseTimeField("min_spawn_delay", node, 200);
            data.maxSpawnDelay = parseTimeField("max_spawn_delay", node, 800);
            data.spawnDelay = parseTimeField("spawn_delay", node, 0);
            data.spawnCount = (short) node.path("spawn_count").asInt(4);
            data.maxNearbyEntities = (short) node.path("max_nearby_entities").asInt(4);
            data.requiredPlayerRange = (short) node.path("required_player_range").asInt(16);
            data.spawnRange = (short) node.path("spawn_range").asInt(4);

            data.entityData = Json.convert(node.get("entity"), EntityData.class);

            return data;
        }

        private short parseTimeField(String key, JsonNode node, int defaultValue) {

            JsonNode timeNode = node.path(key);

            if (timeNode.isInt()) {
                return (short) timeNode.asInt(defaultValue);
            } else if (timeNode.isTextual()) {
                return (short) Functions.parseTicks(timeNode.asText());
            }

            return (short) defaultValue;
        }
    }

    public ItemStack applyNmsTag(ItemStack item) {

        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        CompoundTag tag = new CompoundTag();
        CompoundTag spawnData = new CompoundTag();

        tag.putString("id", "minecraft:mob_spawner");
        tag.putShort("MinSpawnDelay", minSpawnDelay);
        tag.putShort("MaxSpawnDelay", maxSpawnDelay);
        tag.putShort("SpawnCount", spawnCount);
        tag.putShort("SpawnRange", spawnRange);
        tag.putShort("MaxNearbyEntities", maxNearbyEntities);
        tag.putShort("RequiredPlayerRange", requiredPlayerRange);
        tag.putShort("SpawnDelay", spawnDelay);

        CompoundTag entityTag = entityData.getEntityTag();

        spawnData.put("entity", entityTag);

        tag.put("SpawnData", spawnData);
        //tag.put("SpawnPotentials", spawnPotentials);

        TypedEntityData<BlockEntityType<?>> data = TypedEntityData.of(BlockEntityType.MOB_SPAWNER, tag);

        nmsStack.set(DataComponents.BLOCK_ENTITY_DATA, data);

        return nmsStack.asBukkitMirror();
    }

    private CompoundTag getPotentialSpawnEntry(CompoundTag entity, int weight) {

        CompoundTag tag = new CompoundTag();
        CompoundTag dataTag = new CompoundTag();

        dataTag.put("entity", entity);

        tag.put("data", dataTag);
        tag.putInt("weight", weight);

        return tag;
    }


}
