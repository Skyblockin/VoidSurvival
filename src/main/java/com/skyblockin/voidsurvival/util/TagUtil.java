package com.skyblockin.voidsurvival.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class TagUtil {

    public static <T extends Keyed> boolean isTagged(TagKey<@NotNull T> key, Keyed keyed) {
        return RegistryAccess.registryAccess()
            .getRegistry(key.registryKey())
            .getTag(key)
            .contains(TypedKey.create(key.registryKey(), keyed.getKey()));
    }

}
