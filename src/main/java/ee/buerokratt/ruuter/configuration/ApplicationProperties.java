package ee.buerokratt.ruuter.configuration;

import ee.buerokratt.ruuter.domain.steps.http.HttpDefaultAction;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String configPath;
    private Boolean stopInCaseOfException;
    private List<Integer> httpCodesAllowList;
    private HttpDefaultAction httpDefaultAction;
    private Logging logging;
    private IncomingRequests incomingRequests;

    @Setter
    @Getter
    public static class Logging {
        private Boolean displayRequestContent;
        private Boolean displayResponseContent;
    }

    @Getter
    @Setter
    public static class IncomingRequests {
        private List<String> allowedMethodTypes;
    }
}
