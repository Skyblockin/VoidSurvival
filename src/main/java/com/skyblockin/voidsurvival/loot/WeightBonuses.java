package com.skyblockin.voidsurvival.loot;

import java.util.HashMap;

public class WeightBonuses {

    private final HashMap<String, Double> bonusMap = new HashMap<>();
    private double totalWeight = 0.0;

    public void put(String id, double weight) {
        this.bonusMap.put(id, weight);
        this.totalWeight += weight;
    }

    public void put(Weighted<?> weighted, double weight) {
        put(weighted.getId(), weight);
    }

    public void remove(String id) {
        bonusMap.remove(id);
    }

    public double getTotalWeight() {
        return this.totalWeight;
    }

    public double get(Weighted<?> weighted) {
        return get(weighted.id);
    }

    public double get(String id) {
        return bonusMap.getOrDefault(id, 0.0);
    }

}
