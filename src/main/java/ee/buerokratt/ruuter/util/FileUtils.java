package ee.buerokratt.ruuter.util;

import org.ini4j.Ini;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static boolean isFiletype(Path path, List<String> filetypes) {
        if (isDirectory(path)) return true;
        for (String filetype : filetypes)
            if (path.toString().endsWith(filetype)) return true;
        return false;
    }

    public static String getFileNameWithoutSuffix(Path path) {
        String nameWithSuffix = path.getFileName().toString();
        return nameWithSuffix.substring(0, nameWithSuffix.lastIndexOf('.'));
    }

    public static String getFileNameWithPathWithoutSuffix(Path path) {
        String fullPath = path.toAbsolutePath().toString();
        fullPath = fullPath.substring(fullPath.indexOf('/', fullPath.indexOf('/', fullPath.indexOf('/')+1)+1)+1, fullPath.lastIndexOf('.'));
        return fullPath;
    }

    public static String getGuardWithPath(Path path) {
        String fullPath = getFileNameWithPathWithoutSuffix(path);
        String guard = fullPath.contains("/") ? fullPath.substring(0, fullPath.lastIndexOf("/")) : fullPath;
        return guard;
    }

    public static Map<String, Map<String, String>> parseIniFile(File fileToParse) throws IOException {
        Ini ini = new Ini(fileToParse);
        return ini.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static boolean isGuard(Path path) {
        return path.endsWith(".guard");
    }
}
