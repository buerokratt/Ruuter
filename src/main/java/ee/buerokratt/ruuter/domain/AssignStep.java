package ee.buerokratt.ruuter.domain;

import lombok.*;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AssignStep<T> extends ConfigurationStep {
    private HashMap<String, T> assign;
}
