package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.http.HttpResponse;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpGetStep extends HttpStep {

    @Override
    public HttpResponse<String> getHttpRequestResponse(ConfigurationInstance ci) {
        return ci.getHttpHelper().makeHttpGetRequest(args);
    }

    @Override
    public String getType() {
        return "http.get";
    }
}
