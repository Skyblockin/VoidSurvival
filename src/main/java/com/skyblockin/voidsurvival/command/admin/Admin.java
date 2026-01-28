package com.skyblockin.voidsurvival.command.admin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.WorldEditException;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.event.CampfireLoadEvent;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.math.Position;
import com.skyblockin.voidsurvival.util.CommandUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import com.skyblockin.voidsurvival.world.WorldEditUtil;
import com.skyblockin.voidsurvival.world.WorldGuardUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Campfire;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage")
public final class Admin {

    private static final List<String> VALID_STATISTIC_NAMES;

    static {
        VALID_STATISTIC_NAMES = Arrays.stream(Statistic.values())
            .map(Enum::name)
            .toList();
    }

    public static final LiteralCommandNode<CommandSourceStack> COMMAND = literal("admin")
        .requires(ctx -> ctx.getSender().isOp())
        .then(literal("setstatistic")
            .then(argument("players", ArgumentTypes.players())
                .then(argument("statistic", StringArgumentType.word())
                    .suggests(CommandUtil.suggest(() -> VALID_STATISTIC_NAMES))
                    .then(argument("material", ArgumentTypes.namespacedKey())
                        .then(argument("value", IntegerArgumentType.integer(0))
                            .executes(ctx -> {

                                Statistic statistic = Statistic.valueOf(ctx.getArgument("statistic", String.class).toUpperCase());
                                NamespacedKey materialKey = ctx.getArgument("material", NamespacedKey.class);
                                Material material = Registry.MATERIAL.get(materialKey);
                                int value = ctx.getArgument("value", Integer.class);

                                if (material == null) {
                                    ctx.getSource().getSender().sendRichMessage("<red>That is not a valid material!");
                                    return 1;
                                }

                                List<Player> players = ctx.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());

                                for (Player player : players) {
                                    player.setStatistic(statistic, material, value);
                                }

                                return 1;
                            })
                        )
                    )
                )
            )
        )
        .then(literal("setsigncommand")
            .then(argument("command", StringArgumentType.greedyString())
                .executes(CommandUtil.playerCommand((ctx, player) -> {

                    String command = ctx.getArgument("command", String.class);

                    Block target = player.getTargetBlockExact(10);

                    if (target != null && target.getState() instanceof Sign sign) {
                        Accessors.SIGN_COMMAND.write(sign, command);
                        sign.update();
                        player.sendRichMessage("<green>Sign command set!");
                    } else {
                        player.sendRichMessage("<red>Could not find a valid sign in front of you!");
                    }
                }))
            )
        )
        .then(literal("removesigncommand")
            .executes(CommandUtil.playerCommand((ctx, player) -> {

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

            }))
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
        .then(literal("home")
            .then(argument("home", StringArgumentType.word())
                .executes(CommandUtil.playerCommand((ctx, player) -> {

                    String name = ctx.getArgument("home", String.class);

                    try {

                        PlayerData data = PlayerData.getByNameorUuid(name);

                        if (data == null) {
                            player.sendRichMessage("<red>No player with name or uuid '" + name + "' found!");
                            return;
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

                    } catch (Exception ex) {
                        VoidSurvival.logError("Failed to get homes of player '" + name + "'", ex);
                    }

                }))
            )
        )
        .then(literal("homes")
            .then(argument("name", StringArgumentType.word())
                .then(argument("home", StringArgumentType.word())
                    .executes(CommandUtil.playerCommand((ctx, player) -> {

                        String name = ctx.getArgument("name", String.class);
                        String homeName = ctx.getArgument("home", String.class);

                        try {

                            PlayerData data = PlayerData.getByNameorUuid(name);

                            if (data == null) {
                                player.sendRichMessage("<red>No player with name or uuid '" + name + "' found!");
                                return;
                            }

                            Location home = data.getHome(homeName);

                            if (home == null) {
                                player.sendRichMessage("<red>Could not find a home with the name '" + homeName + "'.");
                                return;
                            }

                            player.teleportAsync(home);
                            player.sendRichMessage("<green>You have been teleported to " + name + "'s home '" + homeName + "'.");

                        } catch (Exception ex) {
                            VoidSurvival.logError("Failed to get homes of player '" + name + "'", ex);
                        }
                    }))
                )
            )
        )
        .then(literal("createcampfirewarp")
            .then(argument("name", StringArgumentType.greedyString())
                .executes(CommandUtil.playerCommand((ctx, player) -> {

                    Block targetBlock = player.getTargetBlockExact(10);

                    if (targetBlock != null && targetBlock.getState() instanceof Campfire campfire) {

                        String name = ctx.getArgument("name", String.class);

                        Accessors.CAMPFIRE_WARP_ID.write(campfire, name);
                        Accessors.CAMPFIRE_WARP_POSITION.write(campfire, Position.ofLocation(player.getLocation()));
                        campfire.update();

                        CampfireLoadEvent.callEvent(player, name, targetBlock.getLocation());

                        player.sendRichMessage("<green>Successfully created a new campfire with the name '" + name + "'");

                    } else {
                        player.sendRichMessage("<red>That's not a campfire!");
                    }

                }))
            )
        )
        .then(literal("removecampfireaccess")
            .then(argument("players", ArgumentTypes.players())
                .then(argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> {

                        List<Player> players = ctx.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
                        String warpName = ctx.getArgument("name", String.class);

                        for (Player player : players) {
                            PlayerData.of(player).campfires.remove(warpName);
                        }

                        return 1;
                    })
                )
            )
        )
        .then(literal("savedungeon")
            .then(argument("region", StringArgumentType.word())
                .suggests(CommandUtil.suggestToPlayerSender(player -> WorldGuardUtil.getAllRegionsInWorld(player.getWorld())))
                .executes(CommandUtil.playerCommand((ctx, player) -> {

                    String regionId = ctx.getArgument("region", String.class);

                    try {
                        Location location = player.getLocation();

                        WorldEditUtil.saveDungeon(player.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), regionId);
                    } catch (IOException | WorldEditException e) {
                        VoidSurvival.logError("Failed to save dungeon: ", e);
                    }

                }))
            )
        )
        .then(literal("pastedungeon")
            .then(argument("pos", ArgumentTypes.blockPosition())
                .then(argument("name", StringArgumentType.word())
                    .then(argument("schematic", StringArgumentType.word())
                        .executes(CommandUtil.playerCommand((ctx, player) -> {

                            String name = ctx.getArgument("name", String.class);
                            String schematic = ctx.getArgument("schematic", String.class);

                            try {
                                BlockPosition position = ctx.getArgument("pos", BlockPositionResolver.class).resolve(ctx.getSource());
                                WorldEditUtil.pasteDungeonAt(name, schematic, player.getWorld(), position.blockX(), position.blockY(), position.blockZ());
                            } catch (CommandSyntaxException e) {
                                VoidSurvival.logError("Failed to paste dungeon: ", e);
                            }

                        }))
                    )
                )
            )
        )
        .build();

}
