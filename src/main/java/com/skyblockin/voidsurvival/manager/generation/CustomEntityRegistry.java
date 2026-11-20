package com.skyblockin.voidsurvival.manager.generation;

import com.skyblockin.voidsurvival.entity.CustomEntity;
import com.skyblockin.voidsurvival.entity.Fighter;
import com.skyblockin.voidsurvival.entity.boss.RoyalRevenant;

import java.util.HashMap;

public class CustomEntityRegistry {

    private static final HashMap<String, CustomEntity<?>> ENTITIES = new HashMap<>();

    public static final RoyalRevenant ROYAL_REVENANT = register("royal_revenant", new RoyalRevenant());
    public static final Fighter FIGHTER = register("fighter", new Fighter());

    @SuppressWarnings("unchecked")
    public static <T> CustomEntity<T> get(String id) {
        return (CustomEntity<T>) ENTITIES.get(id);
    }

    public static <T extends CustomEntity<?>> T register(String id, T entity) {
        ENTITIES.put(id, entity);
        return entity;
    }

}
