package com.skyblockin.voidsurvival.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.loot.LootTable;
import com.skyblockin.voidsurvival.loot.LootChestManager;
import com.skyblockin.voidsurvival.loot.LootUtil;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.PlayerData;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class AdminCommands {

    private static CompletableFuture<Suggestions> build(SuggestionsBuilder builder, Collection<String> suggestions) {
        suggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static void register(Commands commands) {

        commands.register(literal("vsreload")
            .requires(ctx -> ctx.getSender().isOp())
            .executes(ctx -> {

                VoidSurvival.getInstance().reload();

                ctx.getSource().getSender().sendRichMessage("<green>VoidSurvival reloaded!");

                return 1;
            })
            .build()
        );

        commands.register(literal("vsdebug")
            .requires(ctx -> ctx.getSender().isOp())
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
            .build()
        );

        commands.register(literal("resetchestcooldowns")
            .requires(ctx -> ctx.getSender().isOp())
            .then(argument("player", ArgumentTypes.player())
                .executes(ctx -> {

                    Player target = ctx.getArgument("player", Player.class);

                    PlayerData.of(target).resetCooldowns();

                    ctx.getSource().getSender().sendRichMessage("<green>Reset chest cooldowns of " + target.getName());

                    return 1;
                })
            )
            .build()
        );

        commands.register(literal("savechestloot")
            .requires(ctx -> ctx.getSender().isOp())
            .executes(ctx -> {

                Database.saveChestLocations(VoidSurvival.getInstance().getLootTableManager().getChestLootTables());
                ctx.getSource().getSender().sendRichMessage("<green>Saved chest loot tables.");

                return 1;
            })
            .build()
        );

        commands.register(literal("removechestloot")
            .requires(ctx -> ctx.getSender().isOp())
            .executes(ctx -> {

                if (ctx.getSource().getSender() instanceof Player player) {

                    LootChestManager manager = VoidSurvival.getInstance().getLootTableManager();

                    Block block = player.getTargetBlock(null, 10);

                    if (manager.deleteTable(block.getLocation())) {
                        player.sendRichMessage("<green>Removed loot table from chest at " + block.getX() + ", " + block.getY() + ", " + block.getZ());
                    } else {
                        player.sendRichMessage("<red>No loot table found at " + block.getX() + ", " + block.getY() + ", " + block.getZ());
                    }

                }

                return 1;

            })
            .build()
        );

        commands.register(literal("setchestloot")
            .requires(ctx -> ctx.getSender().isOp())
            .then(argument("table", StringArgumentType.word())
                .suggests((context, builder) ->
                    build(builder, VoidSurvival.getInstance().getLootTableManager().getTableIds())
                )
                .executes(ctx -> {

                    if (ctx.getSource().getSender() instanceof Player player) {

                        String tableName = ctx.getArgument("table", String.class);

                        LootChestManager manager = VoidSurvival.getInstance().getLootTableManager();
                        LootTable table = manager.getTable(tableName);

                        if (table != null) {

                            Block block = player.getTargetBlock(null, 10);

                            if (block.getBlockData() instanceof Chest) {
                                manager.setChestLoot(block.getLocation(), tableName);
                                player.sendRichMessage(String.format("<green>Set loot table of chest at %d %d %d to '%s'!",
                                    block.getX(), block.getY(), block.getZ(), tableName
                                ));
                            } else {
                                player.sendRichMessage("<red>That's not a chest, you must be looking at a chest!");
                            }

                        } else {
                            player.sendRichMessage("<red>'" + tableName + "' is not a valid loot table!");
                        }

                    }

                    return 1;
                })
            )
            .build()
        );

        commands.register(literal("testloot")
            .requires(ctx -> ctx.getSender().isOp())
            .then(argument("table", StringArgumentType.word())
                .suggests((context, builder) -> {

                    VoidSurvival.getInstance().getLootTableManager().getTableIds().forEach(builder::suggest);

                    return builder.buildFuture();
                })
                .executes(ctx -> {

                    if (ctx.getSource().getSender() instanceof Player player) {

                        String tableName = ctx.getArgument("table", String.class);

                        LootTable table = VoidSurvival.getInstance().getLootTableManager().getTable(tableName);

                        if (table != null) {
                            LootUtil.openInventory(player, table, 0);
                        }

                    }

                    return 1;
                })
            )
            .build()
        );
    }

}
