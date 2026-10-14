package com.skyblockin.voidsurvival.trigger;

import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

public class ProgressionDisplay {

    private final String id;
    private final String objectiveName;
    private final World world;
    private final BlockPosition pos;
    private final int requiredStat;
    private int currentStat = 0;
    private transient TextDisplay displayEntity;

    public ProgressionDisplay(String id, String objectiveName, World world, BlockPosition pos, int requiredStat) {
        this.id = id;
        this.objectiveName = objectiveName;
        this.world = world;
        this.pos = pos;
        this.requiredStat = requiredStat;
    }

    public String getId() {
        return id;
    }

    public void destroy() {
        if (this.displayEntity != null) {
            this.displayEntity.remove();
        }
    }

    public void setStat(int amount) {
        this.currentStat = amount;
        updateEntity();
    }

    public void incrementStat(int by) {
        this.currentStat += by;
        updateEntity();
    }

    public void updateEntity() {
        if (this.displayEntity != null && this.displayEntity.isValid()) {
            this.displayEntity.text(renderText());
        } else {
            createTrackerEntity();
        }
    }

    private void createTrackerEntity() {
        this.displayEntity = world.spawn(pos.toLocation(world), TextDisplay.class, display -> {
            display.setBillboard(Display.Billboard.CENTER);
            display.text(renderText());
            display.setPersistent(false);
        });
    }

    private Component renderText() {
        return TextUtil.color("%s\n<green><bold>%d/%d</bold></green>",
            this.objectiveName, Math.min(this.currentStat, this.requiredStat), this.requiredStat
        );
    }

}
