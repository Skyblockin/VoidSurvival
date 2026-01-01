package com.skyblockin.voidsurvival.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.gui.Gui;
import com.skyblockin.voidsurvival.leaderboard.LeaderboardType;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.storage.Position;
import com.skyblockin.voidsurvival.util.CommandUtil;
import com.skyblockin.voidsurvival.util.DialogUtil;
import com.skyblockin.voidsurvival.util.PlayerUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import com.skyblockin.voidsurvival.world.IslandGenerator;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Campfire;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;

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
                .append(TextUtil.color("<!b>%s%d. %s: %d",
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

        return renderLeaderboard(Component.text("Top " + type.getDisplayName(), NamedTextColor.GOLD, TextDecoration.BOLD), type, leaderboard);
    }

    private static void handlePlayerHome(Player player, String homeName) {

        PlayerData data = PlayerData.of(player);
        Location home = data.getHome(homeName);

        if (home == null) {
            if (homeName.equals("island")) {
                player.sendRichMessage("<red>Your default home has not been set. You can set it by running <dark_red>/sethome");
            } else {
                player.sendRichMessage("<red>Could not find a home with the name '" + homeName + "'. You can set it by running <dark_red>/sethome " + homeName);
            }
            return;
        }

        double eyeHeight = player.getEyeHeight();

        // Check for suffocation
        if (home.getWorld().getBlockAt(home.getBlockX(), (int) (home.getY() + eyeHeight), home.getBlockZ()).isSuffocating()) {
            player.sendRichMessage("<red>Your home location is no longer supported by solid blocks or became obstructed.");
            return;
        }

        Block block = home.getBlock();

        if (block.isSolid()) {
            if (block.getRelative(BlockFace.DOWN).isSolid()) {
                player.teleportAsync(home).thenAccept(success -> {
                    player.sendRichMessage("<green>You have been teleported to your home.");
                });
            } else {
                player.teleportAsync(home.add(0, 1, 0)).thenAccept(success -> {
                    player.sendRichMessage("<green>You have been teleported to your home.");
                });
            }
        } else {

            while (!block.isSolid()) {
                block = block.getRelative(BlockFace.DOWN);
            }

            if (block.getY() == block.getWorld().getMinHeight()) {
                player.sendRichMessage("<red>Your home location is no longer supported by solid blocks or became obstructed.");
                return;
            }

            player.teleportAsync(home).thenAccept(success -> {
                player.sendRichMessage("<green>You have been teleported to your home.");
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
                            "Are you sure you want to set your home here?"
                        ))
                        .build()
                    )
                    .type(DialogUtil.buildMultiAction(
                        List.of(
                            DialogUtil.buildActionButton("Yes", 100, audience -> {
                                PlayerData.of(player).setHome("island", player.getLocation());
                                player.sendRichMessage("<green>Set your home at your current location.");
                            }),
                            DialogUtil.buildActionButton("No", 100, audience -> {
                                audience.sendMessage(TextUtil.color("<green>Your home was not set."));
                            })
                        ),
                        DialogUtil.buildActionButton("Close", 100, audience -> {
                            audience.sendMessage(TextUtil.color("<green>Your home was not set."));
                        }),
                        2
                    ))
                );

                player.showDialog(dialog);
            }))
            .build(), aliasConfig.getStringList("sethome")
        );

        commands.register(literal("createisland")
            .requires(ctx -> ctx.getSender().hasPermission("voidsurvival.createisland"))
            .executes(CommandUtil.playerCommand((ctx, player) -> {

                PlayerData data = PlayerData.of(player);

                if (data.hasGeneratedIsland) {
                    player.sendRichMessage("<red>You made an island already. " +
                        "If you somehow lost it and you're screwed, say something in the Discord server, " +
                        "maybe an admin will feel nice and give you a helping hand! " +
                        "If you did not lose it, simply try running <dark_red>/home</dark_red>!"
                    );
                    return;
                }

                if (data.generatingIsland) {
                    player.sendRichMessage("<red>Hey chill, we're already generating an island for you!");
                    return;
                }

                data.generatingIsland = true;

                final long start = System.currentTimeMillis();

                IslandGenerator generator = VoidSurvival.getInstance()
                    .getIslandGenerator();

                player.sendRichMessage("<green>Generating your island, please wait...");

                generator.findChunkForIsland(player.getWorld())
                    .thenAccept(chunk -> VoidSurvival.getInstance().runTask(() -> {

                        Location island = generator.generateIsland(chunk);

                        Database.saveIsland(island.getChunk().getX(), island.getChunk().getZ());
                        data.setHome("island", island.toCenterLocation());
                        data.hasGeneratedIsland = true;
                        data.generatingIsland = false;

                        VoidSurvival.getInstance().runTaskLater(() -> {

                            player.teleportAsync(island).thenAccept(success -> {

                                long elapsedTime = System.currentTimeMillis() - start;

                                if (elapsedTime > 3000) {
                                    player.sendRichMessage("<green>Woah! That took a while, sorry about that. You have been teleported to your island.");
                                } else if (elapsedTime > 1000) {
                                    player.sendRichMessage("<green>Sorry about the wait. You have been teleported to your island.");
                                } else {
                                    player.sendRichMessage("<green>You have been teleported to your island.");
                                }

                            });

                        }, 10L);

                    }));
            }))
            .build(), aliasConfig.getStringList("createisland")
        );
    }

}
