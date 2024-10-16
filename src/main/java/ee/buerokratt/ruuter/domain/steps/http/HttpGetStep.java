package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpGetStep extends HttpStep {

    @Override
    public ResponseEntity<Object> getRequestResponse(DslInstance di) {
        String evaluatedURL = di.getScriptingHelper().evaluateScripts(args.getUrl(),di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders()).toString();
        Map<String, Object> evaluatedQuery = di.getScriptingHelper().evaluateScripts(args.getQuery(), di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, Object> evaluatedHeaders = di.getScriptingHelper().evaluateScripts(args.getHeaders(), di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, String> mappedHeaders = di.getMappingHelper().convertMapObjectValuesToString(evaluatedHeaders);
        return di.getHttpHelper().doMethod(HttpMethod.GET, evaluatedURL, evaluatedQuery, null, mappedHeaders, args.getContentType() , null, getLimit(), di, args.isDynamicParameters(), resultName != null && !resultName.isEmpty() );
    }

    @Override
    public String getType() {
        return "http.get";
    }
}
