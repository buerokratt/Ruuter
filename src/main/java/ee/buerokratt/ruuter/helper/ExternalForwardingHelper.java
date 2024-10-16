package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.helper.exception.InvalidHttpMethodTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Locale;
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

    public ResponseEntity<Object> forwardRequest(String dsl, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, DslInstance di) {
        return forwardRequest(dsl, requestBody, requestQuery,requestHeaders, this.getClass().getName(), di);
    }

    public ResponseEntity<Object> forwardRequest(String dsl, Map<String, Object> requestBody, Map<String, Object> requestQuery, Map<String, String> requestHeaders, String contentType,
                                                 DslInstance di) {
        String methodType = properties.getIncomingRequests().getExternalForwarding().getMethod().toUpperCase(Locale.ROOT);
        Map<String, Object> query = shouldAddQuery(requestQuery) ? requestQuery : new HashMap<>();
        Map<String, Object> body = shouldAddBody(requestBody) ? requestBody : new HashMap<>();
        Map<String, String> headers = shouldAddHeaders(requestHeaders) ? requestHeaders : new HashMap<>();

        if (shouldAddOrigin()) {
            requestQuery.put("originalEndpoint", dsl);
            if (requestHeaders.containsKey("host"))
                requestQuery.put("originalHost", requestHeaders.get("host"));
        }

        MultiValueMap<String, String> multibody = new LinkedMultiValueMap<>();
        requestQuery.forEach((s, o) -> multibody.add(s, (String) o));

        String forwardingUrl = UriComponentsBuilder.fromUriString(properties.getIncomingRequests().getExternalForwarding().getEndpoint())
            .queryParams(multibody)
            .toUriString();

        if (methodType.equals(HttpMethod.POST.name())) {
            return httpHelper.doMethod(HttpMethod.POST,forwardingUrl, query, body, headers, contentType, null, null, di, false, true );
        }
        if (methodType.equals(HttpMethod.GET.name())) {
            return httpHelper.doMethod(HttpMethod.GET, forwardingUrl,  query,null, headers, null, null, null, di, false, true                        );
        }
        throw new InvalidHttpMethodTypeException(methodType);
    }

    public boolean isAllowedForwardingResponse(Integer responseStatus) {
        return properties.getIncomingRequests().getExternalForwarding().getProceedPredicate().getHttpStatusCode().stream()
            .anyMatch(status -> status.contains(REGION_CHARACTERS) ?
                Integer.parseInt(status.split(REGION_REGEX)[0]) <= responseStatus && responseStatus <= Integer.parseInt(status.split(REGION_REGEX)[1]) :
                Integer.valueOf(status).equals(responseStatus)
            );
    }

    private boolean shouldAddQuery(Map<String, Object> requestParams) {
        return requestParams != null && Boolean.TRUE.equals(properties.getIncomingRequests().getExternalForwarding().getParamsToPass().getGet());
    }

    private boolean shouldAddBody(Map<String, Object> requestBody) {
        return requestBody != null && Boolean.TRUE.equals(properties.getIncomingRequests().getExternalForwarding().getParamsToPass().getPost());
    }

    private boolean shouldAddHeaders(Map<String, String> requestHeaders) {
        return requestHeaders != null && Boolean.TRUE.equals(properties.getIncomingRequests().getExternalForwarding().getParamsToPass().getHeaders());
    }

    private boolean shouldAddOrigin() {
        return Boolean.TRUE.equals(properties.getIncomingRequests().getExternalForwarding().getParamsToPass().getOrigin());
    }

}
