package com.skyblockin.voidsurvival.combat.status;

import com.skyblockin.voidsurvival.combat.StatusManager;
import com.skyblockin.voidsurvival.combat.DamageTypes;
import com.skyblockin.voidsurvival.item.Enchantments;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockType;
import org.bukkit.damage.DamageSource;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("UnstableApiUsage")
public class BleedStatusManager extends StatusManager {

    public BleedStatusManager() {
        super("bleed", BossBar.Color.RED, BossBar.Overlay.NOTCHED_20);
    }

    @Override
    public double calculateStatusDamage(LivingEntity target, LivingEntity damager, EntityDamageByEntityEvent event) {

        EntityEquipment equipment = damager.getEquipment();

        if (equipment == null) return -1;

        ItemStack weapon = equipment.getItemInMainHand();

        int bleedingLevel = weapon.getEnchantmentLevel(Enchantments.BLEED);

        // If the sword has no bleeding, just don't do anything
        if (bleedingLevel == 0) return -1;

        double attackSpeedModifier = damager instanceof Player attacker ? attacker.getAttackCooldown() : 1.0;
        double bleedResistance = calculateResistance(target);

        return 10 * (1.0 - bleedResistance) * (event.isCritical() ? 1.5 : 1.0) * attackSpeedModifier;
    }

    @Override
    public void playStatusActivationEffect(LivingEntity target, LivingEntity damager, EntityDamageByEntityEvent event) {

        EntityEquipment equipment = damager.getEquipment();

        int bleedingLevel = 0;

        if (equipment != null) {
            bleedingLevel = equipment.getItemInMainHand().getEnchantmentLevel(Enchantments.BLEED);
        }

        target.damage(4 + 4 * bleedingLevel, DamageSource.builder(DamageTypes.BLEED)
            .withDirectEntity(damager)
            .withCausingEntity(damager)
            .build()
        );

        target.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE,
            target.getLocation().clone().add(0, 1, 0),
            200, 0.5, 0.5, 0.5, 0,
            BlockType.REDSTONE_BLOCK.createBlockData()
        );

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_TURTLE_EGG_BREAK, 2F, 0.5F);
    }

    @Override
    public void playStatusTickEffect(LivingEntity entity, double progress) {
        entity.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE,
            entity.getBoundingBox().getCenter().toLocation(entity.getWorld()),
            10, 0.5, entity.getBoundingBox().getHeight() / 2.0, 0.5, 1,
            BlockType.REDSTONE_BLOCK.createBlockData()
        );
    }

    @Override
    public double calculateResistance(LivingEntity entity) {

        EntityEquipment equipment = entity.getEquipment();

        if (equipment == null) return 0.0;

        double resistance = 0.0;

        for (ItemStack armor : equipment.getArmorContents()) {

            if (armor == null) continue;

            // +1% per protection level
            // +4% per protection 4
            // +16% from enchantments in total
            resistance += armor.getEnchantmentLevel(Enchantment.PROTECTION);

            ItemAttributeModifiers modifiers = armor.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);

            if (modifiers == null) continue;

            // Full armor bars would mean +20% bleed resistance
            for (ItemAttributeModifiers.Entry modifier : modifiers.modifiers()) {
                if (modifier.attribute().equals(Attributes.ARMOR)) {
                    resistance += modifier.modifier().getAmount();
                }
            }

            // Full protection 4 diamond armor would give +36% bleed resistance in total
        }

        return resistance / 100;
    }
}
