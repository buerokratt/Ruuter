package ee.buerokratt.ruuter.model.step.types;

import ee.buerokratt.ruuter.model.Args;
import ee.buerokratt.ruuter.model.Step;
import lombok.*;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@NoArgsConstructor
public class HttpStep<T> extends Step {

    private Args<T> args;
    private String result;

    @Override
    public String toString(){
        return "{name: %s, result: %s, args: {%s}}".formatted(this.getName(), result, args);
    }
}
