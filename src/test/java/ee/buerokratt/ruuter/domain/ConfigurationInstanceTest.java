package ee.buerokratt.ruuter.domain;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.ConfigurationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.sleuth.Tracer;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest
@ExtendWith(MockitoExtension.class)
class ConfigurationInstanceTest {

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private Tracer tracer;

    @Test
    void execute_shouldAddGlobalHeadersToRequestHeaders() {
        ConfigurationInstance ci = new ConfigurationInstance(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), "",
            configurationService, properties, scriptingHelper, mappingHelper, httpHelper, tracer);
        ApplicationProperties.IncomingRequests incomingRequests = new ApplicationProperties.IncomingRequests() {{
            setHeaders(new HashMap<>() {{
                put("header", "headerValue");
            }});
        }};
        Map<String, String> incomingRequestsHeaders = new HashMap<>() {{
            put("header", "headerValue");
        }};

        when(properties.getIncomingRequests()).thenReturn(incomingRequests);
        when(scriptingHelper.evaluateScripts(anyMap(), anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(incomingRequests.getHeaders());
        when(mappingHelper.convertMapObjectValuesToString(anyMap())).thenReturn(incomingRequestsHeaders);
        ci.execute("random-name");

        assertEquals(incomingRequests.getHeaders(), ci.getRequestHeaders());
    }
}
