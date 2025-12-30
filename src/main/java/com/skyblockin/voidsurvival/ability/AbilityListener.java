package com.skyblockin.voidsurvival.ability;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.util.Offset;
import com.skyblockin.voidsurvival.util.TagUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.api.user.SkillsUser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.util.TriState;
import org.bukkit.GameEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class AbilityListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
        Block block = event.getBlock();
        ItemStack item = player.getInventory().getItemInMainHand();

        breakLogs(block, player, user, item);
    }

    private void breakLogs(Block start, Player player, SkillsUser user, ItemStack item) {

        final Material startType = start.getType();
        double xpGainedForMaterial = 0;

        if (!TagUtil.isTagged(BlockTypeTagKeys.LOGS, startType)) {
            return;
        }

        int level = user.getManaAbilityLevel(Abilities.TREECAPITATOR);
        double manaCost = Abilities.TREECAPITATOR.getManaCost(level);

        // If this value is null, the item is not valid for this type of block
        Double breakSpeed = getBreakSpeed(startType, item);

        if (breakSpeed == null || !player.isSneaking() || !user.consumeMana(manaCost)) {
            return;
        }

        for (XpSource source : Skills.FORAGING.getSources()) {
            if (source.name().equalsIgnoreCase(startType.name())) {
                xpGainedForMaterial = source.getXp() / 2;
                break;
            }
        }

        double finalXpGained = xpGainedForMaterial;

        int maxBlocks = (int) Abilities.TREECAPITATOR.getValue(user.getManaAbilityLevel(Abilities.TREECAPITATOR));
        int blocksPerTick = 20;
        long tickPeriod = 0;

        if (!player.getGameMode().equals(GameMode.CREATIVE)) {

            int efficiencyLevel = item.getEnchantmentLevel(Enchantment.EFFICIENCY);

            // Magic value 6 to make it feel better lol, an unenchanted wooden axe should take roughly 10 ticks to break a log
            double toughness = startType.getHardness() * 6.0;
            double breakingEfficiency = breakSpeed * (1 + efficiencyLevel);
            double finalBreakingSpeed = 1 / (toughness / breakingEfficiency);

            if (finalBreakingSpeed <= 1) {
                tickPeriod = (int) Math.floor(1 / finalBreakingSpeed);
                blocksPerTick = 1;
            } else {
                blocksPerTick = (int) Math.round(finalBreakingSpeed);
            }
        }

        final int finalBlocksPerTick = blocksPerTick;
        final Set<Block> cleared = new HashSet<>();
        final LinkedList<Block> queue = new LinkedList<>();

        queue.add(start);
        cleared.add(start);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < finalBlocksPerTick; i++) {

                    Block block;

                    do {
                        block = queue.poll();
                        if (block == null) {
                            cancel();
                            return;
                        }
                    } while (!cleared.add(block) && !queue.isEmpty());

                    if (!breakBlock(block, user, player, item, finalXpGained) || cleared.size() >= maxBlocks) {
                        cancel();
                        return;
                    }

                    for (Offset offset : Offset.values()) {
                        Block neighbor = offset.getBlock(block);
                        if (neighbor.getType().equals(startType)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }.runTaskTimer(VoidSurvival.getInstance(), 0, tickPeriod);
    }

    private Double getBreakSpeed(Material material, ItemStack item) {

        Tool tool = item.getData(DataComponentTypes.TOOL);
        Double breakSpeed = null;

        if (tool != null) {

            TypedKey<@NotNull BlockType> key = TypedKey.create(RegistryKey.BLOCK, material.key());

            for (Tool.Rule rule : tool.rules()) {
                Float speed = rule.speed();
                if (rule.blocks().contains(key) && rule.correctForDrops() == TriState.TRUE && speed != null) {
                    breakSpeed = speed.doubleValue();
                }
            }

        }

        return breakSpeed;
    }

    private boolean breakBlock(Block block, SkillsUser user, Player player, ItemStack item, double finalXpGained) {

        if (!player.getInventory().getItemInMainHand().equals(item)) {
            player.updateInventory();
            return false;
        }

        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            block.getWorld().sendGameEvent(null, GameEvent.BLOCK_DESTROY, block.getLocation().toVector());
            if (!AuraSkillsBukkit.get().getRegions().isPlacedBlock(block)) {
                user.addSkillXp(Skills.FORAGING, finalXpGained);
            }
            block.setType(Material.AIR);

            return true;
        }

        if (block.breakNaturally(item, true, true)) {
            ItemStack newItem = item.damage(1, player);
            if (newItem.isEmpty()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                return false;
            }
        }

        if (!AuraSkillsBukkit.get().getRegions().isPlacedBlock(block)) {
            user.addSkillXp(Skills.FORAGING, finalXpGained);
        }

        return true;
    }

}
