package com.skyblockin.storage;

import com.skyblockin.VoidSurvival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinHandler implements Listener {

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {

        if (!VoidSurvival.getInstance().isAllowingJoins()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("The server is still loading! Try again later.", NamedTextColor.RED));
            return;
        }

        try {

            PlayerData data = Database.loadPlayer(event.getUniqueId());

            PlayerData.put(data);

        } catch (PlayerDataException exception) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Failed to load your data!", NamedTextColor.RED));
            VoidSurvival.logError("Failed to load data for " + event.getName(), exception);
        }

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {

        try {

            PlayerData data = PlayerData.of(event.getPlayer());

            Database.savePlayer(data);

        } catch (PlayerDataException exception) {
            VoidSurvival.logError("Encountered error while saving player data", exception);
        }

    }

}
