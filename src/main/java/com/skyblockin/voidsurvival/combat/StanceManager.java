package com.skyblockin.voidsurvival.combat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.ItemData;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.util.FileUtil;
import com.skyblockin.voidsurvival.util.TagUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class StanceManager {

    private final HashMap<UUID, Integer> remainingStanceHealths = new HashMap<>();
    private final HashMap<String, Integer> stanceHealths = new HashMap<>();
    private final HashMap<UUID, Long> staggeredMobs = new HashMap<>();

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
            // TODO: Play stance break effect and stun the mob?
            remainingStanceHealths.remove(target.getUniqueId());
        } else {
            remainingStanceHealths.put(target.getUniqueId(), remainingStance);
        }

    }

    private void stunEntity(LivingEntity entity) {

        TextDisplay display = entity.getWorld().spawn(entity.getEyeLocation().add(0, 0.5, 0), TextDisplay.class, d -> {
            d.text(TextUtil.color("<blue>STUNNED!   <yellow>5.0s"));
        });

        new BukkitRunnable() {

            int stunTicks = 20 * 5;

            @Override
            public void run() {

                if (stunTicks <= 0) {
                    display.remove();
                    cancel();
                } else {

                    if (stunTicks % 2 == 0) {
                        display.text(TextUtil.color("<blue>STUNNED!   <yellow>%.1fs", stunTicks * 0.05));
                    }

                    stunTicks--;
                }
            }
        }.runTaskTimer(VoidSurvival.getInstance(), 0, 1);

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
