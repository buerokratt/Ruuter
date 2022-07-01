package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MappingHelper {
    private final ObjectMapper mapper;

    public JsonNode convertStringToNode(String content) {
        try {
            return mapper.readValue(content, JsonNode.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String convertObjectToString(Object o) {
        try {
            return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public JsonNode convertMapToNode(Map<String, Object> response) {
        return mapper.convertValue(response, JsonNode.class);
    }
}
