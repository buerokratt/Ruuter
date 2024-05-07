package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Slf4j
public class LogStep extends DslStep{
    @JsonAlias("log")
    private String message;

    @Override
    protected void executeStepAction(DslInstance di) {
        log.info("LOG: "+ di.getScriptingHelper().evaluateScripts(message, di.getContext(), di.getRequestBody(), di.getRequestQuery(), di.getRequestHeaders()));
    }

    @Override
    public String getType() {
        return "log";
    }
}
