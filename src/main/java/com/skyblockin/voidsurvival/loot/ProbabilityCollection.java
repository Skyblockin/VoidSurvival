package com.skyblockin.voidsurvival.loot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ProbabilityCollection<T> {

    private record Entry<T>(T item, double chance) {}

    private final ArrayList<Entry<T>> items = new ArrayList<>();

    public void add(T item, double chance) {
        this.items.add(new Entry<>(item, chance));
    }

    public void sort() {
        items.sort(Comparator.comparingDouble(entry -> entry.chance));
    }

    public T chooseOne(double lootMultiplier) {

        for (Entry<T> entry : items) {
            if (ThreadLocalRandom.current().nextDouble() < entry.chance * lootMultiplier) {
                return entry.item;
            }
        }

        return null;
    }

    public List<T> choose(double lootMultiplier) {

        List<T> chosen = new ArrayList<>();

        for (Entry<T> entry : items) {
            if (ThreadLocalRandom.current().nextDouble() < entry.chance * lootMultiplier) {
                chosen.add(entry.item);
            }
        }

        return chosen;
    }

}
