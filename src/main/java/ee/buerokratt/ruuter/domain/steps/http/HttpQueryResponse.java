package ee.buerokratt.ruuter.domain.steps.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpQueryResponse {
    private Object body;
    private Map<String, List<String>> headers;
    private Integer status;
    private String requestId;
}
