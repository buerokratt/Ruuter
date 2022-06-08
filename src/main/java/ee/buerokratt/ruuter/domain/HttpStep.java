package ee.buerokratt.ruuter.domain;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpStep<T> extends ConfigurationStep {
    private HttpStepArgs<T> args;

    private String result;
}
