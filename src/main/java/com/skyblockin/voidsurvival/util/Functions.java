package com.skyblockin.voidsurvival.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Keyed;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Functions {

    public static SuggestionProvider<CommandSourceStack> suggest(Supplier<Collection<String>> suggestions) {
        return (ctx, builder) -> {
            suggestions.get().forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

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
    public static <T extends Keyed> RegistryKeySet<@NotNull T> parseRegistryKeySet(RegistryKey<@NotNull T> registryKey, JsonNode node) {

        if (node.isArray()) {

            List<TypedKey<@NotNull T>> keys = new ArrayList<>(node.size());

            for (JsonNode value : node) {
                keys.add(TypedKey.create(registryKey, value.asText()));
            }

            return RegistrySet.keySet(registryKey, keys);

        } else {

            String value = node.asText();

            if (value.startsWith("#")) {
                value = value.substring(1);
                return RegistryAccess.registryAccess().getRegistry(registryKey).getTag(TagKey.create(registryKey, value));
            } else {
                return RegistrySet.keySet(registryKey, TypedKey.create(registryKey, value));
            }

        }

    }

    public static String tableIdToName(String tableId) {

        String[] parts = tableId.split("_");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1);
        }

        return String.join(" ", parts);
    }

    private record TimePair(CustomTimeUnit unit, char suffix) {}
    private static final TimePair[] TIME_PAIRS = {
        new TimePair(CustomTimeUnit.DAYS, 'd'),
        new TimePair(CustomTimeUnit.HOURS, 'h'),
        new TimePair(CustomTimeUnit.MINUTES, 'm'),
        new TimePair(CustomTimeUnit.SECONDS, 's'),
        new TimePair(CustomTimeUnit.TICKS, 't')
    };

    public static long parseMillis(String s) {

        long millis = 0;

        // Standardize the string because yeah lol
        s = s.replaceAll(" ", "").toLowerCase();

        boolean sawAtLeastOneUnit = false;

        for (TimePair pair : TIME_PAIRS) {

            int index = s.indexOf(pair.suffix);
            if (index > 0) {
                sawAtLeastOneUnit = true;
                millis += pair.unit.convert(Long.parseLong(s.substring(0, index)), TimeUnit.MILLISECONDS);
            }

            s = s.substring(index + 1);
        }

        if (!sawAtLeastOneUnit) {
            try {
                return Long.parseLong(s);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid time format! Could not find any valid suffix in time string '" + s + "', valid suffixes are: 'd', 'h', 'm', 's'", ex);
            }
        }

        return millis;
    }

    public static long parseTicks(String s) {
        return parseMillis(s) / 50;
    }

    public static long getTimeFromYaml(ConfigurationSection section, String key, int defaultValue, CustomTimeUnit unit) {

        String s = section.getString(key);

        if (s == null) {
            return defaultValue;
        }

        try {
            return CustomTimeUnit.MILLISECONDS.convert(parseMillis(s), unit);
        } catch (IllegalArgumentException iae) {
            return section.getInt(key, defaultValue);
        }

    }

}
