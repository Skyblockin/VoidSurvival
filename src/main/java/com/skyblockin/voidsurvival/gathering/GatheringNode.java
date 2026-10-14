package com.skyblockin.voidsurvival.gathering;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.config.Pair;
import com.skyblockin.voidsurvival.item.ItemData;
import com.skyblockin.voidsurvival.item.ItemManager;
import com.skyblockin.voidsurvival.math.BlockPosition;
import com.skyblockin.voidsurvival.storage.Accessors;
import com.skyblockin.voidsurvival.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@JsonDeserialize(using = GatheringNode.Deserializer.class)
@JsonSerialize(using = GatheringNode.Serializer.class)
public class GatheringNode {

    private final Location location;

    private UUID interactionEntity;
    private final SpinningItemDisplay itemDisplay;
    private final GatheringCooldownHandler cooldownHandler;

    private final GatheringNodeType type;

    private final HashMap<UUID, GatherAnimation> ongoingAnimations = new HashMap<>();

    public GatheringNode(Location location, GatheringNodeType type) {
        this.location = location;
        Location where = location.clone().toCenterLocation().subtract(0, 1, 0);
        createInteraction(where);
        this.itemDisplay = new SpinningItemDisplay(null, where, type.getReward());
        this.type = type;
        this.cooldownHandler = new GatheringCooldownHandler(this);
    }

    public GatheringNode(Location location, UUID interactionId, UUID displayId, GatheringNodeType type) {
        this.interactionEntity = interactionId;
        this.location = location;
        Location where = location.clone().toCenterLocation().subtract(0, 1, 0);
        createInteraction(where);
        this.itemDisplay = new SpinningItemDisplay(displayId, where, type.getReward());
        this.type = type;
        this.cooldownHandler = new GatheringCooldownHandler(this);
    }

    private void createInteraction(Location where) {
        // If the entity already exists, replace it with a new one in case we want to modify something here
        if (this.interactionEntity != null) {
            Entity entity = Bukkit.getEntity(this.interactionEntity);
            if (entity != null) {
                entity.remove();
            }
        }
        this.interactionEntity = where.getWorld().spawn(where, Interaction.class, interaction -> {
            interaction.setInteractionHeight(2F);
            interaction.setInteractionWidth(2F);
            interaction.setPersistent(true);
        }).getUniqueId();
    }

    public UUID getInteractionEntityUUID() {
        return interactionEntity;
    }

    public UUID getItemDisplayUUID() {
        return itemDisplay.getUniqueId();
    }

    public SpinningItemDisplay getItemDisplay() {
        return itemDisplay;
    }

    public BlockPosition getPos() {
        return BlockPosition.ofLocation(this.location);
    }

    public World getWorld() {
        return location.getWorld();
    }

    public void onLoad(Player player) {

        if (this.cooldownHandler.isOnCooldown(player)) {
            return;
        }

        this.itemDisplay.show(player);
    }

    public boolean isAlreadyGathering(Player player) {
        return this.ongoingAnimations.containsKey(player.getUniqueId());
    }

    public void gather(PlayerInteractAtEntityEvent event) {

        Player player = event.getPlayer();

        if (event.getRightClicked().getUniqueId().equals(this.interactionEntity)) {

            boolean isOnCooldown = this.cooldownHandler.isOnCooldown(player);
            boolean isAlreadyGathering = this.ongoingAnimations.containsKey(player.getUniqueId());

            ItemStack item = player.getInventory().getItemInMainHand();

            ToolData data = getToolTierAndTypeAndPower(item);

            if (data == null) {
                return;
            }

            if (data.toolType != type.requiredToolType && type.requiredToolType > 0) {
                player.sendMessage(TextUtil.color("<red>You need a different type of tool to gather this!"));
                return;
            }

            if (data.toolTier < type.requiredToolTier && type.requiredToolTier > 0) {
                player.sendMessage(TextUtil.color("<red>You need at least a tier %d tool to gather this!", type.requiredToolTier));
                return;
            }

            if (!isOnCooldown && !isAlreadyGathering) {
                this.itemDisplay.hide(player);

                float gatherSpeed = Math.max(0.2F, (float) data.toolPower());
                float gatherPerTick = gatherSpeed / type.getToughness();

                VoidSurvival.logInfo("Gathering node with %.2f toughness with %f power\n", type.getToughness(), gatherSpeed);

                GatherAnimation animation = new GatherAnimation(player, this, gatherPerTick);
                this.ongoingAnimations.put(player.getUniqueId(), animation);
                animation.start();
            }
        }

    }

