package com.skyblockin.voidsurvival.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.leaderboard.LeaderboardType;
import com.skyblockin.voidsurvival.math.Position;
import com.skyblockin.voidsurvival.message.MessageKeys;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.*;
import com.skyblockin.voidsurvival.world.IslandGenerator;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage")
public final class BasicCommands {

    private static Component renderLeaderboard(Component title, LeaderboardType type, List<PlayerData> leaderboard) {

        int counter = 0;

        for (PlayerData data : leaderboard) {
            counter++;
            title = title.appendNewline()
                //"<!b>%s%d. %s: %d"
                .append(TextUtil.message(MessageKeys.LEADERBOARD_ENTRY,
                    getRankingColor(counter), counter, data.lastKnownUserName, type == LeaderboardType.KILLS ? data.kills : data.killStreak)
                );
        }

        return title;
    }

    private static String getRankingColor(int ranking) {
        if (ranking == 1) {
            return "<gold>";
        } else if (ranking == 2) {
            return "<#c0c0c0>";
        } else if (ranking == 3) {
            return "<#cd7f32>";
        } else {
            return "<white>";
        }
    }

    private static Component getLeaderboard(LeaderboardType type, int limit) {

        List<PlayerData> leaderboard = VoidSurvival.getInstance().getLeaderboardManager().getLeaderboard(type, limit);

        return renderLeaderboard(TextUtil.message(MessageKeys.LEADERBOARD_HEADER, type.getDisplayName()), type, leaderboard);
    }

    private static void handlePlayerHome(Player player, String homeName) {

        PlayerData data = PlayerData.of(player);
        Location home = data.getHome(homeName);

        if (home == null) {
            if (homeName.equals("island")) {
                player.sendMessage(TextUtil.message(MessageKeys.HOME_DEFAULT_NOT_SET));
            } else {
                player.sendMessage(TextUtil.message(MessageKeys.HOME_NOT_FOUND, homeName));
            }
            return;
        }

        double eyeHeight = player.getEyeHeight();

        // Check for suffocation
        if (home.getWorld().getBlockAt(home.getBlockX(), (int) (home.getY() + eyeHeight), home.getBlockZ()).isSuffocating()) {
            player.sendMessage(TextUtil.message(MessageKeys.HOME_OBSTRUCTED));
            return;
        }

        Block block = home.getBlock();

        if (block.isSolid()) {
            if (block.getRelative(BlockFace.DOWN).isSolid()) {
                player.teleportAsync(home).thenAccept(success -> {
                    player.sendMessage(TextUtil.message(MessageKeys.HOME_TELEPORTED));
                });
            } else {
                player.teleportAsync(home.add(0, 1, 0)).thenAccept(success -> {
                    player.sendMessage(TextUtil.message(MessageKeys.HOME_TELEPORTED));
                });
            }
        } else {

            while (!block.isSolid()) {
                block = block.getRelative(BlockFace.DOWN);
            }

            if (block.getY() == block.getWorld().getMinHeight()) {
                player.sendMessage(TextUtil.message(MessageKeys.HOME_OBSTRUCTED));
                return;
            }

            player.teleportAsync(home).thenAccept(success -> {
                player.sendMessage(TextUtil.message(MessageKeys.HOME_TELEPORTED));
            });
        }
    }

