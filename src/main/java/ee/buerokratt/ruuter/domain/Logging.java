package ee.buerokratt.ruuter.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Logging {
    @JsonAlias("request")
    private Boolean displayRequestContent;

    @JsonAlias("response")
    private Boolean displayResponseContent;

    private Boolean meaningfulErrors;

    private Boolean printStackTrace;
}
