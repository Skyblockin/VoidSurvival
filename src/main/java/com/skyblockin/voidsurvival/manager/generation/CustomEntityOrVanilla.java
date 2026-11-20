package com.skyblockin.voidsurvival.manager.generation;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.entity.CustomEntity;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.io.IOException;

@JsonDeserialize(using = CustomEntityOrVanilla.Deserializer.class)
public class CustomEntityOrVanilla {

    private EntityType vanilla;
    private String customEntityId;

    public boolean spawnAt(Location location) {

        if (customEntityId != null) {

            CustomEntity<?> entity = CustomEntityRegistry.get(customEntityId);

            if (entity.isValidForSpawn(location)) {
                entity.spawn(location);
                return true;
            }

            return false;

        } else {

            Class<? extends Entity> clazz = vanilla.getEntityClass();

            if (clazz == null) {
                VoidSurvival.logError("Attempted to spawn unknown entity with no known entity class");
                return false;
            }

            Entity entity = location.getWorld().createEntity(location, clazz);

            if (!entity.collidesAt(location)) {
                entity.spawnAt(location);
                return true;
            } else {
                entity.remove();
                return false;
            }
        }
    }

    public static class Deserializer extends StdDeserializer<CustomEntityOrVanilla> {

        protected Deserializer() {
            super(CustomEntityOrVanilla.class);
        }

        @Override
        public CustomEntityOrVanilla deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {

            CustomEntityOrVanilla object = new CustomEntityOrVanilla();

            String data = p.getValueAsString();

            if (data.startsWith("voidsurvival:")) {
                object.customEntityId = data.substring("voidsurvival:".length());
            } else {
                object.vanilla = Registry.ENTITY_TYPE.get(Key.key(data));
            }

            return object;
        }

    }

}
