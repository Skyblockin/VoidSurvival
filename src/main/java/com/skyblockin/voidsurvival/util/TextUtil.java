package com.skyblockin.voidsurvival.util;

import com.skyblockin.voidsurvival.VoidSurvival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.function.Function;

public class TextUtil {

    public static Component message(String messageKey, Object... objects) {
        return VoidSurvival.getInstance().getMessageManager().getMessage(messageKey, objects);
    }

    public static Component color(String text, Object... objects) {
        return MiniMessage.miniMessage().deserialize(String.format(text, objects));
    }

    public static String camelCaseToSnakeCase(String s) {
        return s.replaceAll("([A-Z])", "_$1").toLowerCase();
    }

    public static String snakeCaseToCamelCase(String s) {

        String[] parts = s.toLowerCase().split("_");

        if (parts.length == 1) {
            return parts[0];
        }

        StringBuilder sb = new StringBuilder(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)))
                .append(parts[i].substring(1));
        }

        return sb.toString();
    }

    public static <T> String buildNaturalList(Function<T, String> function, List<T> list) {

        if (list.isEmpty()) {
            return "";
        }

        if (list.size() == 1) {
            return function.apply(list.getFirst());
        }

        StringBuilder sb = new StringBuilder(list.size() * (16 + 3) + 2);

        int i = 0;
        for (; i < list.size() - 2; i++) {
            sb.append(function.apply(list.get(i))).append(", ");
        }

        sb.append(function.apply(list.get(i))).append(" and ").append(function.apply(list.get(i + 1)));

        return sb.toString();
    }

}
