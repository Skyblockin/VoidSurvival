package com.skyblockin.voidsurvival.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CommandUtil {

    public static Command<CommandSourceStack> executorCommand(BiConsumer<CommandContext<CommandSourceStack>, Entity> consumer) {
        return ctx -> {
            if (ctx.getSource().getExecutor() != null) {
                consumer.accept(ctx, ctx.getSource().getExecutor());
            } else {
                ctx.getSource().getSender().sendRichMessage("<red>The command executor cannot be null!");
            }
            return 1;
        };
    }

    public static Command<CommandSourceStack> playerCommand(BiConsumer<CommandContext<CommandSourceStack>, Player> consumer) {
        return ctx -> {
            if (ctx.getSource().getSender() instanceof Player player) {
                consumer.accept(ctx, player);
            } else {
                ctx.getSource().getSender().sendRichMessage("<red>This command can only be executed by players!");
            }
            return 1;
        };
    }

    public static SuggestionProvider<CommandSourceStack> suggestToPlayerSender(Function<Player, Collection<String>> suggestions) {
        return (ctx, builder) -> {
            if (ctx.getSource().getSender() instanceof Player player) {
                suggestions.apply(player).forEach(builder::suggest);
            }
            return builder.buildFuture();
        };
    }

    public static SuggestionProvider<CommandSourceStack> suggest(Function<CommandContext<CommandSourceStack>, Collection<String>> suggestions) {
        return (ctx, builder) -> {
            suggestions.apply(ctx).forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    public static SuggestionProvider<CommandSourceStack> suggest(Supplier<Collection<String>> suggestions) {
        return (ctx, builder) -> {
            suggestions.get().forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

}
