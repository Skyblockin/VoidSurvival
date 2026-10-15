package com.skyblockin.voidsurvival.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.storage.PlayerData;
import com.skyblockin.voidsurvival.trigger.triggerable.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public final class TriggerManager implements Listener {

    private final HashMap<String, Triggerable> triggerables = new HashMap<>();
    private final ArrayList<InteractTrigger> interactTriggers = new ArrayList<>();
    private final ArrayList<KillGainTrigger> killTriggers = new ArrayList<>();

    public void addTriggerable(Triggerable triggerable) {
        triggerables.put(triggerable.getId(), triggerable);
    }

    public Triggerable getTriggerable(String id) {
        return triggerables.get(id);
    }

    public <T extends Event> void addTrigger(TriggerType type, Trigger<T> trigger) {
        switch (type) {
            case INTERACT -> interactTriggers.add((InteractTrigger) trigger);
            case KILL_GAIN -> killTriggers.add((KillGainTrigger) trigger);
        }
    }

    public boolean trigger(String id, Player player, PlayerData data) {

        Triggerable triggerable = triggerables.get(id);

        if (triggerable == null) {
            return false;
        }

        triggerable.trigger(this, player, data);

        return true;
    }

    public boolean trigger(String id) {
        return trigger(id, null, null);
    }

    public void load() {

        try {

            File triggerFile = new File(VoidSurvival.getInstance().getDataFolder(), "triggers.json");
            File triggerableFile = new File(VoidSurvival.getInstance().getDataFolder(), "triggerables.json");

            if (!triggerFile.exists()) {
                triggerFile.getParentFile().mkdirs();
                triggerFile.createNewFile();
            }

            if (!triggerableFile.exists()) {
                triggerableFile.getParentFile().mkdirs();
                triggerableFile.createNewFile();
            }

            // Clear old lists on reload
            interactTriggers.clear();
            killTriggers.clear();

            JsonNode triggerFileNode = Json.readFromFile(triggerFile);
            JsonNode triggersListNode = triggerFileNode.get("triggers");

            if (triggersListNode != null && triggersListNode.isArray()) {

                for (JsonNode node : triggersListNode) {

                    if (!node.has("type")) {
                        VoidSurvival.logError("Encountered trigger with no 'type' field, skipping...");
                        continue;
                    }

                    String triggerType = node.get("type").asText().toLowerCase();

                    switch (triggerType) {
                        case "kill_gain" -> addTrigger(TriggerType.KILL_GAIN, Json.convert(node, KillGainTrigger.class));
                        case "interact" -> addTrigger(TriggerType.INTERACT, Json.convert(node, InteractTrigger.class));
                        default -> VoidSurvival.logError("Unknown trigger type: " + triggerType);
                    }
                }
            }

            JsonNode triggerableFileNode = Json.readFromFile(triggerableFile);

            triggerables.clear();

            for (JsonNode node : triggerableFileNode) {

                if (!node.has("type")) {
                    VoidSurvival.logError("Encountered triggerable with no 'type' field, skipping...");
                    continue;
                }

                String triggerableType = node.get("type").asText().toLowerCase();

                switch (triggerableType) {
                    case "gate" -> addTriggerable(Json.convert(node, Gate.class));
                    case "sound" -> addTriggerable(Json.convert(node, SoundPlayer.class));
                    case "item" -> addTriggerable(Json.convert(node, ItemGive.class));
                    case "compound" -> addTriggerable(Json.convert(node, Compound.class));
                    default -> VoidSurvival.logError("Unknown triggerable type: " + triggerableType);
                }
            }

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to load triggers and triggerables", ex);
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        for (InteractTrigger trigger : interactTriggers) {
            trigger.activate(this, event);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        for (KillGainTrigger trigger : killTriggers) {
            trigger.activate(this, event);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        for (KillGainTrigger trigger : killTriggers) {
            trigger.onPlayerLeave(event);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (KillGainTrigger trigger : killTriggers) {
            trigger.onPlayerJoin(event);
        }
    }

}
