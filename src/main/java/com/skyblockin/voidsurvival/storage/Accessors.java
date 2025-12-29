package com.skyblockin.voidsurvival.storage;

import org.bukkit.persistence.PersistentDataType;

public final class Accessors {

    public static final Accessor<Boolean> CAN_PLACE = new Accessor<>("can_place", PersistentDataType.BOOLEAN);
    public static final Accessor<String> ITEM_ID = new Accessor<>("item_id", PersistentDataType.STRING);
    public static final Accessor<String> ITEM_REPLACEMENT = new Accessor<>("item_replacement", PersistentDataType.STRING);
    public static final Accessor<String> SIGN_COMMAND = new Accessor<>("sign_command", PersistentDataType.STRING);

}
