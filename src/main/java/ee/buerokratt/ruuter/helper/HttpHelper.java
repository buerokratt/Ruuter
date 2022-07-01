package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.domain.ConfigurationInstance;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@RequiredArgsConstructor
public class HttpHelper {

    public HttpResponse<String> makeHttpPostRequest(HttpQueryArgs args, ConfigurationInstance ci) {
        try {

            HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(new URI(getUriFromArgs(args, ci)))
                .timeout(Duration.of(10, SECONDS))
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(ci.getMappingHelper().convertObjectToString(evaluateMapValues(args.getBody(), ci))));
            if (args.getHeaders() != null) {
                request.headers(convertHeadersMapToList(args.getHeaders()));
            }
            return sendHttpRequest(request);
        } catch (URISyntaxException e) {
            Thread.currentThread().interrupt();
            throw new InvalidHttpRequestException(e);
        }
    }

    public HttpResponse<String> makeHttpGetRequest(HttpQueryArgs args, ConfigurationInstance ci) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(new URI(getUriFromArgs(args, ci)))
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

    private String getUriFromArgs(HttpQueryArgs args, ConfigurationInstance ci) {
        boolean hasParams = args.getQuery() != null && args.getQuery().size() > 0;
        return !hasParams ? args.getUrl() : "%s?%s".formatted(args.getUrl(), URLEncodedUtils.format(mapToNameValuePairList(evaluateMapValues(args.getQuery(), ci)), "utf-8"));
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

    private HashMap<String, Object> evaluateMapValues(HashMap<String, Object> map, ConfigurationInstance ci) {
        HashMap<String, Object> evaluatedMap = new HashMap<>();
        map.forEach((k, v) -> evaluatedMap.put(k, ci.getScriptingHelper().evaluateScripts(v, ci.getContext(), ci.getRequestBody(), ci.getRequestParams())));
        return evaluatedMap;
    }
}