    private record ToolData(int toolTier, int toolType, int toolPower) {}

    private ToolData getToolTierAndTypeAndPower(ItemStack item) {

        String id = Accessors.ITEM_ID.read(item);

        if (id != null) {
            ItemData data = ItemManager.getItemById(id);
            if (data == null || data.toolTier == -1 || data.toolType == -1 || data.toolPower == -1) {
                return null;
            }
            return new ToolData(data.toolTier, data.toolType, data.toolPower);
        }

        String[] itemTierAndName = item.getType().name().split("_");

        if (itemTierAndName.length != 2) {
            return null;
        }

        Pair<Integer, Integer> itemTierAndPower = switch (itemTierAndName[0].toLowerCase()) {
            case "wooden" -> new Pair<>(1, 2);
            case "stone" -> new Pair<>(2, 4);
            case "bronze" -> new Pair<>(3, 5);
            case "gold" -> new Pair<>(3, 12);
            case "iron" -> new Pair<>(4, 6);
            case "diamond" -> new Pair<>(5, 8);
            case "netherite" -> new Pair<>(6, 9);
            default -> null;
        };

        int itemType = switch(itemTierAndName[1].toLowerCase()) {
            case "pickaxe" -> 1;
            case "axe" -> 2;
            case "shovel" -> 3;
            case "hoe" -> 4;
            default -> -1;
        };

        if (itemTierAndPower == null || itemType == -1) {
            return null;
        }

        return new ToolData(itemTierAndPower.left, itemType, itemTierAndPower.right);
    }

    public void onCancelGathering(Player player) {
        this.ongoingAnimations.remove(player.getUniqueId());
        this.itemDisplay.show(player);
    }

    public void onFinishGathering(Player player) {
        player.getInventory().addItem(this.type.getReward());
        long ticks = type.getRegenerationTimeTicks();
        this.cooldownHandler.putCooldown(player, ticks);
        this.ongoingAnimations.remove(player.getUniqueId());
    }

    public void destroy() {

        Entity entity = Bukkit.getEntity(this.interactionEntity);

        if (entity != null) {
            entity.remove();
        }

        for (var entry : this.ongoingAnimations.entrySet()) {
            entry.getValue().cancel();
        }

        this.itemDisplay.destroy();
        this.cooldownHandler.destroy();
    }

    public static class Serializer extends StdSerializer<GatheringNode> {

        protected Serializer() {
            super(GatheringNode.class);
        }

        @Override
        public void serialize(GatheringNode value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("location", value.location);
            gen.writeStringField("display_entity", value.itemDisplay.getUniqueId().toString());
            gen.writeStringField("interaction_entity", value.interactionEntity.toString());
            gen.writeStringField("type", value.type.getId());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends StdDeserializer<GatheringNode> {

        protected Deserializer() {
            super(GatheringNode.class);
        }

        @Override
        public GatheringNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            Location location = Json.convert(node.get("location"), Location.class);
            UUID displayUUID = UUID.fromString(node.get("display_entity").asText());
            UUID interactionUUID = UUID.fromString(node.get("interaction_entity").asText());

            GatheringNodeType type = GatheringNodeType.getById(node.get("type").asText());

            return new GatheringNode(location, interactionUUID, displayUUID, type);
        }
    }
}
