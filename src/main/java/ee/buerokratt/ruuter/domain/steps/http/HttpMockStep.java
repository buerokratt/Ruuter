package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
        HttpQueryResponse httpQueryResponse = new HttpQueryResponse(ci.getMappingHelper().convertMapToNode(args.getResponse()), null, 200, null);
        ci.getContext().put(resultName, new HttpStepResult(args.getRequest(), httpQueryResponse));
    }

    @Override
    public String getType() {
        return "reflect.mock";
    }
}
