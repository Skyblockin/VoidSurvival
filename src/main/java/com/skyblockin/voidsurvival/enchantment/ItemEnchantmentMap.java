package com.skyblockin.voidsurvival.enchantment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.skyblockin.voidsurvival.config.Gettable;
import com.skyblockin.voidsurvival.config.RangedValue;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import org.bukkit.enchantments.Enchantment;

import java.io.IOException;
import java.util.HashMap;

@SuppressWarnings("UnstableApiUsage")
@JsonDeserialize(using = ItemEnchantmentMap.Deserializer.class)
public class ItemEnchantmentMap implements Gettable<ItemEnchantments> {

    private final HashMap<Enchantment, RangedValue> enchantments;

    public ItemEnchantmentMap(HashMap<Enchantment, RangedValue> enchantments) {
        this.enchantments = enchantments;
    }

    @Override
    public ItemEnchantments get() {

        ItemEnchantments.Builder builder = ItemEnchantments.itemEnchantments();

        enchantments.forEach((enchantment, value) -> builder.add(enchantment, value.get()));

        return builder.build();
    }

    public static class Deserializer extends StdDeserializer<ItemEnchantmentMap> {

        protected Deserializer() {
            super(ItemEnchantmentMap.class);
        }

        @Override
        public ItemEnchantmentMap deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            HashMap<Enchantment, RangedValue> map = p.getCodec().readValue(p, new TypeReference<>(){});

            return new ItemEnchantmentMap(map);
        }
    }
}
