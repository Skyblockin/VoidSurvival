package com.skyblockin.voidsurvival.chat;

import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatDecorateEvent event) {

        Player player = event.player();

        if (player == null) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.isEmpty()) {
            return;
        }

        event.result(event.result().replaceText(config ->
            config.match("\\[item\\]")
            .replacement(item.effectiveName().hoverEvent(item.asHoverEvent()))
        ));
    }

}
