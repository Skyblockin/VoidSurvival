package com.skyblockin.voidsurvival.config;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.skyblockin.voidsurvival.VoidSurvival;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MaterialTag {

    private static final Map<String, MaterialSetTag> TAGS;
    private static final List<String> TAG_NAMES = new ArrayList<>();

    static {

        Map<String, MaterialSetTag> map = new HashMap<>();

        for (Field field : MaterialTags.class.getFields()) {

            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != MaterialSetTag.class) continue;

            try {

                MaterialSetTag tag = (MaterialSetTag) field.get(null);
                String key = tag.getKey().value().replaceAll("_settag", "");
                map.put(key, tag);
                TAG_NAMES.add(key);

            } catch (Exception e) {
                VoidSurvival.logInfo("Error while initializing static MaterialTag class: %s", e.getMessage());
            }
        }

        VoidSurvival.logInfo("Successfully scraped the following material tags: %s", TAG_NAMES);

        TAGS = Map.copyOf(map);
    }

    public static List<String> getTagNames() {
        return TAG_NAMES;
    }

    public static MaterialSetTag getByKey(String key) {
        return TAGS.get(key);
    }

}
