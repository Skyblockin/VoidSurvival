package com.skyblockin.voidsurvival.message;

import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

public class MessageManager {

    private final HashMap<String, String> messages = new HashMap<>();

    public void reload() {

        File file = VoidSurvival.getInstance().getFile("messages.yml");

        if (!file.exists()) {
            writeEmptyConfig();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        int messageCount = 0;
        messages.clear();

        for (String key : config.getKeys(true)) {
            String value = config.getString(key);
            if (value != null && !value.isEmpty()) {
                messages.put(key, value);
                messageCount++;
            }
        }

        VoidSurvival.logInfo("Loaded %d messages", messageCount);
    }

    public Component getMessage(String messageKey, Object... objects) {

        String message = messages.get(messageKey);

        if (message == null) {
            return Component.text(messageKey);
        }

        return TextUtil.color(message, objects);
    }

    public void writeEmptyConfig() {

        try {

            File file = VoidSurvival.getInstance().getFile("messages.yml");

            Field[] fields = MessageKeys.class.getDeclaredFields();

            YamlConfiguration emptyConfig = new YamlConfiguration();

            for (Field field : fields) {
                emptyConfig.set(field.get(null).toString(), "");
            }

            emptyConfig.save(file);

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to write empty config: ", ex);
        }

    }

}
