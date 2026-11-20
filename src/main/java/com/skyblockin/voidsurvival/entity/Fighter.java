package com.skyblockin.voidsurvival.entity;

import com.skyblockin.voidsurvival.entity.trait.FighterTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Fighter implements CustomEntity<NPC> {

    public NPC spawn(Location location) {

        NPC npc = CitizensAPI.getTemporaryNPCRegistry().createNPC(EntityType.PLAYER, "Fighter");

        npc.setAlwaysUseNameHologram(true);
        npc.setProtected(false);
        npc.addTrait(FighterTrait .class);

        Equipment equipment = npc.getOrAddTrait(Equipment.class);

        equipment.set(Equipment.EquipmentSlot.HELMET, new ItemStack(Material.IRON_HELMET));
        equipment.set(Equipment.EquipmentSlot.BODY, new ItemStack(Material.IRON_CHESTPLATE));
        equipment.set(Equipment.EquipmentSlot.LEGGINGS, new ItemStack(Material.IRON_LEGGINGS));
        equipment.set(Equipment.EquipmentSlot.BOOTS, new ItemStack(Material.IRON_BOOTS));

        equipment.set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.DIAMOND_AXE));

        npc.spawn(location);

        NavigatorParameters params = npc.getNavigator().getLocalParameters();

        params.avoidWater(false);

        return npc;
    }

    @Override
    public boolean isValidForSpawn(Location location) {
        return true;
    }


}
