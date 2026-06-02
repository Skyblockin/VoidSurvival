package com.skyblockin.voidsurvival.nms;

import com.skyblockin.voidsurvival.math.BlockPosition;
import io.papermc.paper.adventure.AdventureCodecs;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class NMSUtil {

    public static Tag color(String text) {
        if (text == null) return null;
        return AdventureCodecs.COMPONENT_CODEC.encodeStart(
            NbtOps.INSTANCE, MiniMessage.miniMessage().deserialize(text)
        ).getOrThrow();
    }

    public static net.minecraft.world.entity.LivingEntity getNMSEntity(LivingEntity entity) {
        return ((CraftLivingEntity)entity).getHandle();
    }

    public static void jump(LivingEntity entity) {
        getNMSEntity(entity)
            .jumpFromGround();
    }

    public static void moveTo(LivingEntity entity, Location location) {
        getNMSEntity(entity)
            .moveOrInterpolateTo(new Vec3(location.getX(), location.getY(), location.getZ()),
                location.getYaw(), location.getPitch()
            );
    }

    public static void sendPacket(Player player, Packet<@NotNull ClientGamePacketListener> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public static void resetBlockCrack(Iterable<Player> audience, BlockPosition position) {
        broadcastBlockCrack(audience, position, -1);
    }

    public static void broadcastBlockCrack(Iterable<Player> audience, BlockPosition position, float progress) {
        int stage = (int) (progress * 10.0F);
        broadcastBlockCrack(audience, position, stage);
    }

    public static void broadcastBlockCrack(Iterable<Player> audience, BlockPosition position, int stage) {
        BlockPos pos = new BlockPos(position.x(), position.y(), position.z());
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(ThreadLocalRandom.current().nextInt(), pos, stage);

        for (Player player : audience) {
            sendPacket(player, packet);
        }
    }
}
