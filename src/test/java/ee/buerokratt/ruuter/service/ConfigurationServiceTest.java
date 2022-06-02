package ee.buerokratt.ruuter.service;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationServiceTest {

    @Test
    void getFolder_shouldThrowWhenPathIsEmpty() {
        assertThrows(IllegalStateException.class, () -> ConfigurationService.getFolder(null));
        assertThrows(IllegalStateException.class, () -> ConfigurationService.getFolder(""));
    }

    @Test
    void getFolder_shouldThrowOnInvalidPath() {
        assertThrows(IllegalStateException.class, () -> ConfigurationService.getFolder("/fake/path"));
    }

    @Test
    void getFolder_shouldThrowWhenNoDirectory() {
        String path = ConfigurationServiceTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "ConfigurationServiceTest.java";
        assertThrows(IllegalStateException.class, () -> ConfigurationService.getFolder(path));
    }

    @Test
    void getFolder_shouldReturnFolder() {
        String path = ConfigurationServiceTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        assertEquals(ConfigurationService.getFolder(path), new File(path));
    }
}
