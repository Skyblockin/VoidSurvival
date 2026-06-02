package com.skyblockin.voidsurvival.command.admin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.loot.LootTable;
import com.skyblockin.voidsurvival.loot.LootUtil;
import com.skyblockin.voidsurvival.nms.NMSUtil;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.Island;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.CommandUtil;
import com.skyblockin.voidsurvival.util.Functions;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class Debug {

    public static final LiteralCommandNode<CommandSourceStack> COMMAND = literal("debug")
        .requires(ctx -> ctx.getSender().isOp())
        .then(literal("getnearbyislands")
            .then(argument("range", IntegerArgumentType.integer(0))
                .then(argument("limit", IntegerArgumentType.integer(1))
                    .executes(CommandUtil.playerCommand((ctx, player) -> {

                        int chunkX = player.getChunk().getX();
                        int chunkZ = player.getChunk().getZ();
                        int range = ctx.getArgument("range", Integer.class);
                        int limit = ctx.getArgument("limit", Integer.class);

                        List<Island> islands = Database.getIslandsInRange(chunkX, chunkZ, range, limit);

                        int i = 1;

                        for (Island island : islands) {

                            int x = island.x();
                            int z = island.z();
                            int chunkDistance = (int) Math.sqrt(Math.pow(x - chunkX, 2) + Math.pow(z - chunkZ, 2));

                            player.sendMessage(TextUtil.color(
                                "%d. island (id: %d) at <click:run_command:/tp %d 64 %d>%d, %d</click> (%d chunks away)",
                                i, island.id(), x * 16 + 8, z * 16 + 8, x, z, chunkDistance
                            ));
                            i++;
                        }

                    }))
                )
            )
        )
        .then(literal("mannequin")
            .then(argument("player", StringArgumentType.word())
                .then(argument("pos1", ArgumentTypes.blockPosition())
                    .then(argument("pos2", ArgumentTypes.blockPosition())
                        .then(argument("speed", FloatArgumentType.floatArg())
                            .executes(ctx -> {

                                if (!(ctx.getSource().getExecutor() instanceof Entity entity)) {
                                    return 1;
                                }

                                Location location = entity.getLocation();
                                String name = ctx.getArgument("player", String.class);
                                float velocity = ctx.getArgument("speed", Float.class);
                                BlockPosition pos1 = ctx.getArgument("pos1", BlockPositionResolver.class).resolve(ctx.getSource());
                                BlockPosition pos2 = ctx.getArgument("pos2", BlockPositionResolver.class).resolve(ctx.getSource());

                                PlayerProfile profile = Bukkit.createProfile(name);

                                CompletableFuture.runAsync(profile::complete).thenAccept(v -> {
                                    VoidSurvival.getInstance().runTask(() -> {
                                        Mannequin mannequin = location.getWorld().spawn(location, Mannequin.class, mob -> {
                                            mob.setProfile(ResolvableProfile.resolvableProfile(profile));
                                        });

                                        new BukkitRunnable() {

                                            final Location start = pos1.toLocation(mannequin.getWorld());
                                            final Location end = pos2.toLocation(mannequin.getWorld());
                                            final Vector movement = end.clone().subtract(start).toVector().normalize().multiply(velocity);
                                            double distance = start.distance(end);
                                            final double step = movement.length();

                                            @Override
                                            public void run() {
                                                NMSUtil.moveTo(mannequin, start);
                                                for (Entity entity : mannequin.getNearbyEntities(3, 3, 3)) {
                                                    if (!entity.equals(mannequin) && entity instanceof Player player) {
                                                        mannequin.lookAt(player.getEyeLocation(), LookAnchor.EYES);
                                                        break;
                                                    }
                                                }
                                                start.add(movement);
                                                distance -= step;
                                                if (distance <= 0) {
                                                    cancel();
                                                }
                                            }
                                        }.runTaskTimer(VoidSurvival.getInstance(), 1L, 1L);

                                    });
                                });

                                return 1;
                            })
                        )
                    )
                )
            )
        )
        .then(literal("testloot")
            .then(argument("table", StringArgumentType.word())
                .suggests(CommandUtil.suggest(() -> VoidSurvival.getInstance().getLootTableManager().getTableIds()))
                .then(argument("loot_bonus", IntegerArgumentType.integer(0))
                    .executes(ctx -> {

                        if (ctx.getSource().getSender() instanceof Player player) {

                            String tableName = ctx.getArgument("table", String.class);
                            int lootBonus = ctx.getArgument("loot_bonus", Integer.class);

                            LootTable table = VoidSurvival.getInstance().getLootTableManager().getTable(tableName);

                            if (table != null) {
                                LootUtil.openInventory(player, table, Functions.tableIdToName(tableName), lootBonus);
                            }

                        }

                        return 1;
                    })
                )
            )
        )
        .then(literal("resetchestcooldowns")
            .then(argument("player", ArgumentTypes.player())
                .executes(ctx -> {

                    PlayerSelectorArgumentResolver target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                    Player player = target.resolve(ctx.getSource()).getFirst();
                    PlayerData.of(player).resetCooldowns();

                    ctx.getSource().getSender().sendRichMessage("<green>Reset chest cooldowns of " + player.getName());

                    return 1;
                })
            )
            .build()
        )
        .then(literal("toggle")
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
        )
        .then(literal("dumpplayer")
            .then(argument("name", StringArgumentType.word())
                .executes(ctx -> {

                    String name = ctx.getArgument("name", String.class);

                    try {

                        PlayerData data = PlayerData.getByNameorUuid(name);

                        if (data == null) {
                            ctx.getSource().getSender().sendRichMessage("<red>No player with name or uuid '" + name + "' found!");
                            return 1;
                        }

                        try {
                            ctx.getSource().getSender().sendRichMessage("<green>Data dump for player '" + name + "':");
                            ctx.getSource().getSender().sendMessage(Component.text(Json.toPrettyJsonString(data)));
                        } catch (Exception ex) {
                            VoidSurvival.logError("Failed to dump player data for '" + name + "'", ex);
                        }

                    } catch (Exception ex) {
                        VoidSurvival.logError("Failed to get player data for '" + name + "'", ex);
                    }

                    return 1;
                })
            )
        )
        .then(literal("resetislandgenerationstatus")
            .then(argument("name", StringArgumentType.word())
                .executes(ctx -> {

                    String name = ctx.getArgument("name", String.class);

                    Player player = Bukkit.getPlayer(name);

                    if (player == null) {
                        ctx.getSource().getSender().sendRichMessage("<red>Player '" + name + "' not found!");
                        return 1;
                    }

                    PlayerData.of(player).hasGeneratedIsland = false;
                    ctx.getSource().getSender().sendRichMessage("<green>Reset island generation status of player '" + name + "'!");

                    return 1;
                })
            )
        ).build();



}
