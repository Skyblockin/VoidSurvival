package com.skyblockin.voidsurvival.config;

import com.skyblockin.voidsurvival.VoidSurvival;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class DataComponentTypeList {

    private static final Map<String, DataComponentType> TYPES;
    private static final List<String> TYPE_NAMES = new ArrayList<>();

    static {

        Map<String, DataComponentType> map = new HashMap<>();

        for (Field field : DataComponentTypes.class.getFields()) {

            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != DataComponentType.class) continue;

            try {

                DataComponentType tag = (DataComponentType) field.get(null);
                String key = field.getName().toLowerCase();
                map.put(key, tag);
                TYPE_NAMES.add(key);

            } catch (Exception e) {
                VoidSurvival.logError("Error while initializing static DataComponentTypeList class: %s", e.getMessage());
            }
        }

        VoidSurvival.logInfo("Successfully scraped the following material tags: %s", TYPE_NAMES);

        TYPES = Map.copyOf(map);
    }

    public static List<String> getTagNames() {
        return TYPE_NAMES;
    }

    public static DataComponentType getByKey(String key) {
        return TYPES.get(key.toLowerCase());
    }

}
