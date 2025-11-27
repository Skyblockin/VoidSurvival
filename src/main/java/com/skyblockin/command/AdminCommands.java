package com.skyblockin.command;

import com.skyblockin.VoidSurvival;
import io.papermc.paper.command.brigadier.Commands;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class AdminCommands {

    public static void register(Commands commands) {

        commands.register(literal("vsreload")
            .executes(ctx -> {

                VoidSurvival.getInstance().reload();

                ctx.getSource().getSender().sendRichMessage("<green>VoidSurvival reloaded!");

                return 1;
            })
            .build()
        );

        commands.register(literal("vsdebug")
            .executes(ctx -> {

                boolean debug = !VoidSurvival.getInstance().getConfig().getBoolean("debug");
                VoidSurvival.getInstance().getConfig().set("debug", debug);
                VoidSurvival.getInstance().saveConfig();

                if (debug) {
                    ctx.getSource().getSender().sendRichMessage("<green>[VoidSurvival] Debug Mode enabled.");
                } else {
                    ctx.getSource().getSender().sendRichMessage("<green>[VoidSurvival] <red>Debug Mode disabled.");
                }

                return 1;
            })
            .build()
        );

    }

}
