package com.skyblockin.voidsurvival.combat;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.constants.DamageTypes;
import com.skyblockin.voidsurvival.constants.Enchantments;
import com.skyblockin.voidsurvival.constants.ItemIds;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.EntityTypeTagKeys;
import io.papermc.paper.tag.EntityTags;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.damage.DamageSource;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class CombatTracker implements Listener {

    private final HashMap<UUID, Double> bleedMap = new HashMap<>();
    private final HashMap<UUID, BossBar> bleedingBars = new HashMap<>();
    private final HashMap<UUID, Long> lastDamageTimes = new HashMap<>();

    public CombatTracker() {

        Server server = VoidSurvival.getInstance().getServer();

        VoidSurvival.getInstance().runTaskTimer(() -> {

            server.getOnlinePlayers().forEach(player -> {

                ItemStack item = player.getEquipment().getHelmet();

                if (item != null && !item.isEmpty() && Accessors.ITEM_ID.equals(item, ItemIds.AVARITIA)) {
                    player.damage(4, DamageSource.builder(DamageTypes.GREED).build());
                }
            });

        }, 0, 20L);

        // Let bleeding decay by 5 points every half a second, meaning 10 seconds to decay from a full bar
        VoidSurvival.getInstance().runTaskTimer(() -> {

            List<UUID> keysToRemove = new ArrayList<>();

            bleedMap.replaceAll((uuid, value) -> {

                Player player = Bukkit.getPlayer(uuid);

                // Play a "bleeding" effect around the player
                if (player != null) {
                    player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE,
                        player.getBoundingBox().getCenter().toLocation(player.getWorld()),
                        10, 0.5, player.getBoundingBox().getHeight() / 2.0, 0.5, 1,
                        BlockType.REDSTONE_BLOCK.createBlockData()
                    );
                }

                // If the player was damaged in the past 5 seconds, prevent bleeding from decaying
                if (lastDamageTimes.getOrDefault(uuid, 0L) + 5000L > System.currentTimeMillis()) {
                    return value;
                }

                double newValue = value - 5;

                if (newValue <= 0) {
                    keysToRemove.add(uuid);
                } else {
                    bleedingBars.computeIfPresent(uuid, (key, bar) ->
                        bar.name(getBleedingTitleForBleedingAmount(newValue))
                            .progress(Math.clamp((float) (newValue / 100), 0F, 1F))
                    );
                }

                return newValue;
            });

            // Cleaning up
            keysToRemove.forEach(uuid -> {
                bleedMap.remove(uuid);
                lastDamageTimes.remove(uuid);

                Player player = Bukkit.getPlayer(uuid);
                BossBar bar = bleedingBars.remove(uuid);

                if (player != null) player.hideBossBar(bar);
            });

        }, 0, 10L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (Registry.ENTITY_TYPE.getTag(EntityTypeTagKeys.ARROWS).contains(
            TypedKey.create(RegistryKey.ENTITY_TYPE, event.getEntity().getType().key())
        )) {
            event.getEntity().remove();
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        // Proteccc
        if (event.getDamageSource().isIndirect()
            || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
            || event.getDamageSource().getDamageType().equals(DamageTypes.BLEED)
        ) {
            return;
        }

        if (event.getDamager() instanceof LivingEntity damager && event.getEntity() instanceof Player player) {

            // Also known as the last time the player was damaged by a living entity
            lastDamageTimes.put(player.getUniqueId(), System.currentTimeMillis());

            EntityEquipment equipment = damager.getEquipment();

            // Null equipment cannot possibly have an enchantment
            if (equipment == null) return;

            ItemStack weapon = equipment.getItemInMainHand();

            int bleedingLevel = weapon.getEnchantmentLevel(Enchantments.BLEED);

            double bleedResistance = calculateBleedResistance(equipment);
            double bleedingDamage = 5 * bleedingLevel * (1.0 - bleedResistance) * (event.isCritical() ? 1.5 : 1.0) * player.getAttackCooldown();
            double currentBleeding = bleedMap.getOrDefault(player.getUniqueId(), 0.0) + bleedingDamage;

            bleedingBars.computeIfAbsent(player.getUniqueId(), key -> {

                BossBar bar = BossBar.bossBar(
                    getBleedingTitleForBleedingAmount(currentBleeding), (float) (currentBleeding / 100), BossBar.Color.RED,
                    BossBar.Overlay.NOTCHED_20
                );

                player.showBossBar(bar);

                return bar;

            }).progress(Math.clamp((float) (currentBleeding / 100), 0F, 1F))
                .name(getBleedingTitleForBleedingAmount(currentBleeding));

            if (currentBleeding >= 100) {

                player.damage(8, DamageSource.builder(DamageTypes.BLEED)
                    .withDirectEntity(damager)
                    .withCausingEntity(damager)
                    .build()
                );

                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE,
                    player.getLocation().clone().add(0, 1, 0),
                    200, 0.5, 0.5, 0.5, 0,
                    BlockType.REDSTONE_BLOCK.createBlockData()
                );

                player.playSound(player.getLocation(), Sound.ENTITY_TURTLE_EGG_BREAK, 2F, 0.5F);

                bleedMap.remove(player.getUniqueId());
                player.hideBossBar(bleedingBars.remove(player.getUniqueId()));

            } else {
                bleedMap.put(player.getUniqueId(), currentBleeding);
            }
        }

    }

    private Component getBleedingTitleForBleedingAmount(double bleeding) {
        return TextUtil.color("<dark_red><bold>BLEED</bold> <red>[%d/100]", (int) bleeding);
    }

    private double calculateBleedResistance(EntityEquipment equipment) {

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

    @EventHandler
    public void onKill(PlayerDeathEvent event) {

        DamageSource source = event.getDamageSource();

        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            event.deathMessage(deathMessage.color(TextColor.color(0xCC0000)));
        }

        // Funny
        if (source.getDamageType().equals(DamageTypes.GREED)) {
            event.deathMessage(Component.text(event.getPlayer().getName() + " got too greedy.", TextColor.color(0xCC0000)));
            event.deathScreenMessageOverride(Component.text("You got too greedy.", TextColor.color(0xCC0000)));
        }

        if (source.getDamageType().equals(DamageTypes.BLEED)) {
            event.deathMessage(Component.text(event.getPlayer().getName() + " bled to death.", TextColor.color(0xCC0000)));
            event.deathScreenMessageOverride(Component.text("You bled to death.", TextColor.color(0xCC0000)));
        }

        // Don't allow a player to get a killstreak on themselves lol
        if (source.getCausingEntity() instanceof Player player && !player.equals(event.getPlayer())) {

            PlayerData data = PlayerData.of(player);
            data.kills++;
            data.killStreak++;

            if (data.killStreak % 10 == 0) {
                Bukkit.broadcast(TextUtil.color("%s has reached a kill streak of %s!", player.getName(), data.killStreak));
            }
        }

        // But definitely allow them to reset their own killstreak and shame them for it!
        PlayerData data = PlayerData.of(event.getPlayer());

        if (data.killStreak >= 10 && source.getCausingEntity() instanceof Player player) {
            Bukkit.broadcast(TextUtil.color("%s has ended %s's kill streak of %d!", player.getName(), event.getPlayer().getName(), data.killStreak));
        }

        data.killStreak = 0;
    }

}
