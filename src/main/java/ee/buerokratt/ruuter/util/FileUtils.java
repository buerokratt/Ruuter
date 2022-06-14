package ee.buerokratt.ruuter.util;


import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

public class FileUtils {

    public static final String SUFFIX_YAML = ".yaml";
    public static final String SUFFIX_YML = ".yml";

    private FileUtils() {
    }

    public static Path getFolderPath(String pathString) {
        if (StringUtils.hasLength(pathString)) {
            Path path = Paths.get(pathString);
            if (exists(path) && isDirectory(path)) {
                return path;
            }
        }
        throw new IllegalArgumentException("Failed to resolve directory: %s".formatted(pathString));
    }

    public static boolean isYmlFile(Path path) {
        String pathString = path.toString();
        return !isDirectory(path) && (pathString.endsWith(SUFFIX_YML) || pathString.endsWith(SUFFIX_YAML));
    }

    public static String getFileNameWithoutSuffix(Path path) {
        String nameWithSuffix = path.getFileName().toString();
        return nameWithSuffix.substring(0, nameWithSuffix.lastIndexOf('.'));
    }
}
