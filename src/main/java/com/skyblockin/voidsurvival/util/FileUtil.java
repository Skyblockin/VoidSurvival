package com.skyblockin.voidsurvival.util;

import com.skyblockin.voidsurvival.VoidSurvival;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static File createOrGetFile(String fileName) {

        File file = new File(VoidSurvival.getInstance().getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                if (file.getParentFile().mkdirs() && file.createNewFile()) {
                    return file;
                } else {
                    return null;
                }
            } catch (Exception ex) {
                return null;
            }
        }

        return file;
    }

    public static List<File> listFiles(String directory) {

        File actualDirectory = new File(VoidSurvival.getInstance().getDataFolder(), directory);

        return listFiles(actualDirectory);
    }

    public static List<File> listFiles(File directory) {

        if (!directory.exists()) {
            if (directory.mkdirs()) {
                VoidSurvival.logInfo("The %s folder was missing, so it was created.", directory.getName());
            } else {
                VoidSurvival.logError("The " + directory.getName() + " folder was missing, and it could not be created. Is the plugin folder read-only?");
            }

            return new ArrayList<>();
        }

        File[] files = directory.listFiles();

        if (files == null) return new ArrayList<>();

        return List.of(files);
    }

}
