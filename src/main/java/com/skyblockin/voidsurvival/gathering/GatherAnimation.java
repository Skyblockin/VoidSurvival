package com.skyblockin.voidsurvival.gathering;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;

public class GatherAnimation extends BukkitRunnable {

    private float progress = 0.0F;
    private final Player player;
    private final GatheringNode node;
    private final float gatherSpeed;
    private TextDisplay textDisplay;

    public GatherAnimation(Player player, GatheringNode node, float gatherSpeed) {
        this.player = player;
        this.node = node;
        this.gatherSpeed = gatherSpeed;
    }

    public boolean isDone() {
        return progress >= 1;
    }

    public void start() {
        this.textDisplay = spawnTextDisplay();
        this.runTaskTimer(VoidSurvival.getInstance(), 0, 10);
    }

    private TextDisplay spawnTextDisplay() {

        BlockPosition pos = node.getPos();

        Location where = new Location(node.getWorld(), pos.x(), pos.y(), pos.z()).toCenterLocation().add(0, 1, 0);

        return node.getWorld().spawn(where, TextDisplay.class, display -> {
            display.setBillboard(Display.Billboard.CENTER);
            display.text(renderProgressBar(0.0F));
        });
    }

    private Component renderProgressBar(float progress) {

        int ticks = (int) Math.ceil(progress * 20.0F);

        if (ticks >= 19) {
           return TextUtil.color("<green>[" + "|".repeat(18) + "]");
        } else {
            int progressedTicks = Math.max(0, ticks - 1);
            return TextUtil.color("<yellow>[" + "|".repeat(progressedTicks) + "<dark_gray>" + "|".repeat(18 - progressedTicks) + "]");
        }
    }

    public void cancel() {
        super.cancel();
        this.textDisplay.remove();
    }

    public void run() {

        progress += gatherSpeed;

        this.textDisplay.text(renderProgressBar(progress));

        if (player.getLocation().distanceSquared(this.textDisplay.getLocation()) >= 15.0) {
            this.node.onCancelGathering(this.player);
            cancel();
        }

        if (isDone()) {
            this.node.onFinishGathering(this.player);
            cancel();
        }
    }
}
