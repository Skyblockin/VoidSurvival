package com.skyblockin.voidsurvival.loot;

public class Weighted<T> implements Comparable<Weighted<T>> {

    public String id;
    public T item;
    public double weight;

    public Weighted(String id, T item, double weight) {
        this.id = id;
        this.item = item;
        this.weight = weight;
    }

    public T getItem() {
        return item;
    }

    public String getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public int compareTo(Weighted other) {
        return Double.compare(getWeight(), other.getWeight());
    }

    public int hashCode() {
        return getItem().hashCode();
    }

}
