package ee.buerokratt.ruuter.util;

import ee.buerokratt.ruuter.domain.HttpStepArgs;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static ee.buerokratt.ruuter.util.StringUtils.convertHeadersMapToList;
import static ee.buerokratt.ruuter.util.StringUtils.getUrlWithQuery;
import static java.time.temporal.ChronoUnit.SECONDS;

public class HttpUtils {

    private HttpUtils() {

    }

    public static HttpRequest getHttpRequest(HttpStepArgs args) throws URISyntaxException {
        HttpRequest.Builder request = HttpRequest.newBuilder(new URI(getUrlWithQuery(args.getUrl(), args.getQuery())))
            .timeout(Duration.of(10, SECONDS))
            .GET();
        if (args.getHeaders() != null) {
            request.headers(convertHeadersMapToList(args.getHeaders()).toArray(new String[0]));
        }
        return request.build();
    }

    public static HttpResponse<String> sendHttpRequest(HttpRequest request) throws IOException, InterruptedException {
        return HttpClient
            .newBuilder()
            .proxy(ProxySelector.getDefault())
            .build()
            .send(request, HttpResponse.BodyHandlers.ofString());
    }
}
