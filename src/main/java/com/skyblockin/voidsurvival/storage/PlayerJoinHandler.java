package com.skyblockin.voidsurvival.storage;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.world.WorldGuardUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        // Exclude ops of course lol
        if (player.isOp()) {
            return;
        }

        var worldGuardLocation = WorldGuardUtil.getFlagValueAt(player.getLocation(), Flags.TELE_LOC);

        if (worldGuardLocation != null) {

            Location location = BukkitAdapter.adapt(worldGuardLocation);

            if (player.getLocation().distanceSquared(location) >= 25) {
                player.teleportAsync(location);
            }
        }

    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {

        if (!VoidSurvival.getInstance().isAllowingJoins()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("The server is still loading! Try again later.", NamedTextColor.RED));
            return;
        }

        try {

            PlayerData data = Database.loadPlayer(event.getUniqueId());

            data.lastKnownUserName = event.getPlayerProfile().getName();

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
