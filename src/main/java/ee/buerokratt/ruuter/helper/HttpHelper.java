package ee.buerokratt.ruuter.helper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.POST;

@Service
@RequiredArgsConstructor
public class HttpHelper {

    public ResponseEntity<Object> doPost(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers) {
        return doPost(url, body, query, headers, this.getClass().getName());
    }
    public ResponseEntity<Object> doPost(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType) {
        return doMethod(POST, url, query, body,headers, contentType, null);
    }

    public ResponseEntity<Object> doPostPlaintext(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String plaintext) {
        return doMethod(POST, url, body, query, headers, "plaintext", plaintext);
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

            BodyInserter bodyValue;
            String mediaType;

            if (method == POST &&
                "plaintext".equals(contentType) && plaintextValue != null) {
                bodyValue = BodyInserters.fromValue(plaintextValue);;
                mediaType = MediaType.TEXT_PLAIN_VALUE;
            } else if (method == POST &&
                    "formdata".equals(contentType)) {
                MultiValueMap<String, String> bp = new LinkedMultiValueMap<>(
                    body.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> Arrays.asList(e.getValue().toString()))));
                bodyValue = BodyInserters.fromFormData(bp);
                mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
            } else if (method == POST && "file".equals(contentType)) {
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE;
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                body.entrySet().forEach(e -> builder.part(e.getKey(), e.getValue()));
                bodyValue = BodyInserters.fromMultipartData(builder.build());
            } else if (body == null) {
                bodyValue = BodyInserters.empty();
                mediaType = MediaType.APPLICATION_JSON_VALUE;
            } else {
                bodyValue = BodyInserters.fromValue(body);
                mediaType = MediaType.APPLICATION_JSON_VALUE;
            }

            return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(getHttpClient())).build()
                .method(method)
                .uri(url, uriBuilder -> uriBuilder.queryParams(qp).build())
                .headers(httpHeaders -> addHeadersIfNotNull(headers, httpHeaders))
                .body(bodyValue)
                .header(HttpHeaders.CONTENT_TYPE, mediaType)
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
