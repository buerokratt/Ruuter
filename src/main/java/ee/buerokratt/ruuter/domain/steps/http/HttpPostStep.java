package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.http.HttpResponse;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpPostStep extends HttpStep {

    @Override
    protected HttpResponse<String> getHttpRequestResponse(ConfigurationInstance ci) {
        args.addHeaders(ci.getProperties().getHttpPost().getHeaders());
        Map<String, Object> evaluatedBody = ci.getScriptingHelper().evaluateMapValues(args.getBody(), ci.getContext(), ci.getRequestBody(), ci.getRequestParams());
        return ci.getHttpHelper().makeHttpPostRequest(args, evaluatedBody);
    }

    @Override
    public String getType() {
        return "http.post";
    }
}
