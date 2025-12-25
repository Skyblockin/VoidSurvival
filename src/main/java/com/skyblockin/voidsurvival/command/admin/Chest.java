package com.skyblockin.voidsurvival.command.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.loot.LootChestManager;
import com.skyblockin.voidsurvival.loot.LootTable;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.util.Functions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class Chest {

    public static final LiteralCommandNode<CommandSourceStack> COMMAND = literal("chest")
        .requires(ctx -> ctx.getSender().isOp())
        .then(literal("setloot")
            .then(argument("table", StringArgumentType.word())
                .suggests(Functions.suggest(() -> VoidSurvival.getInstance().getLootTableManager().getTableIds()))
                .executes(ctx -> {

                    if (ctx.getSource().getSender() instanceof Player player) {

                        String tableName = ctx.getArgument("table", String.class);

                        LootChestManager manager = VoidSurvival.getInstance().getLootTableManager();
                        LootTable table = manager.getTable(tableName);

                        if (table != null) {

                            Block block = player.getTargetBlock(null, 10);

                            if (block.getState() instanceof org.bukkit.block.Chest) {
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
        )
        .then(literal("removeloot")
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
        )
        .then(literal("saveloot")
            .executes(ctx -> {

                Database.saveChestLocations(VoidSurvival.getInstance().getLootTableManager().getChestLootTables());
                ctx.getSource().getSender().sendRichMessage("<green>Saved chest loot tables.");

                return 1;
            })
        )
        .build();

}
