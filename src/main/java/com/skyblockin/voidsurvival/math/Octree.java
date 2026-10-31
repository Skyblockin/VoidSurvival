package com.skyblockin.voidsurvival.math;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A 3D Octree built natively using Bukkit's Location and BoundingBox classes.
 *
 * @param <T> the type of data payload stored in the octree
 */
public class Octree<T> {

    /**
     * Internal entry storing a Bukkit Location and its payload data.
     */
    public record Entry<T>(Location location, T data) {}

    private static class Node<T> {
        BoundingBox bounds;
        List<Entry<T>> points = new ArrayList<>();
        Node<T>[] children = null;
        boolean isDivided = false;

        Node(BoundingBox bounds) {
            this.bounds = bounds;
        }
    }

    private final Node<T> root;
    private final int capacity;
    private final int maxDepth;

    /**
     * Constructs a Bukkit Octree.
     *
     * @param bounds   the root Bukkit BoundingBox covering the space
     * @param capacity max items per node before subdivision
     * @param maxDepth max tree depth
     */
    public Octree(BoundingBox bounds, int capacity, int maxDepth) {
        this.root = new Node<>(bounds);
        this.capacity = capacity;
        this.maxDepth = maxDepth;
    }

    /**
     * Inserts a location and data payload into the octree.
     *
     * @param location the Bukkit Location
     * @param data     the payload
     * @return true if successfully inserted, false if out of root bounds
     */
    public boolean insert(Location location, T data) {
        if (!root.bounds.contains(location.getX(), location.getY(), location.getZ())) {
            return false;
        }
        return insertInternal(root, new Entry<>(location, data), 0);
    }

    private boolean insertInternal(Node<T> node, Entry<T> entry, int depth) {
        Location loc = entry.location();
        if (!node.bounds.contains(loc.getX(), loc.getY(), loc.getZ())) {
            return false;
        }

        if (!node.isDivided) {
            if (node.points.size() < capacity || depth >= maxDepth) {
                node.points.add(entry);
                return true;
            }
            subdivide(node);
        }

        for (Node<T> child : node.children) {
            if (insertInternal(child, entry, depth + 1)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void subdivide(Node<T> node) {
        double minX = node.bounds.getMinX();
        double minY = node.bounds.getMinY();
        double minZ = node.bounds.getMinZ();

        double midX = node.bounds.getCenterX();
        double midY = node.bounds.getCenterY();
        double midZ = node.bounds.getCenterZ();

        double maxX = node.bounds.getMaxX();
        double maxY = node.bounds.getMaxY();
        double maxZ = node.bounds.getMaxZ();

        node.children = new Node[8];
        // Split into 8 sub-boxes using Bukkit's BoundingBox(x1, y1, z1, x2, y2, z2) constructor
        node.children[0] = new Node<>(new BoundingBox(minX, midY, minZ, midX, maxY, midZ));
        node.children[1] = new Node<>(new BoundingBox(midX, midY, minZ, maxX, maxY, midZ));
        node.children[2] = new Node<>(new BoundingBox(minX, minY, minZ, midX, midY, midZ));
        node.children[3] = new Node<>(new BoundingBox(midX, minY, minZ, maxX, midY, midZ));
        node.children[4] = new Node<>(new BoundingBox(minX, midY, midZ, midX, maxY, maxZ));
        node.children[5] = new Node<>(new BoundingBox(midX, midY, midZ, maxX, maxY, maxZ));
        node.children[6] = new Node<>(new BoundingBox(minX, minY, midZ, midX, midY, maxZ));
        node.children[7] = new Node<>(new BoundingBox(midX, minY, midZ, maxX, midY, maxZ));

        node.isDivided = true;

        for (Entry<T> entry : node.points) {
            Location l = entry.location();
            for (Node<T> child : node.children) {
                if (child.bounds.contains(l.getX(), l.getY(), l.getZ())) {
                    child.points.add(entry);
                    break;
                }
            }
        }
        node.points.clear();
    }

    /**
     * Queries all payloads whose locations fall inside the given Bukkit BoundingBox.
     *
     * @param range the Bukkit BoundingBox search area
     * @return a list of matching payloads
     */
    public List<T> queryRange(BoundingBox range) {
        List<T> found = new ArrayList<>();
        queryRangeInternal(root, range, found);
        return found;
    }

    public boolean rangeContains(BoundingBox range, T data) {
        return queryRange(range).contains(data);
    }

    private void queryRangeInternal(Node<T> node, BoundingBox range, List<T> found) {
        if (!node.bounds.overlaps(range)) {
            return;
        }

        if (!node.isDivided) {
            for (Entry<T> entry : node.points) {
                Location l = entry.location();
                if (range.contains(l.getX(), l.getY(), l.getZ())) {
                    found.add(entry.data());
                }
            }
            return;
        }

        for (Node<T> child : node.children) {
            queryRangeInternal(child, range, found);
        }
    }
}
