package ee.buerokratt.ruuter.util;

import ee.buerokratt.ruuter.domain.steps.http.HttpQueryArgs;
import ee.buerokratt.ruuter.service.exception.InvalidHttpRequestException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.SECONDS;

public class HttpUtils {
    private HttpUtils() {
    }

    public static HttpResponse<String> makeHttpRequest(HttpQueryArgs args) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(new URI(getUriFromArgs(args)))
                .timeout(Duration.of(10, SECONDS))
                .GET();
            if (args.getHeaders() != null) {
                request.headers(convertHeadersMapToList(args.getHeaders()));
            }
            return HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request.build(), HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidHttpRequestException(e);
        }
    }

    private static String getUriFromArgs(HttpQueryArgs args) {
        boolean hasParams = args.getQuery() != null && args.getQuery().size() > 0;
        return !hasParams ? args.getUrl() : "%s?%s".formatted(args.getUrl(), URLEncodedUtils.format(mapToNameValuePairList(args.getQuery()), "utf-8"));
    }

    private static List<BasicNameValuePair> mapToNameValuePairList(Map<String, Object> map) {
        return map.entrySet().stream()
            .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue().toString()))
            .toList();
    }

    private static String[] convertHeadersMapToList(Map<String, String> headersMap) {
        if (headersMap != null) {
            List<String> headers = new ArrayList<>();
            headersMap.forEach((k, v) -> {
                headers.add(k);
                headers.add(v);
            });
            return headers.toArray(new String[0]);
        }
        return new String[0];
    }
}
