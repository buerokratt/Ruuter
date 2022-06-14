package ee.buerokratt.ruuter.utils;

import org.junit.jupiter.api.Test;

import java.io.File;

import static ee.buerokratt.ruuter.util.FileUtils.getFolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUtilsTest {

    @Test
    void getFolder_shouldThrowWhenPathIsEmpty() {
        assertThrows(RuntimeException.class, () -> getFolder(null));
        assertThrows(RuntimeException.class, () -> getFolder(""));
    }

    @Test
    void getFolder_shouldThrowOnInvalidPath() {
        assertThrows(RuntimeException.class, () -> getFolder("/fake/path"));
    }

    @Test
    void getFolder_shouldThrowWhenNoDirectory() {
        String path = FileUtilsTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "ConfigurationServiceTest.java";
        assertThrows(RuntimeException.class, () -> getFolder(path));
    }

    @Test
    void getFolder_shouldReturnFolder() {
        String path = "src/test/resources/service";
        assertEquals(getFolder(path), new File(path));
    }
}
