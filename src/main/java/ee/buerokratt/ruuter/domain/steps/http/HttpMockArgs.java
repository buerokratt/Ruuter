package ee.buerokratt.ruuter.domain.steps.http;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class HttpMockArgs {
    private HttpQueryArgs request;
    private Map<String, Object> response;
}
