package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

import java.util.HashMap;

@Data
@NoArgsConstructor
public class DefaultAction {
    private String service;
    private HashMap<String, Object> body;
    private HashMap<String, Object> query;

    public void executeDefaultAction(ConfigurationInstance ci, String resultName) {
        HttpQueryResponse response = ((HttpStepResult) ci.getContext().get(resultName)).getResponse();
        body = body == null ? new HashMap<>() : body;
        body.put("statusCode", response.getStatus().toString());
        body.put("responseBody", ci.getMappingHelper().convertObjectToString(response.getBody()));
        body.put("failedRequestId", MDC.get("spanId"));
        ci.getConfigurationService().execute(service, body, query, ci.getRequestOrigin());
    }
}
