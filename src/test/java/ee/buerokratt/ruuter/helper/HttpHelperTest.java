package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.helper.exception.SsrfGuardException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Uses Reactor Netty's own {@link HttpServer} as the upstream rather than WireMock: this project's
 * dependency graph force-resolves Jetty to 12.x, which is binary-incompatible with the Jetty 9.4
 * server bundled in wiremock-jre8:2.35.1 (fails with NoClassDefFoundError on org.eclipse.jetty.*).
 * reactor-netty-http is already a transitive test dependency (via spring-boot-starter-webflux) and
 * is the same library HttpHelper's WebClient runs on, so there's no protocol-compatibility risk.
 */
class HttpHelperTest {

    private record RecordedRequest(String method, String contentType, String body) {
    }

    private DisposableServer server;
    private final List<RecordedRequest> requests = new CopyOnWriteArrayList<>();
    private volatile int responseStatus;
    private volatile String responseBody;
    private volatile String responseContentType;

    private ApplicationProperties properties;
    private ScriptingHelper scriptingHelper;
    private OutboundRequestGuard outboundRequestGuard;
    private HttpHelper httpHelper;
    private DslInstance di;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        responseStatus = 200;
        responseBody = "";
        responseContentType = null;

        server = HttpServer.create()
            .host("localhost")
            .port(0)
            .handle((request, response) ->
                request.receive().aggregate().asString()
                    .defaultIfEmpty("")
                    .flatMap(body -> {
                        requests.add(new RecordedRequest(request.method().name(), request.requestHeaders().get("Content-Type"), body));
                        response.status(responseStatus);
                        if (responseContentType != null) {
                            response.header("Content-Type", responseContentType);
                        }
                        if (responseBody.isEmpty()) {
                            return response.send().then();
                        }
                        return response.sendString(Mono.just(responseBody)).then();
                    }))
            .bindNow();
        baseUrl = "http://localhost:%d".formatted(server.port());

        properties = new ApplicationProperties();
        properties.setHttpResponseSizeLimit(256);
        properties.setHttpRequestTimeout(15000);

