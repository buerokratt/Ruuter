package ee.buerokratt.ruuter.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String configPath;
    private Boolean stopInCaseOfException;
    private List<Integer> httpCodesAllowList;
    private HttpPost httpPost = new HttpPost();
    private DefaultAction defaultAction = new DefaultAction();
    private Logging logging = new Logging();
    private IncomingRequests incomingRequests = new IncomingRequests();
    private FinalResponse finalResponse = new FinalResponse();

    @Getter
    @Setter
    public static class Logging {
        private Boolean displayRequestContent;
        private Boolean displayResponseContent;
    }

    @Setter
    @Getter
    public static class FinalResponse {
        private Integer httpStatusCode;
    }

    @Getter
    @Setter
    public static class IncomingRequests {
        private List<String> allowedMethodTypes;
        private ExternalForwarding externalForwarding = new ExternalForwarding();

        @Getter
        @Setter
        public static class ExternalForwarding {
            private String method;
            private String endpoint;
            private ParamsToPass paramsToPass = new ParamsToPass();
            private ProceedPredicate proceedPredicate = new ProceedPredicate();

            @Getter
            @Setter
            public static class ParamsToPass {
                private Boolean get;
                private Boolean post;
                private Boolean headers;
            }

            @Getter
            @Setter
            public static class ProceedPredicate {
                private List<String> httpStatusCode;
            }
        }
    }

    @Getter
    @Setter
    public static class HttpPost {
        private Map<String, String> headers;
    }

    @Getter
    @Setter
    public static class DefaultAction {
        private String service;
        private Map<String, Object> body;
        private Map<String, Object> query;
    }
}
