package ee.buerokratt.ruuter.domain.steps.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpStepResult {
    private HttpQueryArgs request;
    private ResponseEntity<Object> response;
    private String requestId;
}
