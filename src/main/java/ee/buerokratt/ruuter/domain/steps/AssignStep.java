package ee.buerokratt.ruuter.domain.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AssignStep<T> extends ConfigurationStep {
    private HashMap<String, T> assign;
}
