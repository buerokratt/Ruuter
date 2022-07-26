package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class MappingHelperTest {

    @Test
    void convertObjectToString_shouldConvertObjectToString() throws JsonProcessingException {
        Object object = new HashMap<>() {{
            put("key", "value");
        }};
        ObjectMapper objectMapper = new ObjectMapper();
        MappingHelper mappingHelper = new MappingHelper(objectMapper);
        assertEquals(objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(object), mappingHelper.convertObjectToString(object));
    }

    @Test
    void convertObjectToStringShouldThrowExceptionWhenInputIsIncorrect() {
        Object object = new Object();
        ObjectMapper objectMapper = new ObjectMapper();
        MappingHelper mappingHelper = new MappingHelper(objectMapper);
        assertThrows(IllegalArgumentException.class, () -> mappingHelper.convertObjectToString(object));
    }
}
