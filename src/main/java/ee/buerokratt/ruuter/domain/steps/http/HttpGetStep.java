package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.databind.JsonNode;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.HttpRequestResponse;
import ee.buerokratt.ruuter.util.MappingUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.http.HttpResponse;

import static ee.buerokratt.ruuter.util.HttpUtils.makeHttpRequest;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpGetStep extends HttpStep {
    @Override
    public void execute(ConfigurationInstance configurationInstance) {
        super.execute(configurationInstance);
        HttpResponse<String> response = makeHttpRequest(args);
        JsonNode body = MappingUtils.convertStringToNode(response.body());
        configurationInstance.getContext().put(resultName, new HttpRequestResponse(body, response.headers().toString(), response.statusCode()));
    }
}
