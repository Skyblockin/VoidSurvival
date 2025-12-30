package com.skyblockin.voidsurvival.entity;

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
            Bukkit.dispatchCommand(sender, "msg " + player.getName() + " Woops! That was a close one!");

            player.setFallDistance(0);
        }
    }

}
