package ee.buerokratt.ruuter.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String configPath;
    private boolean stopProcessingUnRespondingService;
    private Logging logging = new Logging();
    private IncomingRequests incomingRequests = new IncomingRequests();

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
