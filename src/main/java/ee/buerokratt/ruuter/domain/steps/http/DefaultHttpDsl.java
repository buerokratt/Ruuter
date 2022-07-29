package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class DefaultHttpDsl {
    private String dsl;
    private Map<String, Object> body = new HashMap<>();
    private Map<String, Object> query = new HashMap<>();
    private Map<String, Object> headers = new HashMap<>();
    private String requestType;

    public void executeHttpDefaultDsl(DslInstance di, String resultName) {
        ResponseEntity<Object> response = ((HttpStepResult) di.getContext().get(resultName)).getResponse();
        body.put("statusCode", response.getStatusCodeValue());
        body.put("responseBody", di.getMappingHelper().convertObjectToString(response.getBody()));
        body.put("failedRequestId", MDC.get("spanId"));
        Map<String, Object> evaluatedBody = di.getScriptingHelper().evaluateScripts(body, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, Object> evaluatedQuery = di.getScriptingHelper().evaluateScripts(query, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, Object> evaluatedHeaders = di.getScriptingHelper().evaluateScripts(headers, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, String> mappedHeaders = di.getMappingHelper().convertMapObjectValuesToString(evaluatedHeaders);
        di.getDslService().execute(dsl, requestType, evaluatedBody, evaluatedQuery, mappedHeaders, di.getRequestOrigin());
    }
}
