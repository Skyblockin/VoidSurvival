package com.skyblockin.voidsurvival.trigger;

import com.skyblockin.voidsurvival.storage.PlayerData;

public class Requirements {

    public int kills = 0;
    public Boolean hasGeneratedIsland = null;

    public boolean test(PlayerData data) {
        return data.kills >= kills && (hasGeneratedIsland == null || hasGeneratedIsland == data.hasGeneratedIsland);
    }

}
