package com.skyblockin.voidsurvival.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TextUtil {

    public static Component color(String text, Object... objects) {
        return MiniMessage.miniMessage().deserialize(String.format(text, objects));
    }

}
