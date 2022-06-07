package ee.buerokratt.ruuter.model.step.types;

import ee.buerokratt.ruuter.model.Args;
import ee.buerokratt.ruuter.model.Step;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HttpStep<T> extends Step {

    private Args<T> args;
    private String call;
    private String result;

}
