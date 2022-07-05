package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
public class HttpQueryArgs {
    private String url;
    private HashMap<String, Object> query;
    private HashMap<String, String> headers;
    private HashMap<String, Object> body;

    public void addHeaders(ConfigurationInstance ci) {
        ApplicationProperties.HttpPost httpPost = ci.getProperties().getHttpPost();
        if (httpPost != null && httpPost.getHeaders() != null) {
            HashMap<String, String> newHeaders = httpPost.getHeaders();
            if (headers == null) {
                setHeaders(newHeaders);
            } else {
                newHeaders.forEach(headers::putIfAbsent);
            }
        }
    }
}
