package ee.buerokratt.ruuter.domain.steps.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpStepResult {
    private HttpQueryArgs request;
    private HttpQueryResponse response;
}
