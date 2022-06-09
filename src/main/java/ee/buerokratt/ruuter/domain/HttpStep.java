package ee.buerokratt.ruuter.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.buerokratt.ruuter.service.exception.InvalidHttpRequestException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static ee.buerokratt.ruuter.util.HttpUtils.getHttpRequest;
import static ee.buerokratt.ruuter.util.HttpUtils.sendHttpRequest;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpStep extends ConfigurationStep {
    private HttpStepArgs args;
    private String call;
    private String result;

    @Override
    public void execute(ConfigurationInstance configurationInstance) {
        ObjectMapper mapper = new ObjectMapper();
        if (call.equals("http.get")) {
            try {
                HttpRequest request = getHttpRequest(args);
                HttpResponse<String> response = sendHttpRequest(request);
                JsonNode body = mapper.readValue(response.body(), JsonNode.class);
                HttpRequestResponse httpResponse = new HttpRequestResponse(body, response.headers().toString(), response.statusCode());
                configurationInstance.getContext().put(result, httpResponse);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InvalidHttpRequestException(getName(), e);
            }
        } else {
            throw new InvalidHttpRequestException(getName(), new IllegalArgumentException());
        }
        super.execute(configurationInstance);
    }
}
