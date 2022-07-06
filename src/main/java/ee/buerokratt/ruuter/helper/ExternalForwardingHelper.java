package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalForwardingHelper {
    public static final String REGION_REGEX = "\\.\\.";
    public static final String REGION_CHARACTERS = "..";

    private final ApplicationProperties properties;
    private final HttpHelper httpHelper;

    public boolean shouldForwardRequest() {
        return properties.getIncomingRequests().getExternalForwarding().getMethod() != null &&
            properties.getIncomingRequests().getExternalForwarding().getEndpoint() != null &&
            properties.getIncomingRequests().getExternalForwarding().getProceedPredicate().getHttpStatusCode() != null;
    }

    public ResponseEntity<Object> forwardRequest(Map<String, Object> requestBody, Map<String, Object> requestParams) {
        String forwardingUrl = properties.getIncomingRequests().getExternalForwarding().getEndpoint();
        Map<String, Object> params = shouldAddParams() ? requestParams : new HashMap<>();
        Map<String, Object> body = shouldAddBody(requestBody) ? requestBody : new HashMap<>();

        if (properties.getIncomingRequests().getExternalForwarding().getMethod().equals(HttpMethod.POST.name())) {
            return httpHelper.doPost(forwardingUrl, body, params, new HashMap<>());
        }
        if (properties.getIncomingRequests().getExternalForwarding().getMethod().equals(HttpMethod.GET.name())) {
            return httpHelper.doGet(forwardingUrl, params, new HashMap<>());
        }
        throw new IllegalArgumentException();
    }

    public boolean isAllowedForwardingResponse(Integer requestStatus) {
        return properties.getIncomingRequests().getExternalForwarding().getProceedPredicate().getHttpStatusCode().stream()
            .anyMatch(status -> status.contains(REGION_CHARACTERS) ?
                Integer.parseInt(status.split(REGION_REGEX)[0]) <= requestStatus && requestStatus <= Integer.parseInt(status.split(REGION_REGEX)[1]) :
                Integer.valueOf(status).equals(requestStatus)
            );
    }

    private boolean shouldAddParams() {
        return Boolean.TRUE.equals(properties.getIncomingRequests().getExternalForwarding().getParamsToPass().getGet());
    }

    private boolean shouldAddBody(Map<String, Object> requestBody) {
        return requestBody != null && Boolean.TRUE.equals(properties.getIncomingRequests().getExternalForwarding().getParamsToPass().getPost());
    }
}
