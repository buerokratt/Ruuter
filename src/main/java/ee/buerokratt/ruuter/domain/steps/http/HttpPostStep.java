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
public class HttpPostStep extends HttpStep {

    @Override
    protected ResponseEntity<Object> getRequestResponse(DslInstance di) {
        Map<String, Object> defaultHeaders = di.getProperties().getHttpPost().getHeaders();
        if (defaultHeaders != null && !defaultHeaders.isEmpty())
            args.addHeaders(di.getProperties().getHttpPost().getHeaders());
        String evaluatedURL = di.getScriptingHelper().evaluateScripts(args.getUrl(),di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders()).toString();
        Map<String, Object> evaluatedBody = di.getScriptingHelper().evaluateScripts(args.getBody(), di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, Object> evaluatedQuery = di.getScriptingHelper().evaluateScripts(args.getQuery(), di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, Object> evaluatedHeaders = di.getScriptingHelper().evaluateScripts(args.getHeaders(), di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        Map<String, String> mappedHeaders = di.getMappingHelper().convertMapObjectValuesToString(evaluatedHeaders);

        return di.getHttpHelper().doMethod(getMethod(), evaluatedURL,
            evaluatedQuery, evaluatedBody, mappedHeaders,
            args.getContentType(),
            "plaintext".equals(args.getContentType()) ? args.getPlaintext() : null,
            getLimit(), di,
            args.isDynamicParameters(),resultName != null && !resultName.isEmpty() );
    }

    @Override
    public String getType() {
        return "http.post";
    }

    public HttpMethod getMethod() {
        return HttpMethod.POST;
    }
}
