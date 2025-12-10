package com.skyblockin.voidsurvival;

import com.skyblockin.voidsurvival.command.AdminCommands;
import com.skyblockin.voidsurvival.command.BasicCommands;
import com.skyblockin.voidsurvival.enchantment.CustomEnchantments;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.event.RegistryEvents;
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

        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(CustomEnchantments.BLEED::register));
    }

}
