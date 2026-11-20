package com.skyblockin.voidsurvival.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.AbstractIterator;
import com.skyblockin.voidsurvival.constants.Units;
import com.skyblockin.voidsurvival.math.BlockPosition;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import net.minecraft.core.BlockPos;
import org.bukkit.Keyed;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;

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

    public static Iterable<BlockPosition> between(Vector min, Vector max) {
        return between(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }

    public static Iterable<BlockPosition> between(BlockPosition min, BlockPosition max) {
        return between(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    public static Iterable<BlockPosition> between(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

        int xSize = maxX - minX + 1;
        int ySize = maxY - minY + 1;
        int zSize = maxZ - minZ + 1;
        int volume = xSize * ySize * zSize;

        return () -> new Iterator<>() {

            int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < volume;
            }

            @Override
            public BlockPosition next() {
                int x = this.index % xSize;
                int i = this.index / xSize;
                int y = i % ySize;
                int z = i / ySize;
                this.index++;
                return new BlockPosition(minX + x, minY + y, minZ + z);
            }

        };
    }

    public static <T> T randomChoice(List<T> elements) {
        return elements.get(ThreadLocalRandom.current().nextInt(elements.size()));
    }

    public static <K, V> Map.Entry<K, V> randomChoice(Map<K, V> elements) {

        int index = ThreadLocalRandom.current().nextInt(elements.size());

        for (Map.Entry<K, V> entry : elements.entrySet()) {
            if (index-- == 0) {
                return entry;
            }
        }

        return null;
    }

    public static <T> List<T> randomChoice(List<T> elements, int n) {

        ArrayList<T> results = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            results.add(randomChoice(elements));
        }

        return results;
    }

    public static <T> List<T> randomChoiceUnique(List<T> elements, int n) {

        ArrayList<T> copy = new ArrayList<>(elements);
        ArrayList<T> results = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            results.add(copy.remove(ThreadLocalRandom.current().nextInt(elements.size())));
        }

        return results;
    }

    public static boolean randomChance(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    public static String nameToTableId(String name) {
        return name.replace(" ", "_").toLowerCase();
    }

    public static String tableIdToName(String tableId) {

        String[] parts = tableId.split("_");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1);
        }

        return String.join(" ", parts);
    }

    public static long getTimeFromYaml(ConfigurationSection section, String key, int defaultValue, String unit) {

        String s = section.getString(key);

        if (s == null) {
            return defaultValue;
        }

        try {
            return Units.TIME.parse(s, unit);
        } catch (IllegalArgumentException iae) {
            return section.getInt(key, defaultValue);
        }

    }

    public static <T> Iterable<T> iterate(Iterator<T> iterator) {
        return () -> iterator;
    }
}
