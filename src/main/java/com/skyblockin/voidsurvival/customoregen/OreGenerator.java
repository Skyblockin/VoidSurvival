package com.skyblockin.voidsurvival.customoregen;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class OreGenerator implements Listener {

    private final HashMap<String, CriteriaBasedUpgrade> upgrades = new HashMap<>();
    private final ArrayList<CriteriaBasedUpgrade> upgradeOrder = new ArrayList<>();
    private final HashMap<UUID, String> currentUpgrade = new HashMap<>();
    private final HashMap<Block, UUID> lastBrokenBy = new HashMap<>();

    public void reload() {

        File file = FileUtil.createOrGetFile("ore_upgrades.json");

        if (file != null) {

            upgrades.clear();
            upgradeOrder.clear();

            try {

                JsonNode node = Json.readFromFile(file);

                node.elements().forEachRemaining(element -> {

                    CriteriaBasedUpgrade upgrade = Json.convert(element, CriteriaBasedUpgrade.class);

                    upgradeOrder.add(upgrade);
                    upgrades.put(upgrade.getId(), upgrade);
                });

            } catch (Exception ex) {
                VoidSurvival.logError("Encountered error while loading ore upgrades: ", ex);
            }

        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        for (CriteriaBasedUpgrade upgrade : upgradeOrder) {
            if (upgrade.test(player)) {
                currentUpgrade.put(player.getUniqueId(), upgrade.getId());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        lastBrokenBy.put(event.getBlock(), event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {

        UUID uuid = lastBrokenBy.get(event.getBlock());

        if (uuid == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);

        if (player != null && event.getNewState().getType() == Material.COBBLESTONE) {

            CriteriaBasedUpgrade current = upgrades.get(currentUpgrade.get(uuid));

            if (current != null) {
                BlockData data = current.getRewardList().choose().createBlockData();
                event.getNewState().setBlockData(data);
            }

            lastBrokenBy.remove(event.getBlock());
        }

    }

    @EventHandler
    public void onStatisticIncrement(PlayerStatisticIncrementEvent event) {

        Player player = event.getPlayer();
        Statistic.Type type = event.getStatistic().getType();

        if (type != Statistic.Type.BLOCK && type != Statistic.Type.ITEM) {
            return;
        }

        for (CriteriaBasedUpgrade upgrade : upgradeOrder) {
            if (upgrade.test(player)) {
                currentUpgrade.put(player.getUniqueId(), upgrade.getId());
            }
        }
    }

}
