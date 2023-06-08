package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class HttpQueryArgs {
    private String url;
    private Map<String, Object> query = new HashMap<>();
    private Map<String, Object> headers = new HashMap<>();
    private Map<String, Object> body = new HashMap<>();
    private String plaintext;
    private String contentType;
    private String originalUrl = "";

    public void addHeaders(Map<String, Object> newHeaders) {
        newHeaders.forEach(headers::putIfAbsent);
    }

    public void checkUrl(DslInstance di) {
        if (!this.originalUrl.isBlank()) this.url = originalUrl;
        if (this.url != null && this.url.matches(ScriptingHelper.SCRIPT_REGEX)) {
            this.originalUrl = this.url;
            this.url = (String) di.getScriptingHelper().evaluateScripts(this.url, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders());
        }
    }
}
