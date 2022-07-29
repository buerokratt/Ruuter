package ee.buerokratt.ruuter.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Logging {
    private Boolean displayRequestContent;
    private Boolean displayResponseContent;
}
