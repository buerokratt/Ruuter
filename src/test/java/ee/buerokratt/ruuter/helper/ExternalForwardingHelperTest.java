package ee.buerokratt.ruuter.helper;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WireMockTest
class ExternalForwardingHelperTest {

    /*
    private final ApplicationProperties properties = new ApplicationProperties();
    private final HttpHelper httpHelper = new HttpHelper();
    private ApplicationProperties spyProperties;
    private HttpHelper spyHttpHelper;
    private ExternalForwardingHelper helper;

    @BeforeEach
    protected void initializeObjects() {
        spyProperties = spy(properties);
        spyHttpHelper = spy(httpHelper);
        helper = new ExternalForwardingHelper(spyProperties, spyHttpHelper);
    }

    @Test
    void shouldForwardRequest_shouldReturnTrue() {
        ApplicationProperties.IncomingRequests.ExternalForwarding externalForwarding = new ApplicationProperties.IncomingRequests.ExternalForwarding() {{
            setMethod("method");
            setEndpoint("endpoint");
            setProceedPredicate(new ProceedPredicate() {{
                setHttpStatusCode(new LinkedList<>() {{add("item");}});
            }});
        }};
        ApplicationProperties.IncomingRequests incomingRequests = new ApplicationProperties.IncomingRequests();
        incomingRequests.setExternalForwarding(externalForwarding);

        when(spyProperties.getIncomingRequests()).thenReturn(incomingRequests);

        assertTrue(helper.shouldForwardRequest());
    }

    @Test
    void shouldForwardRequest_shouldReturnFalse() {
        ApplicationProperties.IncomingRequests.ExternalForwarding externalForwarding = new ApplicationProperties.IncomingRequests.ExternalForwarding();
        ApplicationProperties.IncomingRequests incomingRequests = new ApplicationProperties.IncomingRequests();
        incomingRequests.setExternalForwarding(externalForwarding);

        when(spyProperties.getIncomingRequests()).thenReturn(incomingRequests);

        assertFalse(helper.shouldForwardRequest());
    }

    @Test
    void forwardRequest_shouldSendPostRequestWhenCorrectMethodType(WireMockRuntimeInfo wireMockRuntimeInfo) {
        ApplicationProperties.IncomingRequests.ExternalForwarding externalForwarding = new ApplicationProperties.IncomingRequests.ExternalForwarding() {{
            setMethod("POST");
            setEndpoint("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
            setParamsToPass(new ApplicationProperties.IncomingRequests.ExternalForwarding.ParamsToPass() {{
                setGet(true);
                setPost(true);
            }});
        }};
        ApplicationProperties.IncomingRequests incomingRequests = new ApplicationProperties.IncomingRequests();
        incomingRequests.setExternalForwarding(externalForwarding);
        Map<String, Object> requestBody = new HashMap<>() {{
            put("key", "value");
        }};
        Map<String, Object> requestQuery = new HashMap<>() {{
            put("key2", "value2");
        }};
        Map<String, String> requestHeaders = new HashMap<>() {{
            put("key3", "value3");
        }};

        when(spyProperties.getIncomingRequests()).thenReturn(incomingRequests);
        helper.forwardRequest("originaldsl", requestBody, requestQuery, requestHeaders);

        verify(spyHttpHelper, times(1)).doPost(externalForwarding.getEndpoint(), requestBody, requestQuery, new HashMap<>());
    }

    @Test
    void forwardRequest_shouldSendGetRequestWhenCorrectMethodType(WireMockRuntimeInfo wireMockRuntimeInfo) {
        ApplicationProperties.IncomingRequests.ExternalForwarding externalForwarding = new ApplicationProperties.IncomingRequests.ExternalForwarding() {{
            setMethod("get");
            setEndpoint("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
            setParamsToPass(new ApplicationProperties.IncomingRequests.ExternalForwarding.ParamsToPass() {{
                setGet(true);
            }});
        }};
        ApplicationProperties.IncomingRequests incomingRequests = new ApplicationProperties.IncomingRequests();
        incomingRequests.setExternalForwarding(externalForwarding);
        Map<String, Object> requestQuery = new HashMap<>() {{
            put("key", "value");
        }};
        Map<String, String> requestHeaders = new HashMap<>() {{
            put("key3", "value3");
        }};

        when(spyProperties.getIncomingRequests()).thenReturn(incomingRequests);
        helper.forwardRequest("originaldsl",new HashMap<>(), requestQuery, requestHeaders);

        verify(spyHttpHelper, times(1)).doGet(externalForwarding.getEndpoint(), requestQuery, new HashMap<>());
    }

    @Test
    void forwardRequest_shouldThrowExceptionWhenIncorrectMethodType() {
        ApplicationProperties.IncomingRequests.ExternalForwarding externalForwarding = new ApplicationProperties.IncomingRequests.ExternalForwarding() {{
            setMethod("wrongMethodType");
            setEndpoint("endpoint");
        }};
        ApplicationProperties.IncomingRequests incomingRequests = new ApplicationProperties.IncomingRequests();
        incomingRequests.setExternalForwarding(externalForwarding);

        when(spyProperties.getIncomingRequests()).thenReturn(incomingRequests);

        assertThrows(IllegalArgumentException.class, () -> helper.forwardRequest("originaldsl",new HashMap<>(), new HashMap<>(), new HashMap<>()));
    }

    @Test
    void isAllowedForwardingResponse_shouldReturnTrueWhenResponseStatusCodeInWhitelist() {
        ApplicationProperties.IncomingRequests.ExternalForwarding externalForwarding = new ApplicationProperties.IncomingRequests.ExternalForwarding() {{
            setProceedPredicate(new ProceedPredicate() {{
                setHttpStatusCode(new LinkedList<>() {{add(String.valueOf(HttpStatus.OK.value()));}});
            }});
        }};
        ApplicationProperties.IncomingRequests incomingRequests = new ApplicationProperties.IncomingRequests();
        incomingRequests.setExternalForwarding(externalForwarding);

        when(spyProperties.getIncomingRequests()).thenReturn(incomingRequests);

        assertTrue(helper.isAllowedForwardingResponse(HttpStatus.OK.value()));
    }

    @Test
    void isAllowedForwardingResponse_shouldReturnFalseWhenResponseStatusCodeNotInWhitelist() {
        ApplicationProperties.IncomingRequests.ExternalForwarding externalForwarding = new ApplicationProperties.IncomingRequests.ExternalForwarding() {{
            setProceedPredicate(new ProceedPredicate() {{
                setHttpStatusCode(new LinkedList<>() {{add("201..302");}});
            }});
        }};
        ApplicationProperties.IncomingRequests incomingRequests = new ApplicationProperties.IncomingRequests();
        incomingRequests.setExternalForwarding(externalForwarding);

        when(spyProperties.getIncomingRequests()).thenReturn(incomingRequests);

        assertFalse(helper.isAllowedForwardingResponse(HttpStatus.OK.value()));
    }

     */
}
