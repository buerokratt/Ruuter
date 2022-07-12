package ee.buerokratt.ruuter.configuration;

import ee.buerokratt.ruuter.domain.steps.http.DefaultHttpService;
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
    private DefaultHttpService defaultServiceInCaseOfException = new DefaultHttpService();
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
}
