package ee.buerokratt.ruuter.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestResponse {
    private JsonNode body;
    private String headers;
    private Integer status;
}
