package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
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
public class HttpMockStep extends ConfigurationStep {
    private String call;
    private HttpMockArgs args;
    @JsonAlias({"result"})
    private String resultName;

    @Override
    public void executeStepAction(ConfigurationInstance ci) {
        ResponseEntity<Object> response = new ResponseEntity<>(args.getResponse(), null, HttpStatus.OK);
        ci.getContext().put(resultName, new HttpStepResult(args.getRequest(), response, MDC.get("spanId")));
    }

    @Override
    public String getType() {
        return "reflect.mock";
    }
}
