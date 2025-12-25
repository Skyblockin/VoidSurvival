package com.skyblockin.voidsurvival.nms;

import io.papermc.paper.adventure.AdventureCodecs;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NMSUtil {

    public static Tag color(String text) {
        if (text == null) return null;
        return AdventureCodecs.COMPONENT_CODEC.encodeStart(
            NbtOps.INSTANCE, MiniMessage.miniMessage().deserialize(text)
        ).getOrThrow();
    }

}