    public static void register(Commands commands, YamlConfiguration aliasConfig) {

        commands.register(literal("warps")
            .requires(ctx -> ctx.getSender().hasPermission("voidsurvival.warpanywhere"))
            .executes(CommandUtil.playerCommand((ctx, player) -> {
                PlayerUtil.openWarpMenu(player);
            }))
            .build(), aliasConfig.getStringList("warps")
        );

        commands.register(literal("killstreaktop")
            .executes(ctx -> {
                ctx.getSource().getSender().sendMessage(getLeaderboard(LeaderboardType.KILLSTREAK, 10));
                return 1;
            })
            .build(), aliasConfig.getStringList("killstreaktop")
        );

        commands.register(literal("killtop")
            .executes(ctx -> {
                ctx.getSource().getSender().sendMessage(getLeaderboard(LeaderboardType.KILLS, 10));
                return 1;
            })
            .build(), aliasConfig.getStringList("killtop")
        );

        commands.register(literal("home")
                .then(argument("name", StringArgumentType.word())
                    .requires(ctx -> ctx.getSender().hasPermission("voidsurvival.home.multiple"))
                    .suggests(CommandUtil.suggestToPlayerSender(player -> PlayerData.of(player).getHomeNames()))
                    .executes(CommandUtil.playerCommand((ctx, player) -> {
                        handlePlayerHome(player, ctx.getArgument("name", String.class));
                    }))
                )
            .requires(ctx -> ctx.getSender().hasPermission("voidsurvival.home"))
            .executes(CommandUtil.playerCommand((ctx, player) ->
                handlePlayerHome(player, "island")
            ))
            .build(), aliasConfig.getStringList("home")
        );

        commands.register(literal("sethome")
            .then(argument("name", StringArgumentType.word())
                .requires(ctx -> ctx.getSender().isOp())
                .executes(CommandUtil.playerCommand((ctx, player) -> {
                    String name = ctx.getArgument("name", String.class);
                    PlayerData.of(player).setHome(name, player.getLocation());
                }))
            )
            .executes(CommandUtil.playerCommand((ctx, player) -> {

                Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(TextUtil.color("Home Alert"))
                        .body(DialogUtil.buildDialogBody(" ", " ", " ", " ", " ", " ", " ",
                            "Are you sure you want to set your home here?",
                            "You only get one home, so moving it here means your old home will be lost!"
                        ))
                        .build()
                    )
                    .type(DialogUtil.buildMultiAction(
                        List.of(
                            DialogUtil.buildActionButton("Yes", 100, audience -> {
                                PlayerData.of(player).setHome("island", player.getLocation());
                                player.sendMessage(TextUtil.message(MessageKeys.HOME_SET));
                            }),
                            DialogUtil.buildActionButton("No", 100, audience -> {
                                audience.sendMessage(TextUtil.message(MessageKeys.HOME_NOT_SET));
                            })
                        ),
                        DialogUtil.buildActionButton("Close", 100, audience -> {
                            audience.sendMessage(TextUtil.message(MessageKeys.HOME_NOT_SET));
                        }),
                        2
                    ))
                );

                player.showDialog(dialog);
            }))
            .build(), aliasConfig.getStringList("sethome")
        );

        commands.register(literal("campfire")
            .then(literal("unset")
                .then(argument("name", StringArgumentType.greedyString())
                    .suggests(CommandUtil.suggest(ctx -> {
                        if (ctx.getSource().getSender() instanceof Player player) {
                            return PlayerData.of(player).campfires.keySet();
                        }
                        return List.of();
                    }))
                    .executes(CommandUtil.playerCommand((ctx, player) -> {

                        String name = ctx.getArgument("name", String.class);
                        PlayerData data = PlayerData.of(player);

                        if (data.campfires.containsKey(name)) {
                            data.campfires.remove(name);
                            player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_DELETED, name));
                        } else {
                            player.sendMessage(TextUtil.message(MessageKeys.CAMPFIRE_NOT_FOUND, name));
                        }

                    }))
                )
            )
            .build(), aliasConfig.getStringList("campfire")
        );

        commands.register(literal("createisland")
            .requires(ctx -> ctx.getSender().hasPermission("voidsurvival.createisland"))
            .executes(CommandUtil.playerCommand((ctx, player) -> {

                PlayerData data = PlayerData.of(player);

                if (data.hasGeneratedIsland) {
                    player.sendMessage(TextUtil.message(MessageKeys.ISLAND_ALREADY_CREATED));
                    return;
                }

                if (data.generatingIsland) {
                    player.sendMessage(TextUtil.message(MessageKeys.ISLAND_ALREADY_GENERATING));
                    return;
                }

                data.generatingIsland = true;

                final long start = System.currentTimeMillis();

                IslandGenerator generator = VoidSurvival.getInstance()
                    .getIslandGenerator();

                player.sendMessage(TextUtil.message(MessageKeys.ISLAND_GENERATING));

                generator.findChunkForIsland(player.getWorld())
                    .thenAccept(chunk -> VoidSurvival.getInstance().runTask(() -> {

                        Location island = generator.generateIsland(chunk);

                        Database.saveIsland(island.getChunk().getX(), island.getChunk().getZ());
                        FileUtil.writeLogLine("islands.log", "%s %s %d %d %d",
                            ZonedDateTime.now().toString(), player.getName(), island.getBlockX(), island.getBlockY(), island.getBlockZ()
                        );

                        data.setHome("island", island.toCenterLocation());
                        data.hasGeneratedIsland = true;
                        data.generatingIsland = false;

                        VoidSurvival.getInstance().runTaskLater(() -> {

                            player.teleportAsync(island).thenAccept(success -> {

                                long elapsedTime = System.currentTimeMillis() - start;

                                if (elapsedTime > 3000) {
                                    player.sendMessage(TextUtil.message(MessageKeys.ISLAND_TELEPORT_LONG_WAIT));
                                } else if (elapsedTime > 1000) {
                                    player.sendMessage(TextUtil.message(MessageKeys.ISLAND_TELEPORT_SMALL_WAIT));
                                } else {
                                    player.sendMessage(TextUtil.message(MessageKeys.ISLAND_TELEPORT));
                                }

                            });

                        }, 10L);

                    }));
            }))
            .build(), aliasConfig.getStringList("createisland")
        );
    }

}
