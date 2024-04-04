package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class DeclarationStep extends DslStep {
    String version;
    String description;
    String declare;

    String method;
    String accepts;
    String returns;

    String namespace;

    List<DslField> usedFields;

    List<String> allowedFields;

    @Override
    protected void executeStepAction(DslInstance di) {
        log.info("Executing declare (%s)".formatted(this.declare));
        return;
    }

    @Override
    public String getType() {
        return "declare";
    }

    public List<String> getAllowedFields() {
        if (allowedFields == null) {
            allowedFields = usedFields.stream().map(field -> field.getField()).toList();
            log.info("Generated allowed fields for "+ declare + "(" + allowedFields.size() + ")" );
        }
        return allowedFields;
    }

}
