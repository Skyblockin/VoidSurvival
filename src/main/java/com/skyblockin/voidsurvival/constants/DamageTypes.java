package com.skyblockin.voidsurvival.constants;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.damage.DamageType;

public final class DamageTypes {

    public static final DamageType GREED = getDamageType("greed");
    public static final DamageType BLEED = getDamageType("bleed");

    public static DamageType getDamageType(String key) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).get(Key.key("voidsurvival", key));
    }

}
