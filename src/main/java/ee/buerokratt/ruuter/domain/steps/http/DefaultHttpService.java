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
        Map<String, Object> evaluatedBody = ci.getScriptingHelper().evaluateScripts(body, ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, Object> evaluatedQuery = ci.getScriptingHelper().evaluateScripts(query, ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, Object> evaluatedHeaders = ci.getScriptingHelper().evaluateScripts(headers, ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, String> mappedHeaders = ci.getMappingHelper().convertMapObjectValuesToString(evaluatedHeaders);
        ci.getConfigurationService().execute(service, "POST", evaluatedBody, evaluatedQuery, mappedHeaders, ci.getRequestOrigin());
    }
}
