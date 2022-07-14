package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpGetStep extends HttpStep {

    @Override
    public ResponseEntity<Object> getRequestResponse(ConfigurationInstance ci) {
        Map<String, Map<String, Object>> evaluatedParameters = ci.getScriptingHelper().evaluateRequestParameters(ci, args.getBody(), args.getQuery(), args.getHeaders());
        return ci.getHttpHelper().doGet(args.getUrl(), evaluatedParameters.get("query"), ci.getMappingHelper().convertMapObjectValuesToString(evaluatedParameters.get("headers")));
    }

    @Override
    public String getType() {
        return "http.get";
    }
}
