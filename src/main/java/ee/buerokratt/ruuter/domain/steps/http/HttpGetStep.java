package ee.buerokratt.ruuter.domain.steps.http;

import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.ResponseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpGetStep extends HttpStep {

    @Override
    public ResponseEntity<Object> getRequestResponse(DslInstance di) {
        return di.getHttpHelper().doGet(args.getUrl(), args.getQuery(), args.getHeaders());
    }

    @Override
    public String getType() {
        return "http.get";
    }
}
