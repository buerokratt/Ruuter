package ee.buerokratt.ruuter.domain.steps.http;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class HttpQueryArgs {
    private String url;
    private Map<String, Object> query;
    private Map<String, Object> headers = new HashMap<>();
    private Map<String, Object> body;

    public void addHeaders(Map<String, Object> newHeaders) {
        newHeaders.forEach(headers::putIfAbsent);
    }
}
