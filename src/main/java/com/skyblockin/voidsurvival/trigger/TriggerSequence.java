package com.skyblockin.voidsurvival.trigger;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.triggerable.Compound;
import com.skyblockin.voidsurvival.trigger.triggerable.Triggerable;
import com.skyblockin.voidsurvival.trigger.triggerable.Wait;
import org.bukkit.entity.Player;

import java.util.List;

public class TriggerSequence {

    public static void runSequence(List<String> triggerableIds, TriggerManager manager, Player player, PlayerData data) {
        processIndex(0, triggerableIds, manager, player, data, () -> {});
    }

    public static void runSequence(List<String> triggerableIds, TriggerManager manager, Player player, PlayerData data, Runnable onComplete) {
        processIndex(0, triggerableIds, manager, player, data, onComplete);
    }

    private static void processIndex(int index, List<String> triggerableIds, TriggerManager manager, Player player, PlayerData data, Runnable onComplete) {
        if (index >= triggerableIds.size()) {
            onComplete.run();
            return;
        }

        String entry = triggerableIds.get(index);
        int nextIndex = index + 1;

        if (tryHandleInline(entry, nextIndex, triggerableIds, manager, player, data, onComplete)) {
            return;
        }

        Triggerable triggerable = manager.getTriggerable(entry);

        switch (triggerable) {
            case null -> processIndex(nextIndex, triggerableIds, manager, player, data, onComplete);
            case Wait wait -> VoidSurvival.getInstance().runTaskLater(() ->
                    processIndex(nextIndex, triggerableIds, manager, player, data, onComplete), wait.ticks()
            );
            case Compound compound ->
                compound.triggerWithCallback(manager, player, data, () ->
                    processIndex(nextIndex, triggerableIds, manager, player, data, onComplete)
                );
            default -> {
                triggerable.trigger(manager, player, data);
                processIndex(nextIndex, triggerableIds, manager, player, data, onComplete);
            }
        }
    }

    private static boolean tryHandleInline(String entry, int nextIndex, List<String> triggerableIds, TriggerManager manager, Player player, PlayerData data, Runnable onComplete) {
        int separatorIndex = entry.indexOf(':');
        if (separatorIndex == -1) return false;

        String command = entry.substring(0, separatorIndex).toLowerCase();
        String args = entry.substring(separatorIndex + 1);

        switch (command) {
            case "wait" -> {
                try {
                    long ticks = Long.parseLong(args);
                    VoidSurvival.getInstance().runTaskLater(() ->
                            processIndex(nextIndex, triggerableIds, manager, player, data, onComplete),
                        ticks
                    );
                } catch (NumberFormatException e) {
                    processIndex(nextIndex, triggerableIds, manager, player, data, onComplete);
                }
                return true;
            }
            case "sound" -> {
                playInlineSound(player, args);
                processIndex(nextIndex, triggerableIds, manager, player, data, onComplete);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void playInlineSound(Player player, String args) {
        String[] parts = args.split(":");
        String soundName = parts[0];
        float volume = 1.0f;
        float pitch = 1.0f;

        try {
            if (parts.length > 1) volume = Float.parseFloat(parts[1]);
            if (parts.length > 2) pitch = Float.parseFloat(parts[2]);
        } catch (NumberFormatException ignored) {}

        player.playSound(player.getLocation(), soundName, volume, pitch);
    }
}
