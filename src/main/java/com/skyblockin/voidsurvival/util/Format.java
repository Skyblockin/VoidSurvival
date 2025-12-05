package com.skyblockin.voidsurvival.util;

import io.papermc.paper.util.Tick;

import java.time.Duration;
import java.util.Locale;

public class Format {

    public static String format(double value, int significantDecimals) {

        if (value == 0.0) {
            return "0";
        }

        if (value > 1.0D) {
            return String.format(Locale.ENGLISH, "%." + significantDecimals + "f", value);
        }

        String text = String.format(Locale.ENGLISH, "%.100f", value);

        int position = text.indexOf('.') + 1;

        // Find first non-zero
        while (position < text.length() && text.charAt(position) == '0') {
            position++;
        }

        return text.substring(0, Math.min(position + significantDecimals, text.length()));
    }

    public static String camelCaseToSnakeCase(String camelCase) {

        StringBuilder sb = new StringBuilder();

        int previous = 0;
        int position = 0;

        for (int i = 0; i < camelCase.length(); i++) {

            char c = camelCase.charAt(i);

            if (c >= 'A' && c <= 'Z') {
                sb.append("_");
            }

            position++;

            sb.append(camelCase.substring(previous, position).toLowerCase());

            previous = position;
        }

        return sb.toString();
    }

    public static String getFormattedTickTime(long ticks) {

        if (ticks < 0) {
            return "Infinite";
        }

        return getFormattedTime(Tick.of(ticks));
    }

    public static String title(String text) {

        String[] parts = text.split("[_ ]");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
        }

        return String.join(" ", parts);
    }

    public static String getFormattedTime(Duration duration) {

        long seconds = duration.getSeconds();

        if (seconds == 0) {
            return "0s";
        }

        long h = duration.toHours();
        long m = duration.toMinutes() % 60;
        long s = seconds % 60;

        if (seconds < 60) {
            return String.format("%ds", seconds);
        }

        if (seconds < 3600) {
            return String.format("%dm %ds", m, s);
        }

        return String.format("%dh %dm %ds", h, m, s);
    }

    /**
     * Format the time given in seconds as H:mm:ss
     * @param seconds the time in seconds
     * @return the time formatted as H:mm:ss
     */
    public static String getFormattedTime(long seconds) {

        if (seconds == 0) {
            return "0s";
        }

        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = (seconds % 60);

        if (seconds < 60) {
            return String.format("%ds", seconds);
        }

        if (seconds < 3600) {
            return String.format("%dm %ds", m, s);
        }

        return String.format("%dh %dm %ds", h, m, s);
    }

}
