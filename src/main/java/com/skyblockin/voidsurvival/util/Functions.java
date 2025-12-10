package com.skyblockin.voidsurvival.util;

import com.fasterxml.jackson.databind.JsonNode;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;

public class Functions {

    public static <T> CompletableFuture<T> runAsync(Callable<T> callable) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static <T> T randomChoice(List<T> elements) {
        return elements.get(ThreadLocalRandom.current().nextInt(elements.size()));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T extends Keyed> RegistryKeySet<@NotNull T> parseRegistryKeySet(RegistryKey<@NotNull T> registryKey, JsonNode rule) {

        JsonNode type = rule.get("type");

        if (type.isArray()) {

            List<TypedKey<@NotNull T>> blockTypeKeys = new ArrayList<>(type.size());

            for (JsonNode value : type) {
                TypedKey<@NotNull T> blockTypeKey = TypedKey.create(registryKey, value.asText());
                blockTypeKeys.add(blockTypeKey);
            }

            return RegistrySet.keySet(registryKey, blockTypeKeys);

        } else {

            String value = type.asText();

            if (value.startsWith("#")) {
                value = value.substring(1);
                return RegistryAccess.registryAccess().getRegistry(registryKey).getTag(TagKey.create(registryKey, value));
            } else {
                TypedKey<@NotNull T> blockType = TypedKey.create(registryKey, value);
                return RegistrySet.keySet(registryKey, blockType);
            }

        }

    }

}
