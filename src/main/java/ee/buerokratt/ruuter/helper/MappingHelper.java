package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MappingHelper {
    private final ObjectMapper mapper;

    public String convertObjectToString(Object o) {
        try {
            return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Map<String, String> convertMapObjectValuesToString(Map<String, Object> map) {
        return map.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
    }
}
