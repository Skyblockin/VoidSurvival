package com.skyblockin.voidsurvival.social;

import org.bukkit.entity.Player;

import java.util.Objects;

public class Friend {

    public final String uuid;
    public final String name;

    public Friend(Player player) {
        this.uuid = player.getUniqueId().toString();
        this.name = player.getName();
    }

    public Friend(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Friend friend && Objects.equals(friend.uuid, uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

}
