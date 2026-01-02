package com.skyblockin.voidsurvival.entity;

import com.skyblockin.voidsurvival.message.MessageKeys;
import com.skyblockin.voidsurvival.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        if (event.getTo().getY() <= player.getWorld().getMinHeight()) {

            ConsoleCommandSender sender = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(sender, "spawn " + player.getName());
            player.sendMessage(TextUtil.message(MessageKeys.CLOSE_ONE));

            player.setFallDistance(0);
        }
    }

}
