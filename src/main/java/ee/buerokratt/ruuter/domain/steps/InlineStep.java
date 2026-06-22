package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * DSL Step type for specific routines that are
 * separate from business logic and for which
 * it makes sense to use Java level optimizations
 */

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class InlineStep extends DslStep {

    private String inline;

    @JsonAlias({"result"})
    private String resultName;

    private Map<String, Object> args;

    @Override
    protected void executeStepAction(DslInstance di) {

        Object resultValue;

        switch (inline) {
            case "extAuth":
                resultValue = isExtAuth(di);
                break;
            default:
                resultValue = null;
        }

        di.getContext().put(resultName, resultValue);
    }

    @Override
    public String getType() {
        return "inline";
    }

    private Boolean isExtAuth(DslInstance di) {
        log.info("INCOMING name: "+ di.getName());

        List<String> list = di.getProperties().getExternalAuthAllowed();
        if (list == null || list.isEmpty())
            return false;

        return list.contains(di.getName());
    }

}
