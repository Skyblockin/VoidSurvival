package com.skyblockin.voidsurvival;

import com.skyblockin.voidsurvival.command.BasicCommands;
import com.skyblockin.voidsurvival.command.MainCommand;
import com.skyblockin.voidsurvival.constants.CustomEnchantments;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DamageTypeKeys;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageScaling;
import org.bukkit.damage.DamageType;
import org.bukkit.damage.DeathMessageType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class VoidSurvivalBootstrap implements PluginBootstrap {

    private final TypedKey<@NotNull DamageType> BLEED_DAMAGE_TYPE_KEY = makeKey("bleed");
    private final TypedKey<@NotNull DamageType> GREED_DAMAGE_TYPE_KEY = makeKey("greed");

    private TypedKey<@NotNull DamageType> makeKey(String name) {
        return DamageTypeKeys.create(Key.key("voidsurvival", name));
    }

    @Override
    public void bootstrap(BootstrapContext context) {

        LifecycleEventManager<@NotNull BootstrapContext> manager = context.getLifecycleManager();

        manager.registerEventHandler(RegistryEvents.DAMAGE_TYPE.compose().newHandler(event -> {

            try {

                event.registry().register(BLEED_DAMAGE_TYPE_KEY, b -> {
                    b.damageEffect(DamageEffect.HURT)
                        .messageId("bleed")
                        .damageScaling(DamageScaling.ALWAYS)
                        .exhaustion(1F)
                        .deathMessageType(DeathMessageType.DEFAULT);
                });

                event.registry().register(GREED_DAMAGE_TYPE_KEY, b -> {
                    b.damageEffect(DamageEffect.HURT)
                        .messageId("greed")
                        .damageScaling(DamageScaling.NEVER)
                        .exhaustion(1F)
                        .deathMessageType(DeathMessageType.DEFAULT);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }));

        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.DAMAGE_TYPE).newHandler(event -> {
            event.registrar().addToTag(DamageTypeTagKeys.NO_KNOCKBACK, List.of(GREED_DAMAGE_TYPE_KEY, BLEED_DAMAGE_TYPE_KEY));
            event.registrar().addToTag(DamageTypeTagKeys.NO_IMPACT, List.of(GREED_DAMAGE_TYPE_KEY, BLEED_DAMAGE_TYPE_KEY));
            event.registrar().addToTag(DamageTypeTagKeys.BYPASSES_ARMOR, List.of(BLEED_DAMAGE_TYPE_KEY));
            event.registrar().addToTag(DamageTypeTagKeys.BYPASSES_ENCHANTMENTS, List.of(BLEED_DAMAGE_TYPE_KEY));
        }));

        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(CustomEnchantments.BLEED::register));
    }

}
