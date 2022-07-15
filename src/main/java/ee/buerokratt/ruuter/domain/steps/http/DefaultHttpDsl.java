package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

@Data
@NoArgsConstructor
public class DefaultHttpDsl {
    private String dsl;
    private HashMap<String, Object> body = new HashMap<>();
    private HashMap<String, Object> query;
    private String requestType;

    public void executeHttpDefaultDsl(DslInstance di, String resultName) {
        ResponseEntity<Object> response = ((HttpStepResult) di.getContext().get(resultName)).getResponse();
        body.put("statusCode", response.getStatusCodeValue());
        body.put("responseBody", di.getMappingHelper().convertObjectToString(response.getBody()));
        body.put("failedRequestId", MDC.get("spanId"));
        di.getDslService().execute(dsl, requestType, body, query, di.getRequestOrigin());
    }
}
