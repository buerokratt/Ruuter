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
        Map<String, Object> evaluatedQuery = ci.getScriptingHelper().evaluateScripts(args.getQuery(), ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, Object> evaluatedHeaders = ci.getScriptingHelper().evaluateScripts(args.getHeaders(), ci.getContext(), ci.getRequestBody(), ci.getRequestQuery(), ci.getRequestHeaders());
        Map<String, String> mappedHeaders = ci.getMappingHelper().convertMapObjectValuesToString(evaluatedHeaders);
        return ci.getHttpHelper().doGet(args.getUrl(), evaluatedQuery, mappedHeaders);
    }

    @Override
    public String getType() {
        return "http.get";
    }
}
