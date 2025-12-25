package com.skyblockin.voidsurvival.region;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.skyblockin.voidsurvival.config.Json;
import org.jetbrains.annotations.Nullable;

public class MobCapFlag extends Flag<MobCapMap> {

    public MobCapFlag(String name) {
        super(name);
    }

    @Override
    public MobCapMap parseInput(FlagContext context) throws InvalidFlagFormat {
        try {
            return Json.stringToValue(context.getUserInput(), MobCapMap.class);
        } catch (JsonProcessingException e) {
            throw new InvalidFlagFormat("Could not parse input into MobCapMap: " + e.getMessage());
        }
    }

    @Override
    public MobCapMap unmarshal(@Nullable Object o) {
        return Json.convert(o, MobCapMap.class);
    }

    @Override
    public Object marshal(MobCapMap o) {
        return Json.toJson(o).toString();
    }
}
