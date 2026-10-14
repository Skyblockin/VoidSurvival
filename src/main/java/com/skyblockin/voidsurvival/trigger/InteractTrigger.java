package com.skyblockin.voidsurvival.trigger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.storage.PlayerData;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

//@JsonDeserialize(using = InteractTrigger.Deserializer.class)
public class InteractTrigger implements Trigger<PlayerInteractEvent> {

    private final String id;
    private final List<String> triggerableIds;
    private final Block block;
    private final Requirements requirements;

    @JsonCreator
    public InteractTrigger(String id, List<String> triggerableIds, World world, BlockPosition pos, Requirements requirements) {
        this.id = id;
        this.triggerableIds = triggerableIds;
        this.block = pos.toLocation(world).getBlock();
        this.requirements = requirements;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void activate(PlayerInteractEvent event) {

        Block clicked = event.getClickedBlock();
        Player player = event.getPlayer();
        PlayerData data = PlayerData.of(player);

        if (clicked != null && clicked.equals(this.block) && (requirements == null || requirements.test(data))) {
            for (String triggerableId : triggerableIds) {
                VoidSurvival.getInstance().getTriggerManager().trigger(triggerableId, player, data);
            }
        }
    }

    @Override
    public Requirements getRequirements() {
        return requirements;
    }

    /*
    public static class Deserializer extends StdDeserializer<InteractTrigger> {

        public Deserializer() {
            super(InteractTrigger.class);
        }

        @Override
        public InteractTrigger deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            String id = node.get("id").asText();

            JsonNode triggersNode = node.get("triggers");

            List<String> triggerableIds = Json.listFromJson(triggersNode, String.class);

            World world = Json.fromJson(node.get("world"), World.class);
            BlockPos pos = Json.fromJson(node.get("pos"), BlockPos.class);

            Requirements requirements = null;
            if (node.has("requirements")) {
                requirements = Json.fromJson(node.get("requirements"), Requirements.class);
            }

            return new InteractTrigger(id, triggerableIds, world, pos, requirements);
        }
    }*/

}
