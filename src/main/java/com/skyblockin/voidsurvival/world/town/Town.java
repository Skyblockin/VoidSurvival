package com.skyblockin.voidsurvival.world.town;

import com.skyblockin.voidsurvival.entity.AllySet;
import com.skyblockin.voidsurvival.entity.trait.FighterTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashSet;

public class Town {

    public String name;
    public String id;
    public Location center;
    public String regionId;
    public HashSet<String> enemies;
    public HashSet<String> allies;
    public int population;
    public Color color;

    public boolean isEnemiesWith(Town other) {
        return other.enemies.contains(id);
    }

    public boolean isAlliedWith(Town other) {
        return other.allies.contains(id);
    }

    public NPC createSoldier(Location location) {

        NPC npc = CitizensAPI.getTemporaryNPCRegistry().createNPC(EntityType.PLAYER, "§c" + name + " Soldier");

        npc.setAlwaysUseNameHologram(true);
        npc.setProtected(false);
        npc.addTrait(FighterTrait.class);

        Equipment equipment = npc.getOrAddTrait(Equipment.class);

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        helmet.editMeta(LeatherArmorMeta.class, meta -> {
            if (color != null) meta.setColor(color);
        });

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        chestplate.editMeta(LeatherArmorMeta.class, meta -> {
            if (color != null) meta.setColor(color);
        });

        equipment.set(Equipment.EquipmentSlot.HELMET, helmet);
        equipment.set(Equipment.EquipmentSlot.BODY, chestplate);
        equipment.set(Equipment.EquipmentSlot.LEGGINGS, new ItemStack(Material.IRON_LEGGINGS));
        equipment.set(Equipment.EquipmentSlot.BOOTS, new ItemStack(Material.IRON_BOOTS));

        equipment.set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.DIAMOND_AXE));

        npc.spawn(location);

        NavigatorParameters params = npc.getNavigator().getLocalParameters();

        params.avoidWater(false);

        npc.data().set("allies", new AllySet(allies));
        npc.data().set("faction", id);
        npc.data().set("id", id + "_soldier");
        npc.data().set("name", name + " Soldier");

        return npc;
    }

    public void beginSiege(Town other) {

        Siege siege = new Siege(this, other);

        siege.begin();

    }
}
