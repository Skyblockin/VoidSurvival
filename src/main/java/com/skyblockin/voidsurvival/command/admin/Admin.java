package com.skyblockin.voidsurvival.command.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class Admin {

    public static final LiteralCommandNode<CommandSourceStack> COMMAND = literal("admin")
        .requires(ctx -> ctx.getSender().isOp())
        .then(literal("home")
            .then(argument("home", StringArgumentType.word())
                .executes(ctx -> {

                    String name = ctx.getArgument("home", String.class);

                    try {

                        if (ctx.getSource().getSender() instanceof Player player) {

                            PlayerData data = PlayerData.getByNameorUuid(name);

                            if (data == null) {
                                player.sendRichMessage("<red>No player with name or uuid '" + name + "' found!");
                                return 1;
                            }

                            Component component = TextUtil.color("<green><bold>Homes of %s:</bold></green>", name);

                            int counter = 0;

                            for (Map.Entry<String, Location> entry : data.homes.entrySet()) {

                                String homeName = entry.getKey();
                                Location home = entry.getValue();

                                counter++;

                                component = component
                                    .append(
                                        TextUtil.color("\n<gray>- %d. <aqua><underline>%s</underline></aqua>", counter, homeName)
                                            .clickEvent(ClickEvent.runCommand(String.format("/tp %f %f %f", home.getX(), home.getY(), home.getZ())))
                                            .hoverEvent(TextUtil.color("<gray>Click to teleport to home '%s' at <white>%d, %d, %d</white>",
                                                homeName, home.getBlockX(), home.getBlockY(), home.getBlockZ())
                                            )
                                    );
                            }

                            player.sendMessage(component);

                        } else {
                            ctx.getSource().getSender().sendRichMessage("<red>This command can only be executed by a player!");
                        }

                    } catch (Exception ex) {
                        VoidSurvival.logError("Failed to get homes of player '" + name + "'", ex);
                    }

                    return 1;
                })
            )
        )
        .then(literal("homes")
            .then(argument("name", StringArgumentType.word())
                .then(argument("home", StringArgumentType.word())
                    .executes(ctx -> {

                        String name = ctx.getArgument("name", String.class);
                        String homeName = ctx.getArgument("home", String.class);

                        try {

                            if (ctx.getSource().getSender() instanceof Player player) {

                                PlayerData data = PlayerData.getByNameorUuid(name);

                                if (data == null) {
                                    player.sendRichMessage("<red>No player with name or uuid '" + name + "' found!");
                                    return 1;
                                }

                                Location home = data.getHome(homeName);

                                if (home == null) {
                                    player.sendRichMessage("<red>Could not find a home with the name '" + homeName + "'.");
                                    return 1;
                                }

                                player.teleportAsync(home);
                                player.sendRichMessage("<green>You have been teleported to " + name + "'s home '" + homeName + "'.");

                            } else {
                                ctx.getSource().getSender().sendRichMessage("<red>This command can only be executed by a player!");
                            }

                        } catch (Exception ex) {
                            VoidSurvival.logError("Failed to get homes of player '" + name + "'", ex);
                        }

                        return 1;
                    })
                )
            )
        )
        .build();

}
