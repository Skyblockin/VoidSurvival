package com.skyblockin.voidsurvival.storage;

import org.bukkit.persistence.PersistentDataType;

public final class Accessors {

    public static final Accessor<String> LOOT_CHEST_ID = new Accessor<>("loot_chest", PersistentDataType.STRING);

}
