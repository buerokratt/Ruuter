package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class DefaultHttpService {
    private String service;
    private Map<String, Object> body = new HashMap<>();
    private Map<String, Object> query = new HashMap<>();
    private Map<String, Object> headers = new HashMap<>();

    public void executeHttpDefaultService(ConfigurationInstance ci, String resultName) {
        ResponseEntity<Object> response = ((HttpStepResult) ci.getContext().get(resultName)).getResponse();
        body.put("statusCode", response.getStatusCodeValue());
        body.put("responseBody", ci.getMappingHelper().convertObjectToString(response.getBody()));
        body.put("failedRequestId", MDC.get("spanId"));
        Map<String, Map<String, Object>> evaluatedParameters = ci.getScriptingHelper().evaluateRequestParameters(ci, body, query, headers);
        ci.getConfigurationService().execute(service, "POST", evaluatedParameters.get("body"), evaluatedParameters.get("query"), ci.getMappingHelper().convertMapObjectValuesToString(evaluatedParameters.get("headers")), ci.getRequestOrigin());
    }
}
