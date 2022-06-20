package ee.buerokratt.ruuter.domain.steps.http;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpMockArgs extends HttpArgs {
    private HashMap<String, Object> request;
    private HashMap<String, Object> response;
}
