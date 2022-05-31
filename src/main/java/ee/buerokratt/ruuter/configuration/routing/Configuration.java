package ee.buerokratt.ruuter.configuration.routing;

import ee.buerokratt.ruuter.model.ConfigurationNode;
import ee.buerokratt.ruuter.service.ConfigurationNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static ee.buerokratt.ruuter.util.FileUtils.readFile;

@Slf4j
@Component
public final class Configuration {

    private static String configPath;
    private static final ConfigurationNode configurationNode = new ConfigurationNode();
    private static final ConfigurationNodeService configurationNodeService = new ConfigurationNodeService(configurationNode);

    private Configuration() {}

    public static void load() {
        try {
            File configFolder = valid(configPath);
            if (configFolder.listFiles() != null) {

                List<Path> paths = Files.walk(Paths.get(configFolder.getAbsolutePath())).filter(Files::isRegularFile).toList();
                for (Path path : paths) {
                    File file  = path.toFile();
                    if (file.isDirectory() || !file.getName().endsWith(".yml")) {
                        continue;
                    }
                    configurationNodeService.addNode(file.getName(), readFile(file.getAbsolutePath()));
                }
            } else {
                terminate("Configuration folder is empty");
            }
        } catch (Exception e) {
            terminate("Couldn't load configuration.");
        }
    }

    private static File valid(String folder) {
        File configFolder = null;
        if (folder != null) {
            configFolder = new File(folder);
            if (!configFolder.exists()) {
                terminate("No configuration file found from specified folder: " + folder);
            }
        } else {
            terminate("Missing configuration folder.");
        }
        return configFolder;
    }

    private static void terminate(String errorMsg) {
        log.error(errorMsg);
        throw new IllegalStateException(errorMsg);
    }

    @Value("${ruuter.config.path}")
    public void setConfigPath(String newPath) {
        if (configPath == null) {
            configPath = newPath;
        }
    }
}
