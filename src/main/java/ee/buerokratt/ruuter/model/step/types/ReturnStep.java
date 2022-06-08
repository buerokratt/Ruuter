package ee.buerokratt.ruuter.model.step.types;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.model.Step;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ReturnStep extends Step {

    @JsonAlias({"return"})
    private String returnValue;
}
