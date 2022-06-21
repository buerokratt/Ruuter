package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.databind.JsonNode;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.util.MappingUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.http.HttpResponse;

import static ee.buerokratt.ruuter.util.HttpUtils.makeHttpPostRequest;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpPostStep extends HttpStep {
    @Override
    public void execute(ConfigurationInstance configurationInstance) {
        super.execute(configurationInstance);
        HttpResponse<String> response = makeHttpPostRequest(args);
        JsonNode responseBody = response.body().isEmpty() ? null : MappingUtils.convertStringToNode(response.body());
        HttpQueryResponse httpQueryResponse = new HttpQueryResponse(responseBody, response.headers().map(), response.statusCode());
        configurationInstance.getContext().put(resultName, new HttpStepResult(args, httpQueryResponse));
    }
}
