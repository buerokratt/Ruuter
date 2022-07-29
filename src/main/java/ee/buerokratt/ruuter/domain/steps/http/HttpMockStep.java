package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpMockStep extends DslStep {
    private String call;
    private HttpMockArgs args;
    @JsonAlias({"result"})
    private String resultName;

    @Override
    public void executeStepAction(DslInstance di) {
        ResponseEntity<Object> response = new ResponseEntity<>(args.getResponse(), null, HttpStatus.OK);
        di.getContext().put(resultName, new HttpStepResult(args.getRequest(), response, MDC.get("spanId")));
    }

    @Override
    public String getType() {
        return "reflect.mock";
    }
}
