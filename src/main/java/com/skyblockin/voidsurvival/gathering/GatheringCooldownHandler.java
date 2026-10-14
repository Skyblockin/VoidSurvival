package com.skyblockin.voidsurvival.gathering;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class GatheringCooldownHandler {

    private final HashMap<UUID, Pair<Long, UUID>> cooldowns = new HashMap<>();
    private final BukkitTask task;
    private final GatheringNode node;

    public GatheringCooldownHandler(GatheringNode node) {
        this.node = node;
        this.task = new BukkitRunnable() {
            @Override
            public void run() {

                Iterator<Map.Entry<UUID, Pair<Long, UUID>>> iterator = cooldowns.entrySet().iterator();

                while (iterator.hasNext()) {

                    var entry = iterator.next();
                    var value = entry.getValue();
                    value.left -= 20;

                    Entity display = Bukkit.getEntity(value.right);

                    if (value.left <= 0) {
                        if (display != null) {
                            display.remove();
                        }
                        iterator.remove();
                        node.getItemDisplay().show(Bukkit.getPlayer(entry.getKey()));
                    } else {
                        if (display instanceof TextDisplay textDisplay) {
                            textDisplay.text(renderTime(value.left));
                        }
                    }
                }

            }
        }.runTaskTimer(VoidSurvival.getInstance(), 0, 20);
    }

    public void putCooldown(Player player, long cooldownTicks) {

        TextDisplay display = createCooldownDisplayEntity(cooldownTicks);

        player.showEntity(VoidSurvival.getInstance(), display);

        this.cooldowns.put(player.getUniqueId(), new Pair<>(cooldownTicks, display.getUniqueId()));
    }

    public boolean isOnCooldown(Player player) {
        return cooldowns.containsKey(player.getUniqueId());
    }

    private TextDisplay createCooldownDisplayEntity(long cooldownTicks) {
        Location location = new Location(node.getWorld(), node.getPos().x(), node.getPos().y() + 1, node.getPos().z()).toCenterLocation();
        return node.getWorld().spawn(location, TextDisplay.class, d -> {
            d.setBillboard(Display.Billboard.CENTER);
            d.text(renderTime(cooldownTicks));
            d.setVisibleByDefault(false);
        });
    }

    private Component renderTime(long ticksRemaining) {
        int seconds = (int) ticksRemaining / 20;
        return TextUtil.color("<gray>%d s", seconds);
    }

    public void destroy() {
        this.task.cancel();
        this.cooldowns.forEach((uuid, pair) -> {
            if (pair.right != null) {
                Entity entity = Bukkit.getEntity(pair.right);
                if (entity != null) {
                    entity.remove();
                }
            }
        });
        this.cooldowns.clear();
    }

}
