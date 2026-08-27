package ee.buerokratt.ruuter.helper;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The wiremock/Jetty combination here is fragile: Spring Boot's dependency management forces
 * org.eclipse.jetty:* core artifacts to a version that {@code wiremock-jetty12}'s bundled
 * org.eclipse.jetty.ee10:* artifacts don't line up with by default (see the jetty-ee10-*
 * constraints in build.gradle). This test exists purely to catch that regressing silently on a
 * future dependency bump - if it fails, check those Jetty version constraints first.
 */
@WireMockTest
class WireMockSmokeTest {

    @Test
    void wireMockServerStartsAndServesStubbedResponse(WireMockRuntimeInfo wireMockRuntimeInfo) throws IOException, InterruptedException {
        stubFor(get(urlEqualTo("/ping")).willReturn(aResponse().withStatus(200).withBody("pong")));

        HttpResponse<String> response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder(URI.create("http://localhost:%s/ping".formatted(wireMockRuntimeInfo.getHttpPort()))).GET().build(),
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("pong", response.body());
    }
}
