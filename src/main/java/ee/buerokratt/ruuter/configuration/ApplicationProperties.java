package ee.buerokratt.ruuter.configuration;

import ee.buerokratt.ruuter.domain.steps.http.DefaultHttpDsl;
import ee.buerokratt.ruuter.domain.Logging;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
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
    private DefaultHttpDsl defaultDslInCaseOfException = new DefaultHttpDsl();
    private Logging logging = new Logging();
    private IncomingRequests incomingRequests = new IncomingRequests();
    private FinalResponse finalResponse = new FinalResponse();
    private Integer maxStepRecursions;
    private CORS cors;
    private DSL dsl;
    private InternalRequests internalRequests;
    private OpenSearchConfiguration openSearchConfiguration;

    private Integer httpResponseSizeLimit;

    private Boolean allowDuplicateRequestKeys;

    @Setter
    @Getter
    public static class FinalResponse {
        private Integer dslWithResponseHttpStatusCode;
        private Integer dslWithoutResponseHttpStatusCode;
    }

    @Getter
    @Setter
    public static class IncomingRequests {
        private List<String> allowedMethodTypes;
        private ExternalForwarding externalForwarding = new ExternalForwarding();
        private Map<String, Object> headers = new HashMap<>();

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
                private Boolean origin;
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
        private Map<String, Object> headers;
    }

    @Getter
    @Setter
    public static class CORS {
        private List<String> allowedOrigins;
        private Boolean allowCredentials;
    }

    @Getter
    @Setter
    public static class DSL {
        private List<String> allowedFiletypes;
        private List<String> processedFiletypes;
        private boolean allowDslReloading;
    }

    @Getter
    @Setter
    public static class InternalRequests {
        private List<String> allowedIPs;
        private List<String> allowedURLs;
    }

    @Getter
    @Setter
    public static class OpenSearchConfiguration {
        private String url;
        private String index;
    }
}
