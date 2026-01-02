package com.skyblockin.voidsurvival.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.message.MessageKeys;
import com.skyblockin.voidsurvival.social.Friend;
import com.skyblockin.voidsurvival.social.PaginatedList;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.storage.PlayerDataException;
import com.skyblockin.voidsurvival.util.CommandUtil;
import com.skyblockin.voidsurvival.util.PlayerUtil;
import com.skyblockin.voidsurvival.util.TextUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class FriendCommand {

    public static final LiteralCommandNode<CommandSourceStack> COMMAND = literal("friend")
        .then(literal("add")
            .then(argument("player", StringArgumentType.word())
                .executes(CommandUtil.playerCommand((ctx, player) -> {

                    String name = ctx.getArgument("player", String.class);

                    if (name.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(TextUtil.message(MessageKeys.FRIEND_ADD_SELF));
                        return;
                    }

                    Player other = PlayerUtil.getOnlinePlayer(name);

                    if (other != null) {
                        VoidSurvival.getInstance().getFriendManager().createFriendRequest(player, other);
                    } else {
                        player.sendMessage(TextUtil.message(MessageKeys.PLAYER_NOT_FOUND, name));
                    }
                }))
            )
        )
        .then(literal("remove")
            .then(argument("player", StringArgumentType.word())
                .suggests(CommandUtil.suggestToPlayerSender(player -> PlayerData.of(player).getFriends().getNames()))
                .executes(CommandUtil.playerCommand((ctx, player) -> {

                    String name = ctx.getArgument("player", String.class);

                    if (name.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(TextUtil.message(MessageKeys.FRIEND_REMOVE_SELF));
                        return;
                    }

                    VoidSurvival.getInstance().getFriendManager().removeFriend(player, name);
                }))
            )
        )
        .then(literal("mutuals")
            .then(argument("player", StringArgumentType.word())
                .then(argument("page", IntegerArgumentType.integer(1))
                    .executes(CommandUtil.playerCommand((ctx, player) -> {

                        String name = ctx.getArgument("player", String.class);
                        int page = ctx.getArgument("page", Integer.class);

                        if (name.equalsIgnoreCase(player.getName())) {
                            player.sendMessage(TextUtil.message(MessageKeys.FRIEND_MUTUALS_SELF));
                            return;
                        }

                        handleMutualFriends(player, name, page);
                    }))
                )
                .executes(CommandUtil.playerCommand((ctx, player) -> {
                    String name = ctx.getArgument("player", String.class);
                    handleMutualFriends(player, name, 1);
                }))
            )
        )
        .then(literal("list")
            .then(argument("page", IntegerArgumentType.integer(1))
                .executes(CommandUtil.playerCommand((ctx, player) -> {
                    int page = ctx.getArgument("page", Integer.class);
                    player.sendMessage(renderFriendListPage(MessageKeys.FRIEND_LIST_HEADER, MessageKeys.FRIEND_LIST_ENTRY,
                        new PaginatedList<>(PlayerData.of(player).getFriends()), page)
                    );
                }))
            )
            .executes(CommandUtil.playerCommand((ctx, player) -> {
                player.sendMessage(renderFriendListPage(MessageKeys.FRIEND_LIST_HEADER, MessageKeys.FRIEND_LIST_ENTRY,
                    new PaginatedList<>(PlayerData.of(player).getFriends()), 1)
                );
            }))
        )
        .executes(ctx -> {
            ctx.getSource().getSender().sendMessage(TextUtil.message(MessageKeys.FRIEND_FEEDBACK));
            return 1;
        })
        .build();

    private static void handleMutualFriends(Player sender, String name, int page) {

        try {

            PlayerData data = PlayerData.of(sender);
            PlayerData other = PlayerData.getByNameorUuid(name);

            ArrayList<Friend> mutuals = data.getMutualFriends(other);

            if (mutuals.isEmpty()) {
                sender.sendMessage(TextUtil.message(MessageKeys.FRIEND_MUTUALS_NO_MUTUALS));
                return;
            }

            PaginatedList<Friend> list = new PaginatedList<>(mutuals, 10);
            sender.sendMessage(renderFriendListPage(MessageKeys.FRIEND_MUTUALS_FRIEND_LIST_HEADER, MessageKeys.FRIEND_MUTUALS_FRIEND_LIST_ENTRY, list, page));

        } catch (PlayerDataException e) {
            sender.sendMessage(TextUtil.message(MessageKeys.PLAYER_NOT_FOUND, name));
        }

    }

    private static Component renderFriendListPage(String headerMessageKey, String entryMessageKey, PaginatedList<Friend> list, int page) {

        if (list.isEmpty()) {
            return TextUtil.message(MessageKeys.FRIEND_LIST_NO_FRIENDS);
        }

        Component header = TextUtil.message(headerMessageKey, Math.clamp(page, 1, list.getPageCount()), list.getPageCount());

        for (Friend friend : list.getPage(page)) {
            header = header.appendNewline()
                .append(TextUtil.message(entryMessageKey, friend.name));
        }

        return header;
    }
}
