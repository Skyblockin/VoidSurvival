package com.skyblockin.voidsurvival.social;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.Database;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class FriendManager {

    private final HashMap<UUID, HashSet<UUID>> pendingFriendRequests = new HashMap<>();

    public boolean testFriendship(Player first, Player second) {
        return PlayerData.of(first).friendList.contains(second);
    }

    public void createFriendRequest(Player sender, Player recipient) {

        PlayerData senderData = PlayerData.of(sender);

        HashSet<UUID> senderRequests = pendingFriendRequests.computeIfAbsent(sender.getUniqueId(), k -> new HashSet<>());
        HashSet<UUID> recipientRequests = pendingFriendRequests.computeIfAbsent(recipient.getUniqueId(), k -> new HashSet<>());

        if (senderRequests.contains(recipient.getUniqueId())) {
            sender.sendRichMessage("<#fc0202>You have already sent this person a friend request!");
            return;
        }

        // The map is sender -> recipients
        // When the other person runs the add command, the roles will be flipped
        if (recipientRequests.contains(sender.getUniqueId())) {
            acceptFriendRequest(recipient, sender);
            return;
        }

        if (!senderData.friendList.contains(recipient)) {
            senderRequests.add(recipient.getUniqueId());
            sender.sendMessage(TextUtil.color("<#05fcbe>Sent a friend request to <yellow>%s</yellow>! Wait for them to accept.", recipient.getName()));
            recipient.sendMessage(
                TextUtil.color("<#05fcbe>%s has sent you a friend request! Run <yellow><click:suggest_command:/friend add %s>/friend add %s</click></yellow> to accept!</green>",
                    sender.getName(), sender.getName(), sender.getName()
                )
            );
        } else {
            sender.sendRichMessage("<#fc0202>" + recipient.getName() + " is already on your friends list!");
        }

        // Some cleanup just because why not
        if (senderRequests.isEmpty()) {
            pendingFriendRequests.remove(sender.getUniqueId());
        }

        if (recipientRequests.isEmpty()) {
            pendingFriendRequests.remove(recipient.getUniqueId());
        }
    }

    public void removeFriend(Player sender, String name) {

        try {

            PlayerData senderData = PlayerData.of(sender);
            PlayerData removedData = PlayerData.getByNameorUuid(name);

            if (removedData == null) {
                sender.sendRichMessage("<#fc0202>Could not find a player with the name or uuid " + name + "!");
                return;
            }

            if (senderData.friendList.contains(removedData.asFriend())) {
                senderData.friendList.remove(removedData.asFriend());
                removedData.friendList.remove(senderData.asFriend());

                sender.sendRichMessage("<#05fcbe>Removed " + name + " from your friends list!");

                Player player = Bukkit.getPlayer(removedData.uuid);
                if (player != null) {
                    player.sendRichMessage("<#05fcbe>" + sender.getName() + " removed you from their friends list!");
                }

                try {
                    Database.savePlayers(List.of(senderData, removedData));
                } catch (Exception ex) {
                    VoidSurvival.logError("Encountered error while saving players after friend remove: ", ex);
                }

            } else {
                sender.sendRichMessage("<#fc0202>" + name + " is not on your friends list!");
            }

        } catch (Exception ex) {
            sender.sendRichMessage("<#fc0202>Could not find a player with the name or uuid " + name + "!");
        }

    }

    // Sender is the one who sent the friend request
    // Recipient is the one accepting the request
    public void acceptFriendRequest(Player sender, Player recipient) {

         HashSet<UUID> uuidSet = pendingFriendRequests.get(sender.getUniqueId());

         if (uuidSet.contains(recipient.getUniqueId())) {

             PlayerData senderData = PlayerData.of(sender);
             PlayerData recipientData = PlayerData.of(recipient);

             senderData.friendList.add(recipient);
             recipientData.friendList.add(sender);

             recipient.sendMessage(TextUtil.color("<#05fcbe>%s has been added to your friend list!", sender.getName()));
             sender.sendMessage(TextUtil.color("<#05fcbe>%s has accepted your friend request!", recipient.getName()));

             uuidSet.remove(recipient.getUniqueId());

             try {
                 Database.savePlayers(List.of(senderData, recipientData));
             } catch (Exception ex) {
                 VoidSurvival.logError("Encountered error while saving players after friend accept: ", ex);
             }

             if (uuidSet.isEmpty()) {
                 pendingFriendRequests.remove(sender.getUniqueId());
             }
         }

    }
}
