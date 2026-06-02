package com.skyblockin.voidsurvival.storage;

import com.skyblockin.voidsurvival.constants.CustomPersistentDataType;
import com.skyblockin.voidsurvival.math.Position;
import org.bukkit.persistence.PersistentDataType;

public final class Accessors {

    public static final Accessor<Boolean> CAN_PLACE = new Accessor<>("can_place", PersistentDataType.BOOLEAN);
    public static final Accessor<String> ITEM_ID = new Accessor<>("item_id", PersistentDataType.STRING);
    public static final Accessor<String> ITEM_REPLACEMENT = new Accessor<>("item_replacement", PersistentDataType.STRING);
    public static final Accessor<String> SIGN_COMMAND = new Accessor<>("sign_command", PersistentDataType.STRING);
    public static final Accessor<String> CAMPFIRE_WARP_ID = new Accessor<>("campfire_warp", PersistentDataType.STRING);
    public static final Accessor<Position> CAMPFIRE_WARP_POSITION = new Accessor<>("campfire_warp_position", CustomPersistentDataType.POSITION);
    public static final Accessor<String> STANCE_ID = new Accessor<>("stance_id", PersistentDataType.STRING);

}
