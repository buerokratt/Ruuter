package ee.buerokratt.ruuter.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ee.buerokratt.ruuter.util.FileUtils.getFolderPath;
import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void getFolder_shouldThrowWhenPathIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> getFolderPath(null));
        assertThrows(IllegalArgumentException.class, () -> getFolderPath(""));
    }

    @Test
    void getFolder_shouldThrowOnInvalidPath() {
        assertThrows(RuntimeException.class, () -> getFolderPath("/fake/path"));
    }

    @Test
    void getFolder_shouldThrowWhenNoDirectory() {
        String path = FileUtilsTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "DslServiceTest.java";
        assertThrows(RuntimeException.class, () -> getFolderPath(path));
    }

    @Test
    void getFolderPath_shouldReturnFolderPath() {
        String path = "src/test/resources/service";

        Path folderPath = getFolderPath(path);

        assertEquals(folderPath, Paths.get(path));
        assertTrue(Files.isDirectory(folderPath));
    }
}
