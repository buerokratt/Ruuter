package ee.buerokratt.ruuter.model.step.types;

import ee.buerokratt.ruuter.model.Step;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AssignStep<T> extends Step {

    private HashMap<String, T> assign;

    @Override
    public String toString(){
        return "{name: %s, assign: %s}".formatted(this.getName(), assign);
    }
}
