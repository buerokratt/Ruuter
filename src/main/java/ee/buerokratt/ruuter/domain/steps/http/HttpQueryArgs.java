package ee.buerokratt.ruuter.domain.steps.http;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class HttpQueryArgs {
    private String url;
    private Map<String, Object> query;
    private Map<String, String> headers;
    private Map<String, Object> body;

    public void addHeaders(Map<String, String> newHeaders) {
        if (headers == null) {
            setHeaders(newHeaders);
        } else {
            newHeaders.forEach(headers::putIfAbsent);
        }
    }
}
