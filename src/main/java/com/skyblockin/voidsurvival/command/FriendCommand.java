package com.skyblockin.voidsurvival.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.social.Friend;
import com.skyblockin.voidsurvival.social.PaginatedList;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.storage.PlayerDataException;
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
                .executes(ctx -> {

                    String name = ctx.getArgument("player", String.class);

                    if (ctx.getSource().getSender() instanceof Player player) {

                        if (name.equalsIgnoreCase(player.getName())) {
                            player.sendRichMessage("<#fc0202>That's wholesome, but there's no point in adding yourself to your own friend list!");
                            return 1;
                        }

                        Player other = PlayerUtil.getOnlinePlayer(name);

                        if (other != null) {
                            VoidSurvival.getInstance().getFriendManager().createFriendRequest(player, other);
                        } else {
                            player.sendRichMessage("<#fc0202>No player with name " + name + " found, are they online?");
                        }

                    }

                    return 1;
                })
            )
        )
        .then(literal("remove")
            .then(argument("player", StringArgumentType.word())
                .executes(ctx -> {

                    String name = ctx.getArgument("player", String.class);

                    if (ctx.getSource().getSender() instanceof Player player) {

                        if (name.equalsIgnoreCase(player.getName())) {
                            player.sendRichMessage("<#fc0202>Why would you do that?");
                            return 1;
                        }

                        VoidSurvival.getInstance().getFriendManager().removeFriend(player, name);
                    }

                    return 1;
                })
            )
        )
        .then(literal("mutuals")
            .then(argument("player", StringArgumentType.word())
                .then(argument("page", IntegerArgumentType.integer(1))
                    .executes(ctx -> {

                        String name = ctx.getArgument("player", String.class);
                        int page = ctx.getArgument("page", Integer.class);

                        if (ctx.getSource().getSender() instanceof Player player) {

                            if (name.equalsIgnoreCase(player.getName())) {
                                player.sendRichMessage("<#fc0202>I agree, you are friends with yourself, try running <yellow><click:suggest_command:/friendlist>/friend list</click></yellow> though");
                                return 1;
                            }

                            handleMutualFriends(player, name, page);
                        }

                        return 1;
                    })
                )
                .executes(ctx -> {

                    String name = ctx.getArgument("player", String.class);

                    if (ctx.getSource().getSender() instanceof Player player) {
                        handleMutualFriends(player, name, 1);
                    }

                    return 1;
                })
            )
        )
        .then(literal("list")
            .then(argument("page", IntegerArgumentType.integer(1))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        int page = ctx.getArgument("page", Integer.class);
                        player.sendMessage(renderFriendListPage("<#05fcbe>Friends (page %d of %d)",
                            PlayerData.of(player).friendList.asPaginatedList(10), page)
                        );
                    }
                    return 1;
                })
            )
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    player.sendMessage(renderFriendListPage("<#05fcbe>Friends (page %d of %d)",
                        PlayerData.of(player).friendList.asPaginatedList(10), 1)
                    );
                }
                return 1;
            })
        )
        .build();

    private static void handleMutualFriends(Player sender, String name, int page) {

        try {

            PlayerData data = PlayerData.of(sender);
            PlayerData other = PlayerData.getByNameorUuid(name);

            ArrayList<Friend> mutuals = data.getMutualFriends(other);

            if (mutuals.isEmpty()) {
                sender.sendRichMessage("<#fc0202>You have no mutual friends with " + name + "!");
                return;
            }

            PaginatedList<Friend> list = new PaginatedList<>(mutuals, 10);
            sender.sendMessage(renderFriendListPage("<#05fcbe>Mutual friends with " + name + " (page %d of %d)", list, page));

        } catch (PlayerDataException e) {
            sender.sendRichMessage("<#fc0202>No player with name or uuid " + name + " found!");
        }

    }

    private static Component renderFriendListPage(String title, PaginatedList<Friend> list, int page) {

        if (list.isEmpty()) {
            return TextUtil.color("<#fc0202>You don't have any friends! Maybe make some?");
        }

        Component header = TextUtil.color(title, Math.clamp(page, 1, list.getPageCount()), list.getPageCount());

        for (Friend friend : list.getPage(page)) {
            header = header.appendNewline()
                .append(TextUtil.color("  <#05fcbe>%s", friend.name));
        }

        return header;
    }
}
