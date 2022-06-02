package ee.buerokratt.ruuter.model.step.types;

import ee.buerokratt.ruuter.model.Step;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@NoArgsConstructor
public class ReturnStep extends Step {

    private String returnValue;

    @Override
    public String toString(){
        return "{name: %s, returnValue: %s}".formatted(this.getName(), returnValue);
    }
}
