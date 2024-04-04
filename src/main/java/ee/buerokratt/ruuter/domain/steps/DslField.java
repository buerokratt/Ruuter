package ee.buerokratt.ruuter.domain.steps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DslField {
    private String field;
    private String type;
    private String description;
}
