package ee.buerokratt.ruuter.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingUtils {
    private MappingUtils() {
    }

    public static JsonNode convertStringToNode(String content) {
        try {
            return new ObjectMapper().readValue(content, JsonNode.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String convertObjectToJson(Object o) {
        try {
            return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
