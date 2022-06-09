package ee.buerokratt.ruuter.util;

import ee.buerokratt.ruuter.service.exception.InvalidConfigurationDirectoryException;

import java.io.File;

public class FileUtils {

    private FileUtils() {
    }

    public static File getFolder(String path) {
        if (path != null) {
            File folder = new File(path);
            if (folder.exists() && folder.isDirectory()) {
                return folder;
            }
        }
        throw new InvalidConfigurationDirectoryException(path, new IllegalArgumentException());
    }

    public static boolean isYmlFile(File file) {
        return !file.isDirectory() && file.getName().endsWith(".yml");
    }

    public static String getFileNameWithoutYmlSuffix(File file) {
        String name = file.getName();
        if (isYmlFile(file)) {
            return name.substring(0, name.length() - 4);
        }
        return file.getName();
    }
}
