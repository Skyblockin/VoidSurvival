package com.skyblockin.voidsurvival.combat;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.item.ItemData;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.util.FileUtil;
import com.skyblockin.voidsurvival.util.TagUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class StanceManager {

    private final HashMap<UUID, Integer> remainingStanceHealths = new HashMap<>();
    private final HashMap<String, Integer> stanceHealths = new HashMap<>();
    private final HashMap<UUID, BukkitTask> staggeredMobs = new HashMap<>();

    /**
     * Get the stance damage the given entity would deal if it were to attack in its current state
     * @param entity the entity whose stance damage to get
     * @return the stance damage
     */
    public int getStanceDamage(LivingEntity entity) {

        EntityEquipment equipment = entity.getEquipment();

        double stanceDamage = 5;

        if (equipment != null) {

            ItemStack weapon = equipment.getItemInMainHand();
            ItemType type = weapon.getType().asItemType();

            if (TagUtil.isTagged(ItemTypeTagKeys.SWORDS, type)) {
                stanceDamage = 20;
            } else if (TagUtil.isTagged(ItemTypeTagKeys.AXES, type)) {
                stanceDamage = 50;
            }

            String id = Accessors.ITEM_ID.read(weapon);

            if (id != null) {

                ItemData data = VoidSurvival.getInstance().getItemManager().getItem(id);

                if (data != null) {
                    stanceDamage = data.stanceDamage;
                }
            }

        }

        if (entity instanceof Player player) {
            stanceDamage *= (player.getAttackCooldown() + 0.25);
        }

        return (int) Math.ceil(stanceDamage);
    }

    public int getStance(LivingEntity entity) {

        Integer remainingStance = remainingStanceHealths.get(entity.getUniqueId());

        if (remainingStance == null) {
            String stanceId = Accessors.STANCE_ID.read(entity);
            remainingStance = stanceHealths.get(stanceId);
        }

        if (remainingStance == null) {
            return -1;
        }

        return remainingStance;
    }

    public boolean isStanceBroken(LivingEntity entity) {
        return this.staggeredMobs.containsKey(entity.getUniqueId());
    }

    public void updateStance(LivingEntity attacker, LivingEntity target) {

        // No stance system for players
        if (target instanceof Player) {
            return;
        }

        int remainingStance = getStance(target);

        // If the stance is already 0 here it means the entity is not interacting with the stance system
        if (remainingStance < 0) {
            return;
        }

        remainingStance -= getStanceDamage(attacker);

        if (remainingStance <= 0) {
            stunEntity(target);
            remainingStanceHealths.remove(target.getUniqueId());
        } else {
            remainingStanceHealths.put(target.getUniqueId(), remainingStance);
        }

    }

    public void stunEntity(LivingEntity entity) {

        TextDisplay display = entity.getWorld().spawn(entity.getEyeLocation().add(0, 0.5, 0), TextDisplay.class, d -> {
            d.text(TextUtil.color("<blue>STUNNED!   <yellow>5.0s"));
        });

        entity.setAI(false);

        Location lookPos = entity.getLocation().add(entity.getLocation().getDirection().multiply(entity.getHeight() * 1.5));
        entity.lookAt(lookPos.x(), lookPos.y(), lookPos.z(), LookAnchor.EYES);

        BukkitTask task = new BukkitRunnable() {

            int stunTicks = 20 * 5;

            @Override
            public void run() {

                if (stunTicks <= 0) {
                    display.remove();
                    unstunEntity(entity);
                } else {

                    if (stunTicks % 2 == 0) {
                        display.text(TextUtil.color("<blue>STUNNED!   <yellow>%.1fs", stunTicks * 0.05));
                    }

                    stunTicks--;
                }
            }
        }.runTaskTimer(VoidSurvival.getInstance(), 0, 1);

        this.staggeredMobs.put(entity.getUniqueId(), task);
    }

    public void unstunEntity(LivingEntity entity) {

        BukkitTask task = this.staggeredMobs.remove(entity.getUniqueId());

        if (task != null) {
            task.cancel();
        }

        entity.setAI(true);
    }

    public void reload() throws IOException {

        File file = FileUtil.createOrGetFile("mob_stances.json");

        if (file != null) {

            JsonNode node = Json.readFromFile(file);

            stanceHealths.clear();

            node.fields().forEachRemaining(entry -> {
                stanceHealths.put(entry.getKey(), entry.getValue().asInt());
            });

            VoidSurvival.logInfo("Loaded %d stance values", stanceHealths.size());
        } else {
            VoidSurvival.logError("Failed to load stance values from mob_stances.json because the file could not be found nor created.");
        }
    }
}
