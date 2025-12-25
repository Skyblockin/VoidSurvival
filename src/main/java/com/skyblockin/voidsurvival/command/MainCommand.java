package com.skyblockin.voidsurvival.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.command.admin.Admin;
import com.skyblockin.voidsurvival.command.admin.Chest;
import com.skyblockin.voidsurvival.command.admin.Debug;
import com.skyblockin.voidsurvival.command.admin.Give;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.function.Consumer;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class MainCommand {

    public static final LiteralArgumentBuilder<CommandSourceStack> COMMAND = literal("vs")
        .then(Admin.COMMAND)
        .then(Chest.COMMAND)
        .then(Give.COMMAND)
        .then(Debug.COMMAND)
        .then(quickOpCommand("reload", ctx -> {
                VoidSurvival.getInstance().reload();
                ctx.getSource().getSender().sendRichMessage("<green>VoidSurvival reloaded!");
            })
        );

    private static LiteralArgumentBuilder<CommandSourceStack> quickOpCommand(String name, Consumer<CommandContext<CommandSourceStack>> consumer) {

        return literal(name)
            .requires(ctx -> ctx.getSender().isOp())
            .executes(ctx -> {
                consumer.accept(ctx);
                return 1;
            });

    }

}
