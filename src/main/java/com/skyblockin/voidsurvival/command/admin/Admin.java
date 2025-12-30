package com.skyblockin.voidsurvival.command.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.DialogUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage")
public final class Admin {

    public static final LiteralCommandNode<CommandSourceStack> COMMAND = literal("admin")
        .requires(ctx -> ctx.getSender().isOp())
        .then(literal("setsigncommand")
            .requires(ctx -> ctx.getExecutor() instanceof Player)
            .then(argument("command", StringArgumentType.greedyString())
                .executes(ctx -> {

                    String command = ctx.getArgument("command", String.class);

                    if (ctx.getSource().getExecutor() instanceof Player player) {

                        Block target = player.getTargetBlockExact(10);

                        if (target != null && target.getState() instanceof Sign sign) {
                            Accessors.SIGN_COMMAND.write(sign, command);
                            sign.update();
                            player.sendRichMessage("<green>Sign command set!");
                        } else {
                            player.sendRichMessage("<red>Could not find a valid sign in front of you!");
                        }
                    }

                    return 1;
                })
            )
        )
        .then(literal("removesigncommand")
            .requires(ctx -> ctx.getExecutor() instanceof Player)
            .executes(ctx -> {

                if (ctx.getSource().getExecutor() instanceof Player player) {

                    Block target = player.getTargetBlockExact(10);

                    if (target != null && target.getState() instanceof Sign sign) {

                        String command = Accessors.SIGN_COMMAND.read(sign);

                        if (command != null) {
                            sign.getPersistentDataContainer().remove(VoidSurvival.createKey("sign_command"));
                            sign.update();
                            player.sendRichMessage("<green>Removed command <reset>'" + command + "'</reset> from the sign!");
                        } else {
                            player.sendRichMessage("<red>That sign does not have a command set!");
                        }

                    } else {
                        player.sendRichMessage("<red>Could not find a valid sign in front of you!");
                    }
                }

                return 1;
            })
        )
        .then(literal("testmsg")
            .then(argument("text", StringArgumentType.greedyString())
                .executes(ctx -> {

                    String msg = ctx.getArgument("text", String.class);
                    ctx.getSource().getSender().sendMessage(TextUtil.color(msg));

                    return 1;
                })
            )
        )
        .then(literal("testdialog")
            .executes(ctx -> {

                Collection<? extends Player> players = Bukkit.getOnlinePlayers();

                Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(TextUtil.color("<blue>Test Dialog"))
                        .canCloseWithEscape(true)
                        .body(DialogUtil.buildDialogBody(
                            "<white>Line 1",
                            "<blue>Line 2",
                            "<red>Line 3"
                        ))
                        .inputs(List.of(
                            DialogUtil.buildBooleanInput("firstBool", "Boolean", "No", "Yes", true),
                            DialogUtil.buildNumberRangeInput("firstNumberRange", "Number Range", 0, 100, "%s: %s", 50, 1),
                            DialogUtil.buildTextInput("firstTextInput", "Text Input")
                        ))
                        .build()
                    )
                    .type(DialogUtil.buildMultiAction(
                        List.of(
                            DialogUtil.buildActionButton("Option 1", 100,
                                audience -> audience.sendMessage(TextUtil.color("<green>Clicked option 1!"))
                            ),
                            DialogUtil.buildActionButton("Option 2", 100,
                                audience -> audience.sendMessage(TextUtil.color("<green>Clicked option 2!"))
                            ),
                            DialogUtil.buildActionButton("Option 3", 100,
                                audience -> audience.sendMessage(TextUtil.color("<green>Clicked option 3!"))
                            ),
                            DialogUtil.buildActionButton("Option 4", 100,
                                audience -> audience.sendMessage(TextUtil.color("<green>Clicked option 4!"))
                            )
                        ),
                        DialogUtil.buildActionButton("Close", 100, audience -> audience.sendMessage(TextUtil.color("<green>Dialog closed!"))),
                        2
                    ))
                );

                if (ctx.getSource().getSender() instanceof Player player) {
                    player.showDialog(dialog);
                }

                return 1;
            })
        )
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
