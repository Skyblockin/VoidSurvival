package com.skyblockin.voidsurvival.world.town;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.math.Cuboid;
import com.skyblockin.voidsurvival.world.edit.WorldGuardUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class Siege {

    public Town defender;
    public List<Town> defenderAllies = new ArrayList<>();
    public Town attacker;
    public BukkitTask task = null;

    public Siege(Town attacker, Town defender) {
        this.attacker = attacker;
        this.defender = defender;
    }

    public void addDefenderAlly(Town town) {
        defenderAllies.add(town);
    }

    public void begin() {

        World world = defender.center.getWorld();

        ProtectedRegion region = WorldGuardUtil.getRegion(world, defender.regionId);

        if (region == null) {
            return;
        }

        BlockVector3 max = region.getMaximumPoint();
        BlockVector3 min = region.getMinimumPoint();

        Cuboid cuboid = new Cuboid(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());

        ArrayList<SiegeSpawnTask> siegeTasks = new ArrayList<>();

        var attackerTask = new SiegeSpawnTask(world, cuboid, false, attacker.population, attacker::createSoldier);
        var defenderTask = new SiegeSpawnTask(world, cuboid, true, defender.population, defender::createSoldier);

        siegeTasks.add(defenderTask);

        for (Town town : defenderAllies) {
            var allyTask = new SiegeSpawnTask(world, cuboid, true, town.population / 5, town::createSoldier);
            siegeTasks.add(allyTask);
        }

        for (SiegeSpawnTask runnable : siegeTasks) {
            runnable.runTaskTimer(VoidSurvival.getInstance(), 0, 200);
        }

        attackerTask.runTaskTimer(VoidSurvival.getInstance(), 0, 200);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (isCancelled() || attackerTask.isCancelled()) {
                    for (SiegeSpawnTask runnable : siegeTasks) {
                        runnable.cancel();
                    }
                    attackerTask.cancel();
                    cancel();
                }
            }
        }.runTaskTimer(VoidSurvival.getInstance(), 0, 200);
    }

    public boolean isRunning() {
        return this.task != null && !this.task.isCancelled();
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    public static class SiegeSpawnTask extends BukkitRunnable {

        private final World world;
        private final Cuboid cuboid;
        private final boolean isDefender;
        final ArrayList<NPC> aliveFighters = new ArrayList<>();
        final Function<Location, NPC> fighterCreator;
        final int maxSpawnAttempts;
        final int maxAlive;
        int remainingFighters;

        public SiegeSpawnTask(World world, Cuboid cuboid, boolean isDefender, int population, Function<Location, NPC> fighterCreator) {
            this.world = world;
            this.cuboid = cuboid;
            this.isDefender = isDefender;
            this.fighterCreator = fighterCreator;
            this.remainingFighters = 1 + population / 20;
            this.maxSpawnAttempts = remainingFighters / 10;
            this.maxAlive = maxSpawnAttempts;
        }

        @Override
        public void run() {

            if (remainingFighters == 0) {
                cancel();
            }

            aliveFighters.removeIf(defender -> {
                if (defender.getEntity().isDead()) {
                    remainingFighters--;
                    return true;
                }
                return false;
            });

            if (aliveFighters.size() < maxAlive) {
                int attempts = 0;
                while (attempts < maxSpawnAttempts && aliveFighters.size() < maxAlive) {
                    attempts++;
                    Location where = findSuitableSpawnLocation(world, cuboid, 20);
                    if (where != null) {
                        aliveFighters.add(fighterCreator.apply(where));
                    }
                }
            }

        }

        private Location findSuitableSpawnLocation(World world, Cuboid cuboid, int attempts) {

            ThreadLocalRandom random = ThreadLocalRandom.current();

            while (attempts > 0) {

                attempts--;

                int x = random.nextInt(cuboid.getMin().x(), cuboid.getMax().x() + 1);
                int z = random.nextInt(cuboid.getMin().z(), cuboid.getMax().z() + 1);

                Block block = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);

                if (block.isLiquid() || block.getType() == Material.CACTUS) continue;

                Block up1 = block.getRelative(BlockFace.UP);
                Block up2 = up1.getRelative(BlockFace.UP);

                if (up1.isEmpty() && up2.isEmpty()) {
                    return up1.getLocation().add(0.5, 0, 0.5);
                }

            }

            return null;
        }
    }
}
