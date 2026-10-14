package com.skyblockin.voidsurvival.entity.boss;

import com.skyblockin.voidsurvival.entity.boss.trait.RoyalRevenantTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class RoyalRevenant {

    public static NPC spawn(Location location) {

        NPC npc = CitizensAPI.getTemporaryNPCRegistry().createNPC(EntityType.ZOMBIE, "§cRoyal Revenant");

        npc.setAlwaysUseNameHologram(true);
        npc.setProtected(false);

        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        npc.addTrait(RoyalRevenantTrait.class);

        equipment.setCosmetic(Equipment.EquipmentSlot.BODY, new ItemStack(Material.DIAMOND_CHESTPLATE));
        equipment.setCosmetic(Equipment.EquipmentSlot.LEGGINGS, new ItemStack(Material.CHAINMAIL_LEGGINGS));
        equipment.setCosmetic(Equipment.EquipmentSlot.BOOTS, new ItemStack(Material.DIAMOND_BOOTS));

        equipment.set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.DIAMOND_AXE));

        npc.spawn(location);

        LivingEntity entity = (LivingEntity) npc.getEntity();

        entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(50.0);
        entity.getAttribute(Attribute.SCALE).setBaseValue(1.5);
        entity.setHealth(50.0);

        npc.data().set("id", "royal_revenant");
        npc.data().set("name", "Royal Revenant");

        return npc;
    }

}
