package com.skyblockin.voidsurvival.entity;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.ItemData;
import com.skyblockin.voidsurvival.storage.Accessors;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class EntityEquipmentHandler implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {

        if (event.getEntity() instanceof LivingEntity entity) {

            EntityEquipment equipment = entity.getEquipment();

            if (equipment == null) {
                return;
            }

            for (EquipmentSlot slot : EquipmentSlot.values()) {

                ItemStack item = equipment.getItem(slot);
                String id = Accessors.ITEM_REPLACEMENT.read(item);

                if (id == null) continue;

                ItemData data = VoidSurvival.getInstance().getItemManager().getItem(id);

                if (data == null) {
                    VoidSurvival.logError("Item with id '%s' specified in entity equipment replacements does not exist!", id);
                } else {
                    equipment.setItem(slot, data.createItem());
                }
            }
        }
    }

}
