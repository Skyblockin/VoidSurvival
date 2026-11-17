package com.skyblockin.voidsurvival.command.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.manager.Manager;
import com.skyblockin.voidsurvival.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.HashMap;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class ToggleCommand {

    private static HashMap<String, Manager> managers = new HashMap<>();

    private static void register(String featureName, Manager manager) {
        managers.put(featureName, manager);
    }

    static {
        register("mining", VoidSurvival.getInstance().getMiningManager());
    }

    public static final LiteralCommandNode<CommandSourceStack> COMMAND = literal("toggle")
        .requires(ctx -> ctx.getSender().isOp())
        .then(argument("feature", StringArgumentType.string())
            .suggests(CommandUtil.suggest(() -> managers.keySet()))
            .executes(ctx -> {

                String featureName = StringArgumentType.getString(ctx, "feature");

                Manager manager = managers.get(featureName);

                if (manager == null) {
                    ctx.getSource().getSender().sendRichMessage("Feature not found");
                    return 1;
                }

                boolean enabled = !manager.isEnabled();

                manager.setEnabled(enabled);

                if (enabled) {
                    manager.onEnable(VoidSurvival.getInstance());
                    ctx.getSource().getSender().sendRichMessage("Feature" + featureName + " enabled");
                } else {
                    manager.onDisable(VoidSurvival.getInstance());
                    ctx.getSource().getSender().sendRichMessage("Feature " + featureName + " disabled");
                }

                return 1;
            })
        )
        .build();

}
