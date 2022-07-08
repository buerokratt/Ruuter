package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.domain.steps.http.HttpStepResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class ReturnStep extends ConfigurationStep {
    @JsonAlias({"return"})
    private String returnValue;

    @Override
    protected void executeStepAction(ConfigurationInstance ci) {
        ci.setReturnValue((HttpStepResult) ci.getScriptingHelper().evaluateScripts(returnValue, ci.getContext(), ci.getRequestBody(), ci.getRequestParams()));
        Integer finalResponseStatusCode = ci.getProperties().getFinalResponse().getHttpStatusCode();
        if (finalResponseStatusCode != null) {
            ci.getReturnValue().getResponse().setStatus(finalResponseStatusCode);
        }
    }

    @Override
    public String getType() {
        return "return";
    }
}
