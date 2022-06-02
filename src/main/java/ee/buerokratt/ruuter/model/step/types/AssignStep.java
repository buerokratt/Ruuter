package ee.buerokratt.ruuter.model.step.types;

import ee.buerokratt.ruuter.model.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import java.util.HashMap;

@Getter
@Setter
@Service
@NoArgsConstructor
public class AssignStep<T> extends Step {

    private HashMap<String, T> assign;

    @Override
    public String toString(){
        return "{name: %s, assign: %s}".formatted(this.getName(), assign);
    }
}
