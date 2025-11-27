package com.skyblockin;

import com.skyblockin.command.AdminCommands;
import com.skyblockin.command.BasicCommands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class VoidSurvivalBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            BasicCommands.register(commands.registrar());
            AdminCommands.register(commands.registrar());
        });

    }

}
