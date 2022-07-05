package ee.buerokratt.ruuter.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String configPath;
    private Boolean stopInCaseOfException;
    private List<Integer> httpCodesAllowList;
    private DefaultAction defaultAction;
    private Logging logging;
    private IncomingRequests incomingRequests;
    private HttpPost httpPost;

    @Getter
    @Setter
    public static class Logging {
        private Boolean displayRequestContent;
        private Boolean displayResponseContent;
    }

    @Getter
    @Setter
    public static class IncomingRequests {
        private List<String> allowedMethodTypes;
    }

    @Getter
    @Setter
    public static class HttpPost {
        private HashMap<String, String> headers;
    }

    @Getter
    @Setter
    public static class DefaultAction {
        private String service;
        private HashMap<String, Object> body;
        private HashMap<String, Object> query;
    }

}
