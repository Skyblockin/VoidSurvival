package com.skyblockin.voidsurvival.loot;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedCollection<T> {

    private ArrayList<Weighted<T>> items = new ArrayList<>();
    private double totalWeight = 0.0;

    public WeightedCollection<T> copy() {

        WeightedCollection<T> collection = new WeightedCollection<>();

        collection.items = new ArrayList<>(items);
        collection.totalWeight = totalWeight;

        return collection;
    }

    public void add(Weighted<T> weighted) {
        this.items.add(weighted);
        this.totalWeight += weighted.getWeight();
    }

    public T choose(double lootBonus) {

        if (lootBonus < 0) {
            lootBonus = 0;
        }

        if (isEmpty()) {
            return null;
        }

        double randomMultiplier = ThreadLocalRandom.current().nextDouble();
        double randomWeight = randomMultiplier * totalWeight;

        for (Weighted<T> item : items) {

            randomWeight += lootBonus * randomMultiplier;
            randomWeight -= item.getWeight() + lootBonus;

            if (randomWeight <= 0) {
                return item.getItem();
            }

        }

        return null;
    }

    public T choose(WeightBonuses bonuses) {

        if (isEmpty()) {
            return null;
        }

        double randomMultiplier = ThreadLocalRandom.current().nextDouble();
        double randomWeight = randomMultiplier * totalWeight;

        for (Weighted<T> item : items) {

            if (bonuses != null) {

                double bonus = bonuses.get(item);

                randomWeight += bonus * randomMultiplier;
                randomWeight -= item.getWeight() + bonus;

            } else {
                randomWeight -= item.getWeight();
            }

            if (randomWeight <= 0) {
                return item.getItem();
            }

        }

        return null;
    }

    public T choose() {
        return choose(null);
    }

    public ArrayList<? extends Weighted<T>> getItems() {
        return items;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        return String.format("WeightedCollection{weight=%f, items=%d}", totalWeight, items.size());
    }

}
