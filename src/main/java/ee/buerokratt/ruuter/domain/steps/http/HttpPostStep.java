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
        args.addHeaders(di.getProperties().getHttpPost().getHeaders());
        Map<String, Object> evaluatedBody = di.getScriptingHelper().evaluateScripts(args.getBody(), di.getContext(), di.getRequestBody(), di.getRequestParams());
        return di.getHttpHelper().doPost(args.getUrl(), evaluatedBody, args.getQuery(), args.getHeaders());
    }

    @Override
    public String getType() {
        return "http.post";
    }
}
