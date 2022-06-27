package ee.buerokratt.ruuter.domain.steps.conditional;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Condition {
    @JsonAlias({"condition"})
    private String conditionStatement;
    @JsonAlias({"next"})
    private String nextStepName;
}
