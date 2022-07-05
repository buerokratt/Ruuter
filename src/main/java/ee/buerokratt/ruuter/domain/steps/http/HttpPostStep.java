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
        Map<String, Object> evaluatedBody = ci.getScriptingHelper().evaluateScripts(args.getBody(), ci.getContext(), ci.getRequestBody(), ci.getRequestParams());
        return ci.getHttpHelper().post(args.getUrl(), evaluatedBody, args.getQuery(), args.getHeaders());
    }

    @Override
    public String getType() {
        return "http.post";
    }
}
