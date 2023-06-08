package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.DslInstance;
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
    protected ResponseEntity<Object> getRequestResponse(DslInstance di) {
        Map<String, Object> defaultHeaders = di.getProperties().getHttpPost().getHeaders();
        if (defaultHeaders != null && !defaultHeaders.isEmpty())
            args.addHeaders(di.getProperties().getHttpPost().getHeaders());
        Map<String, Object> evaluatedBody = di.getScriptingHelper().evaluateScripts(args.getBody(), di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, Object> evaluatedQuery = di.getScriptingHelper().evaluateScripts(args.getQuery(), di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, Object> evaluatedHeaders = di.getScriptingHelper().evaluateScripts(args.getHeaders(), di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, String> mappedHeaders = di.getMappingHelper().convertMapObjectValuesToString(evaluatedHeaders);

        if ("plaintext".equals(args.getContentType()))
            return di.getHttpHelper().doPostPlaintext(args.getUrl(), evaluatedBody, evaluatedQuery, mappedHeaders, args.getPlaintext());
        else
            return di.getHttpHelper().doPost(args.getUrl(), evaluatedBody, evaluatedQuery, mappedHeaders);

    }

    @Override
    public String getType() {
        return "http.post";
    }
}
