package com.skyblockin.voidsurvival;

import com.skyblockin.voidsurvival.command.AdminCommands;
import com.skyblockin.voidsurvival.command.BasicCommands;
import com.skyblockin.voidsurvival.constants.CustomEnchantments;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.DamageTypeRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DamageTypeKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageScaling;
import org.bukkit.damage.DamageType;
import org.bukkit.damage.DeathMessageType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class VoidSurvivalBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {

        LifecycleEventManager<@NotNull BootstrapContext> manager = context.getLifecycleManager();

        manager.registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            BasicCommands.register(commands.registrar());
            AdminCommands.register(commands.registrar());
        });

        manager.registerEventHandler(RegistryEvents.DAMAGE_TYPE.compose().newHandler(event -> {

            TypedKey<@NotNull DamageType> bleedDamageType = DamageTypeKeys.create(Key.key("voidsurvival", "bleed"));
            TypedKey<@NotNull DamageType> greedDamageType = DamageTypeKeys.create(Key.key("voidsurvival", "greed"));

            event.registry().register(bleedDamageType, b -> {
                b.damageEffect(DamageEffect.HURT)
                    .damageScaling(DamageScaling.ALWAYS)
                    .exhaustion(0.5F)
                    .deathMessageType(DeathMessageType.DEFAULT);
            });

            event.registry().register(greedDamageType, b -> {
                b.damageEffect(DamageEffect.HURT)
                    .damageScaling(DamageScaling.NEVER)
                    .exhaustion(0.0F)
                    .deathMessageType(DeathMessageType.DEFAULT);
            });
        }));

        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(CustomEnchantments.BLEED::register));
    }

}
