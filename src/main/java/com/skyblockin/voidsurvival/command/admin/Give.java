package com.skyblockin.voidsurvival.command.admin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.ItemData;
import com.skyblockin.voidsurvival.util.Functions;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class Give {

      public static final LiteralCommandNode<CommandSourceStack> COMMAND = literal("give")
          .requires(ctx -> ctx.getSender().isOp())
          .then(argument("player", ArgumentTypes.players())
              .then(argument("item", StringArgumentType.word())
                  .suggests(Functions.suggest(() -> VoidSurvival.getInstance().getItemManager().getIds()))
                  .then(argument("amount", IntegerArgumentType.integer(1))
                      .executes(ctx -> {

                          CommandSender sender = ctx.getSource().getSender();
                          String itemId = ctx.getArgument("item", String.class);
                          List<Player> players = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
                          int amount = ctx.getArgument("amount", Integer.class);

                          return handleCommand(sender, itemId, players, amount);
                      })
                  )
                  .executes(ctx -> {

                      CommandSender sender = ctx.getSource().getSender();
                      String itemId = ctx.getArgument("item", String.class);
                      List<Player> players = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());

                      return handleCommand(sender, itemId, players, 1);
                  })
              )
          ).build();

      private static int handleCommand(CommandSender sender, String itemId, List<Player> players, int amount) {

          ItemData data = VoidSurvival.getInstance().getItemManager().getItem(itemId);

          if (data != null) {

              ItemStack item = data.createItem();

              for (Player player : players) {
                  player.getInventory().addItem(item);
              }

              Component itemComponent = item.effectiveName()
                  .append(amount > 1 ? TextUtil.color(" <aqua>x %d", amount) : Component.empty())
                  .hoverEvent(item.asHoverEvent());

              if (players.size() > 5) {
                  sender.sendMessage(TextUtil.color("<green>Gave the players</green> ").append(
                      itemComponent)
                  );
              } else {
                  sender.sendMessage(TextUtil.color("<green>Gave %s</green> ",
                      TextUtil.buildNaturalList(player -> "<yellow>" + player.getName() + "</yellow>", players)
                  ).append(itemComponent));
              }

          } else {
              sender.sendRichMessage("<red>No item with id '" + itemId + "' found!");
          }

          return 1;
      }
}
