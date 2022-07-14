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
public class HttpPostStep extends HttpStep {

    @Override
    protected ResponseEntity<Object> getRequestResponse(ConfigurationInstance ci) {
        args.addHeaders(ci.getProperties().getHttpPost().getHeaders());
        Map<String, Map<String, Object>> evaluatedParameters = ci.getScriptingHelper().evaluateRequestParameters(ci, args.getBody(), args.getQuery(), args.getHeaders());
        return ci.getHttpHelper().doPost(args.getUrl(), evaluatedParameters.get("body"), evaluatedParameters.get("query"), ci.getMappingHelper().convertMapObjectValuesToString(evaluatedParameters.get("headers")));
    }

    @Override
    public String getType() {
        return "http.post";
    }
}
