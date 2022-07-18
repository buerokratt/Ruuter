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
        Map<String, Object> evaluatedBody = ci.getScriptingHelper().evaluateScripts(args.getBody(), ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, Object> evaluatedQuery = ci.getScriptingHelper().evaluateScripts(args.getQuery(), ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, Object> evaluatedHeaders = ci.getScriptingHelper().evaluateScripts(args.getHeaders(), ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, String> mappedHeaders = ci.getMappingHelper().convertMapObjectValuesToString(evaluatedHeaders);
        return ci.getHttpHelper().doPost(args.getUrl(), evaluatedBody, evaluatedQuery, mappedHeaders);
    }

    @Override
    public String getType() {
        return "http.post";
    }
}
