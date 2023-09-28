package ee.buerokratt.ruuter.helper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HttpHelper {

    public ResponseEntity<Object> doPost(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers) {
        return doPost(url, body, query, headers, this.getClass().getName());
    }
    public ResponseEntity<Object> doPost(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType) {
        return doMethod(HttpMethod.POST, url, query, body,headers, contentType, null);
    }

    public ResponseEntity<Object> doPostPlaintext(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String plaintext) {
        return doMethod(HttpMethod.POST, url, body, query, headers, "plaintext", plaintext);
    }

    public ResponseEntity<Object> doGet(String url, Map<String, Object> query, Map<String, String> headers) {
        return doMethod(HttpMethod.GET, url, query, null, headers, null, null);
    }

    public ResponseEntity<Object> doPut(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType) {
        return doMethod(HttpMethod.PUT, url, query, body,headers, contentType, null);
    }

    public ResponseEntity<Object> doDelete(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType) {
        return doMethod(HttpMethod.DELETE, url, query, body,headers, contentType, null);
    }

    public ResponseEntity<Object> doMethod(HttpMethod method,
                                           String url,
                                           Map<String, Object> query,
                                           Map<String, Object> body,
                                           Map<String, String> headers,
                                           String contentType,
                                           String plaintextValue) {
        try {
            MultiValueMap<String, String> qp = new LinkedMultiValueMap<>(
                query.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e-> Arrays.asList(e.getValue().toString()))));

            Object bodyValue;
            if (method == HttpMethod.POST &&
                "plaintext".equals(contentType) && plaintextValue != null)
                bodyValue = plaintextValue;
            else if (body == null)
                bodyValue = new HashMap<>();
            else
                bodyValue = body;

            return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(getHttpClient())).build()
                .method(method)
                .uri(url, uriBuilder -> uriBuilder.queryParams(qp).build())
                .bodyValue(bodyValue)
                .headers(httpHeaders -> addHeadersIfNotNull(headers, httpHeaders))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .toEntity(Object.class)
                .block();
        } catch (WebClientResponseException e) {
            return new ResponseEntity<>(e.getStatusText(), e.getStatusCode());
        }
    }

    private void addHeadersIfNotNull(Map<String, String> headers, HttpHeaders httpHeaders) {
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }
    }

    private HttpClient getHttpClient() {
        return HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofMillis(10000))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(10000, TimeUnit.MILLISECONDS)));
    }
}
