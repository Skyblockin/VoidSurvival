package com.skyblockin.voidsurvival.storage;

import org.bukkit.persistence.PersistentDataType;

public final class Accessors {

    public static final Accessor<Boolean> CAN_PLACE = new Accessor<>("can_place", PersistentDataType.BOOLEAN);
    public static final Accessor<String> ITEM_ID = new Accessor<>("item_id", PersistentDataType.STRING);

}
