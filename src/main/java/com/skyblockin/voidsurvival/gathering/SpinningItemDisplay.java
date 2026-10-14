package com.skyblockin.voidsurvival.gathering;

import com.skyblockin.voidsurvival.VoidSurvival;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class SpinningItemDisplay {

    private BukkitTask task;
    private UUID uuid;

    public SpinningItemDisplay(UUID displayId, Location where, ItemStack itemStack) {
        this.uuid = displayId;
        createItem(where, itemStack);
        startSpinningModelTask();
    }

    public void hide(Player player) {
        if (player == null) return;
        Entity entity = Bukkit.getEntity(this.uuid);
        if (entity != null) {
            player.hideEntity(VoidSurvival.getInstance(), entity);
        }
    }

    public void show(Player player) {
        if (player == null) return;
        Entity entity = Bukkit.getEntity(this.uuid);
        if (entity != null) {
            player.showEntity(VoidSurvival.getInstance(), entity);
        }
    }

    public void destroy() {
        Entity entity = Bukkit.getEntity(this.uuid);
        if (entity != null) {
            entity.remove();
        }
        this.task.cancel();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    private void createItem(Location where, ItemStack itemStack) {
        if (this.uuid != null) {
            Entity entity = Bukkit.getEntity(this.uuid);
            if (entity != null) {
                entity.remove();
            }
        }
        this.uuid = where.getWorld().spawn(where.clone().add(0, 2, 0), ItemDisplay.class, item -> {
            item.setItemStack(itemStack);
            item.setVisibleByDefault(true);
            item.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
            item.setInterpolationDuration(2);
            item.setPersistent(false);
            item.customName(itemStack.displayName());
        }).getUniqueId();
    }

    private void startSpinningModelTask() {

        this.task = new BukkitRunnable() {
            private float angleDegrees = 0;

            @Override
            public void run() {

                ItemDisplay display = (ItemDisplay) Bukkit.getEntity(uuid);

                if (display == null) {
                    return;
                }

                if (!display.isValid()) {
                    cancel();
                    return;
                }

                angleDegrees = (angleDegrees + 5) % 360;
                float radians = (float) Math.toRadians(angleDegrees);

                display.setInterpolationDelay(0);
                display.setInterpolationDuration(2);

                display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(radians, 0, 1, 0),
                    new Vector3f(1, 1, 1),
                    new AxisAngle4f()
                ));
            }
        }.runTaskTimer(VoidSurvival.getInstance(), 0L, 2L);

    }
}
