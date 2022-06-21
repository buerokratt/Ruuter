package ee.buerokratt.ruuter.domain.steps.http;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
public class HttpMockArgs {
    private HttpQueryArgs request;
    private HashMap<String, Object> response;
}