        scriptingHelper = mock(ScriptingHelper.class);
        outboundRequestGuard = mock(OutboundRequestGuard.class); // permissive by default - doesn't throw
        httpHelper = new HttpHelper(properties, scriptingHelper, outboundRequestGuard);
        di = mock(DslInstance.class);
    }

    @AfterEach
    void tearDown() {
        server.disposeNow();
    }

    private void stub(int status, String body, String contentType) {
        responseStatus = status;
        responseBody = body;
        responseContentType = contentType;
    }

    @Test
    void doGet_shouldReturnUpstreamResponse() {
        stub(200, "\"hello\"", "application/json");

        ResponseEntity<Object> response = httpHelper.doGet(baseUrl + "/endpoint", new HashMap<>(), new HashMap<>(), di, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("hello", response.getBody());
    }

    @Test
    void doPost_shouldSendJsonBodyWithDefaultContentType() {
        stub(201, "", null);

        Map<String, Object> body = new HashMap<>();
        body.put("some_val", "Hello World");

        httpHelper.doPost(baseUrl + "/endpoint", body, new HashMap<>(), new HashMap<>(), di, false, null);

        assertEquals(1, requests.size());
        assertTrue(requests.get(0).contentType().startsWith("application/json"));
        assertEquals("{\"some_val\":\"Hello World\"}", requests.get(0).body());
    }

    @Test
    void doPost_withDynamicBody_shouldSendTheRawStringValueUnencoded() {
        stub(200, "", null);

        Map<String, Object> body = new HashMap<>();
        body.put("dynamicBody", "hello");

        httpHelper.doPost(baseUrl + "/endpoint", body, new HashMap<>(), new HashMap<>(), di, true, null);

        assertEquals(1, requests.size());
        // the raw string is sent as-is (not JSON-string-encoded), even though Content-Type says application/json -
        // this is what lets callers pass pre-built JSON documents through dynamicBody without double-encoding
        assertEquals("hello", requests.get(0).body());
    }

    @Test
    void doPostPlaintext_shouldSendRawTextBody() {
        stub(200, "", null);

        httpHelper.doPostPlaintext(baseUrl + "/endpoint", new HashMap<>(), new HashMap<>(), new HashMap<>(), "hello world", di);

        assertEquals(1, requests.size());
        assertTrue(requests.get(0).contentType().startsWith("text/plain"));
        assertEquals("hello world", requests.get(0).body());
    }

    @Test
    void doPut_shouldSendBody() {
        stub(204, "", null);

        Map<String, Object> body = new HashMap<>();
        body.put("key", "value");

        ResponseEntity<Object> response = httpHelper.doPut(baseUrl + "/endpoint", body, new HashMap<>(), new HashMap<>(), null, di, false, true);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("PUT", requests.get(0).method());
    }

    @Test
    void doDelete_shouldSendRequest() {
        stub(200, "", null);

        httpHelper.doDelete(baseUrl + "/endpoint", new HashMap<>(), new HashMap<>(), new HashMap<>(), null, di);

        assertEquals(1, requests.size());
        assertEquals("DELETE", requests.get(0).method());
    }

    @Test
    void doMethod_formdata_shouldUrlEncodeBodyWhenNoFileEntriesPresent() {
        stub(200, "", null);

        Map<String, Object> body = new HashMap<>();
        body.put("username", "alar");

        httpHelper.doMethod(HttpMethod.POST, baseUrl + "/endpoint", new HashMap<>(), body,
            new HashMap<>(), "formdata", null, null, di, false, true, null);

        assertEquals(1, requests.size());
        assertTrue(requests.get(0).contentType().startsWith("application/x-www-form-urlencoded"));
        assertEquals("username=alar", requests.get(0).body());
    }

    @Test
    void doMethod_formdata_shouldUploadFileAndEvaluateFilenameThroughScriptingHelper() {
        stub(200, "", null);
        when(scriptingHelper.evaluateScripts("notes.txt", di)).thenReturn("evaluated-notes.txt");

        Map<String, Object> body = new HashMap<>();
        body.put("file:attachment:notes.txt", "file contents");

        ResponseEntity<Object> response = httpHelper.doMethod(HttpMethod.POST, baseUrl + "/endpoint",
            new HashMap<>(), body, new HashMap<>(), "formdata", null, null, di, false, true, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).contentType().startsWith("multipart/form-data"));
    }

    @Test
    void doMethod_jsonOverride_shouldForceJsonDecodingRegardlessOfUpstreamContentType() {
        stub(200, "{\"key\":\"value\"}", "text/plain");

        ResponseEntity<Object> response = httpHelper.doMethod(HttpMethod.GET, baseUrl + "/endpoint",
            new HashMap<>(), null, new HashMap<>(), "json_override", null, null, di, false, true, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("key", "value"), response.getBody());
    }

    @Test
    void doMethod_shouldReturnResponseStatusAndTextInsteadOfThrowing_onUpstreamErrorStatus() {
        stub(500, "boom", "text/plain");

        ResponseEntity<Object> response = httpHelper.doGet(baseUrl + "/endpoint", new HashMap<>(), new HashMap<>(), di, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void doMethod_shouldReturnForbidden_andSkipTheRequest_whenOutboundRequestGuardBlocksTheUrl() {
        stub(200, "should never be reached", null);
        doThrow(new SsrfGuardException("Target host resolves to a restricted network: internal"))
            .when(outboundRequestGuard).assertAllowed(baseUrl + "/endpoint");

        ResponseEntity<Object> response = httpHelper.doGet(baseUrl + "/endpoint", new HashMap<>(), new HashMap<>(), di, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Target host resolves to a restricted network: internal", response.getBody());
        assertEquals(0, requests.size());
        verifyNoInteractions(scriptingHelper);
    }

    @Test
    void doMethod_shouldReturnBadGateway_whenResponseExceedsConfiguredSizeLimit() {
        // Spring wraps the DataBufferLimitException from exceeding maxInMemorySize in a
        // WebClientResponseException that still carries the upstream's original 2xx status. doMethod
        // must not report that back as if the call had succeeded.
        stub(200, "\"" + "x".repeat(5000) + "\"", "application/json");

        ResponseEntity<Object> response = httpHelper.doMethod(HttpMethod.GET, baseUrl + "/endpoint", new HashMap<>(), null,
            new HashMap<>(), null, null, 1, di, false, true, null);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Exceeded limit"));
    }
}
