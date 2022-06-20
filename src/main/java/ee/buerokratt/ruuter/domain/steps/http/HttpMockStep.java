package ee.buerokratt.ruuter.domain.steps.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.ConfigurationStep;
import ee.buerokratt.ruuter.util.MappingUtils;
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
    public void execute(ConfigurationInstance configurationInstance) {
        super.execute(configurationInstance);
        HttpQueryResponse httpQueryResponse = new HttpQueryResponse(MappingUtils.convertMapToNode(args.getResponse()), null, 200);
        configurationInstance.getContext().put(resultName, new HttpStepResult(args, httpQueryResponse));
    }
}
