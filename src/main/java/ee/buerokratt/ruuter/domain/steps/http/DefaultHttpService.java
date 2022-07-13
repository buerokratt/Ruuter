package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

@Data
@NoArgsConstructor
public class DefaultHttpService {
    private String service;
    private HashMap<String, Object> body = new HashMap<>();
    private HashMap<String, Object> query;

    public void executeHttpDefaultService(ConfigurationInstance ci, String resultName) {
        ResponseEntity<Object> response = ((HttpStepResult) ci.getContext().get(resultName)).getResponse();
        body.put("statusCode", response.getStatusCodeValue());
        body.put("responseBody", ci.getMappingHelper().convertObjectToString(response.getBody()));
        body.put("failedRequestId", MDC.get("spanId"));
        ci.getConfigurationService().execute(service, "POST", body, query, ci.getRequestOrigin());
    }
}
