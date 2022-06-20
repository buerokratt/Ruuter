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
public class HttpQueryArgs extends HttpArgs {
    private String url;
    private HashMap<String, Object> query;
    private HashMap<String, String> headers;
    private HashMap<String, Object> body;
}
