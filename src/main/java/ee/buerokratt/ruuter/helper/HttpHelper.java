package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.domain.steps.http.HttpQueryArgs;
import ee.buerokratt.ruuter.service.exception.InvalidHttpRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

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

@Service
@RequiredArgsConstructor
public class HttpHelper {
    private final MappingHelper mappingHelper;

    public HttpResponse<String> makeHttpPostRequest(HttpQueryArgs args, Map<String, Object> body) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(new URI(getUriFromArgs(args, body)))
                .timeout(Duration.of(10, SECONDS))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mappingHelper.convertObjectToString(body)));
            if (args.getHeaders() != null) {
                request.headers(convertHeadersMapToList(args.getHeaders()));
            }
            return sendHttpRequest(request);
        } catch (URISyntaxException e) {
            Thread.currentThread().interrupt();
            throw new InvalidHttpRequestException(e);
        }
    }

    public HttpResponse<String> makeHttpGetRequest(HttpQueryArgs args) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(new URI(getUriFromArgs(args, args.getBody())))
                .timeout(Duration.of(10, SECONDS))
                .GET();
            if (args.getHeaders() != null) {
                request.headers(convertHeadersMapToList(args.getHeaders()));
            }
            return sendHttpRequest(request);
        } catch (URISyntaxException e) {
            throw new InvalidHttpRequestException(e);
        }
    }

    public HttpResponse<String> sendHttpRequest(HttpRequest.Builder request) {
        try {
            return HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidHttpRequestException(e);
        }
    }

    private String getUriFromArgs(HttpQueryArgs args, Map<String, Object> body) {
        boolean hasParams = args.getQuery() != null && args.getQuery().size() > 0;
        return !hasParams ? args.getUrl() : "%s?%s".formatted(args.getUrl(), URLEncodedUtils.format(mapToNameValuePairList(body), "utf-8"));
    }

    private List<BasicNameValuePair> mapToNameValuePairList(Map<String, Object> map) {
        return map.entrySet().stream()
            .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue().toString()))
            .toList();
    }

    private String[] convertHeadersMapToList(Map<String, String> headersMap) {
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
