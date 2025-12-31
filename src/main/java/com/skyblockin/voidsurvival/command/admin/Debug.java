package com.skyblockin.voidsurvival.command.admin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.loot.LootTable;
import com.skyblockin.voidsurvival.loot.LootUtil;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.CommandUtil;
import com.skyblockin.voidsurvival.util.Functions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class Debug {

    public static final LiteralCommandNode<CommandSourceStack> COMMAND = literal("debug")
        .requires(ctx -> ctx.getSender().isOp())
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
