package com.skyblockin.voidsurvival.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.InvalidHomeException;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.world.IslandGenerator;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class BasicCommands {

    public static void register(Commands commands) {

        commands.register(literal("home")
                .then(argument("name", StringArgumentType.word())
                    .requires(ctx -> ctx.getSender().hasPermission("voidsurvival.home.multiple"))
                    .suggests((ctx, builder) -> {

                        if (ctx.getSource() instanceof Player player) {
                            for (String homeName : PlayerData.of(player).getHomeNames()) {
                                builder.suggest(homeName);
                            }
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {

                        if (ctx.getSource().getSender() instanceof Player player) {

                            String homeName = ctx.getArgument("name", String.class);

                            try {

                                Location home = PlayerData.of(player).getHome(homeName);

                                if (home != null) {
                                    player.teleportAsync(home).thenAccept(success -> {
                                        player.sendRichMessage("<green>You have been teleported to your home.");
                                    });
                                } else {
                                    player.sendRichMessage("<red>Could not find a home with the name '" + homeName + "'. You can set it by running <dark_red>/sethome " + homeName);
                                }

                            } catch (InvalidHomeException e) {

                                Location infirmary = VoidSurvival.getInstance().getInfirmaryLocation();

                                if (infirmary != null) {
                                    player.teleportAsync(infirmary).thenAccept(success -> {
                                        player.sendRichMessage("<red>Your home location is no longer supported by solid blocks or became obstructed. You have been teleported to safety.");
                                    });
                                } else {
                                    player.performCommand("spawn");
                                    player.sendRichMessage("<red>Your home location is no longer supported by solid blocks or became obstructed. You have been teleported to spawn.");
                                }
                            }

                        }

                        return 1;
                    })
                )
            .requires(ctx -> ctx.getSender().hasPermission("voidsurvival.home"))
            .executes(ctx -> {

                if (ctx.getSource().getSender() instanceof Player player) {

                    try {

                        Location home = PlayerData.of(player).getHome("island");

                        if (home != null) {
                            player.teleportAsync(home).thenAccept(success -> {
                                player.sendRichMessage("<green>You have been teleported to your home.");
                            });
                        } else {
                            player.sendRichMessage("<red>Your default home has not been set. You can set it by running <dark_red>/sethome");
                        }

                    } catch (InvalidHomeException e) {

                        Location infirmary = VoidSurvival.getInstance().getInfirmaryLocation();

                        if (infirmary != null) {
                            player.teleportAsync(infirmary).thenAccept(success -> {
                                player.sendRichMessage("<red>Your home location is no longer supported by solid blocks or became obstructed. You have been teleported to safety.");
                            });
                        } else {
                            player.performCommand("spawn");
                            player.sendRichMessage("<red>Your home location is no longer supported by solid blocks or became obstructed. You have been teleported to spawn.");
                        }
                    }

                }

                return 1;
            })
            .build()
        );

        commands.register(literal("sethome")
            .then(argument("name", StringArgumentType.word())
                .requires(ctx -> ctx.getSender().isOp())
                .executes(ctx -> {

                    if (ctx.getSource().getSender() instanceof Player player) {

                        String name = ctx.getArgument("name", String.class);

                        PlayerData.of(player).setHome(name, player.getLocation());
                    }

                    return 1;
                })
            )
            .executes(ctx -> {

                if (ctx.getSource().getSender() instanceof Player player) {
                    PlayerData.of(player).setHome("island", player.getLocation());
                    player.sendRichMessage("<green>Set your home at your current location.");
                }

                return 1;
            })
            .build()
        );

        commands.register(literal("createisland")
            .requires(ctx -> ctx.getSender().hasPermission("voidsurvival.createisland"))
            .executes(ctx -> {

                if (ctx.getSource().getSender() instanceof Player player) {

                    PlayerData data = PlayerData.of(player);

                    if (data.hasGeneratedIsland) {
                        player.sendRichMessage("<red>You made an island already. " +
                            "If you somehow lost it and you're screwed, say something in the Discord server, " +
                            "maybe an admin will feel nice and give you a helping hand! " +
                            "If you did not lose it, simply try running <dark_red>/home</dark_red>!"
                        );
                        return 1;
                    }

                    if (data.generatingIsland) {
                        player.sendRichMessage("<red>Hey chill, we're already generating an island for you!");
                        return 1;
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

                            VoidSurvival.getInstance().runTaskLater(() -> {

                                data.setHome("island", island);
                                data.generatingIsland = false;

                                player.teleportAsync(island).thenAccept(success -> {

                                    long elapsedTime = System.currentTimeMillis() - start;

                                    if (elapsedTime > 3000) {
                                        player.sendRichMessage("<green>Woah! That took a while, sorry about that. You have been teleported to your island.");
                                    } else if (elapsedTime > 1000) {
                                        player.sendRichMessage("<green>Sorry about the wait. You have been teleported to your island.");
                                    } else {
                                        player.sendRichMessage("<green>You have been teleported to your island.");
                                    }

                                    data.hasGeneratedIsland = true;
                                });

                            }, 10L);

                        }));
                }

                return 1;
            })
            .build()
        );
    }

}
