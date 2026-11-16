package com.skyblockin.voidsurvival.manager;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.gathering.GatheringNode;
import com.skyblockin.voidsurvival.gathering.GatheringNodeType;
import com.skyblockin.voidsurvival.math.BlockPosition;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GatheringNodeManager extends Manager implements Listener {

    private final HashMap<String, GatheringNodeType> gatheringNodeTypes = new HashMap<>();
    private final HashMap<BlockPosition, GatheringNode> gatheringNodes = new HashMap<>();

    public void registerNodeType(GatheringNodeType type) {
        gatheringNodeTypes.put(type.getId(), type);
    }

    public GatheringNodeType getGatheringNodeType(String id) {
        return gatheringNodeTypes.get(id);
    }

    /**
     * Register a new gathering
     * @param node the new node
     * @return true if creating this node replaced an older node
     */
    public boolean addNode(GatheringNode node) {
        GatheringNode old = gatheringNodes.put(node.getPos(), node);
        if (old != null) {
            old.destroy();
            return true;
        }
        return false;
    }

    public boolean removeNode(BlockPosition pos) {

        GatheringNode node = gatheringNodes.remove(pos);

        if (node != null) {
            node.destroy();
            return true;
        }

        return false;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {

        if (!isEnabled()) return;

        GatheringNode alreadyGathering = null;

        for (GatheringNode node : gatheringNodes.values()) {
            if (node.isAlreadyGathering(event.getPlayer())) {
                if (alreadyGathering != null) {
                    alreadyGathering.onCancelGathering(event.getPlayer());
                    break;
                }
                alreadyGathering = node;
            } else if (alreadyGathering == null) {
                node.gather(event);
            }
        }
    }

    @EventHandler
    public void onLoad(PlayerChunkLoadEvent event) {

        if (!isEnabled()) return;

        Player player = event.getPlayer();
        Chunk chunk = event.getChunk();

        for (GatheringNode node : gatheringNodes.values()) {
            if (node.getWorld().equals(chunk.getWorld()) && chunk.getX() == node.getPos().x() >> 4 && chunk.getZ() == node.getPos().z() >> 4) {
                node.onLoad(player);
            }
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (!isEnabled()) return;

        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        for (GatheringNode node : gatheringNodes.values()) {
            if (node.getWorld().equals(chunk.getWorld()) && chunk.getX() == node.getPos().x() >> 4 && chunk.getZ() == node.getPos().z() >> 4) {
                node.onLoad(player);
            }
        }

    }

    @Override
    public void reload(VoidSurvival plugin) {

        try {

            File typeFile = plugin.getOrCreateFile("gathering_node_types.json");
            File dataFile = plugin.getOrCreateFile("gathering_nodes.json");

            if (typeFile == null || dataFile == null) {
                VoidSurvival.logError("Failed to load gathering node files");
                return;
            }

            for (GatheringNode node : gatheringNodes.values()) {
                node.destroy();
            }

            gatheringNodeTypes.clear();
            gatheringNodes.clear();

            List<GatheringNodeType> types = Json.listFromJson(typeFile, GatheringNodeType.class);

            for (GatheringNodeType node : types) {
                registerNodeType(node);
            }

            List<GatheringNode> nodes = Json.listFromJson(dataFile, GatheringNode.class);

            VoidSurvival.logInfo("Loaded %d nodes", nodes.size());

            for (GatheringNode node : nodes) {
                addNode(node);
            }
        } catch (Exception ex) {
            VoidSurvival.logError("Failed to load gathering node data", ex);
        }

    }

    public void save() throws IOException {

        File dataFile = new File(VoidSurvival.getInstance().getDataFolder(), "gathering_nodes.json");

        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            dataFile.createNewFile();
        }

        Json.writeToFile(dataFile, gatheringNodes.values());
    }

}
