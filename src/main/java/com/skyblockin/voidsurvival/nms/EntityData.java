package com.skyblockin.voidsurvival.nms;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.config.ItemData;
import com.skyblockin.voidsurvival.config.Json;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;

@JsonDeserialize(using = EntityData.Deserializer.class)
public class EntityData {

    public HashMap<EquipmentSlot, ItemData> equipment = new HashMap<>();
    public HashMap<String, Float> attributes = new HashMap<>();
    public boolean persistent;
    public String entityId = null;
    public String customName = null;

    public static class Deserializer extends StdDeserializer<EntityData> {

        protected Deserializer() {
            super(EntityData.class);
        }

        @Override
        public EntityData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            EntityData data = new EntityData();

            data.persistent = node.path("persistent").asBoolean(false);
            data.entityId = node.get("id").asText();
            data.customName = node.get("name").asText();
            data.attributes = Json.convert(node.get("attributes"), new TypeReference<>() {});
            data.equipment = Json.convert(node.get("equipment"), new TypeReference<>() {});

            return data;
        }
    }

    public Entity spawnEntity(Location location) {

        World world = location.getWorld();

        ServerLevel level = ((CraftWorld) world).getHandle();

        net.minecraft.world.entity.Entity mob = EntityType.loadEntityRecursive(getEntityTag(), level, EntitySpawnReason.COMMAND, entity -> {
            entity.snapTo(location.getX(), location.getY(), location.getZ());
            return entity;
        });

        if (mob != null && level.tryAddFreshEntityWithPassengers(mob)) {
            return mob.getBukkitEntity();
        } else {
            return null;
        }
    }

    public CompoundTag getEntityTag() {

        CompoundTag entity = new CompoundTag();

        entity.putString("id", entityId);
        entity.put("equipment", getEquipmentTag());
        entity.putString("CustomName", customName);

        ListTag attributes = new ListTag();

        this.attributes.forEach((key, value) -> {

            CompoundTag attribute = new CompoundTag();

            attribute.putString("id", key);
            attribute.putFloat("base", value);

            attributes.add(attribute);
        });

        entity.putBoolean("PersistenceRequired", persistent);
        entity.put("attributes", attributes);

        return entity;
    }

    public CompoundTag getEquipmentTag() {

        CompoundTag tag = new CompoundTag();

        equipment.forEach((slot, data) -> {

            ItemStack apiStack = data.createItem();
            net.minecraft.world.item.ItemStack nmsStack = ((CraftItemStack)apiStack).handle;
            Tag itemTag = net.minecraft.world.item.ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, nmsStack).getOrThrow();

            tag.put(apiEquipmentSlotToNms(slot), itemTag);
        });

        return tag;
    }

    private String apiEquipmentSlotToNms(EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> "mainhand";
            case OFF_HAND -> "offhand";
            case FEET -> "feet";
            case LEGS -> "legs";
            case CHEST -> "chest";
            case HEAD -> "head";
            case BODY -> "body";
            default -> slot.name().toLowerCase();
        };
    }

}
