package com.skyblockin.voidsurvival.ability;

import dev.aurelium.auraskills.api.mana.CustomManaAbility;
import dev.aurelium.auraskills.api.registry.NamespacedId;

public class Abilities {

    public static final CustomManaAbility TREECAPITATOR = CustomManaAbility
        .builder(NamespacedId.of("voidsurvival", "treecapitator"))
        .displayName("Treecapitator")
        .unlock(0)
        .baseCooldown(0)
        .cooldownPerLevel(0)
        .baseManaCost(5)
        .manaCostPerLevel(0)
        .description("Quickly break connected logs [Shift while breaking a log to activate]")
        .build();

}
