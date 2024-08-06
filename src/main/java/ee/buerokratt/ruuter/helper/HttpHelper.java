package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
import ee.buerokratt.ruuter.util.LoggingUtils;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.POST;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpHelper {

    final private ApplicationProperties properties;

    final private ScriptingHelper scriptingHelper;

    public ResponseEntity<Object> doPost(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, DslInstance di, boolean dynamicBody) {
        return doPost(url, body, query, headers, this.getClass().getName(), di, dynamicBody);
    }
    public ResponseEntity<Object> doPost(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType, DslInstance di, boolean dynamicBody) {
        return doMethod(POST, url, query, body,headers, contentType, null, null, di, dynamicBody);
    }

    public ResponseEntity<Object> doPostPlaintext(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String plaintext, DslInstance di) {
        return doMethod(POST, url, body, query, headers, "plaintext", plaintext, null, di, false);
    }

    public ResponseEntity<Object> doGet(String url, Map<String, Object> query, Map<String, String> headers, DslInstance di) {
        return doMethod(HttpMethod.GET, url, query, null, headers, null, null, null, di, false);
    }

    public ResponseEntity<Object> doPut(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType, DslInstance di, boolean dynamicBody) {
        return doMethod(HttpMethod.PUT, url, query, body,headers, contentType, null, null, di, dynamicBody);
    }

    public ResponseEntity<Object> doDelete(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType, DslInstance di) {
        return doMethod(HttpMethod.DELETE, url, query, body, headers, contentType, null, null, di, false);
    }

    public ResponseEntity<Object> doMethod(HttpMethod method,
                                           String url,
                                           Map<String, Object> query,
                                           Map<String, Object> body,
                                           Map<String, String> headers,
                                           String contentType,
                                           String plaintextValue,
                                           Integer limit,
                                           DslInstance instance,
                                           boolean dynamicBody) {
        try {
            MultiValueMap<String, String> qp = new LinkedMultiValueMap<>(
                query.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e-> Arrays.asList(e.getValue().toString()))));

            BodyInserter bodyValue;
            String mediaType;
            if (method == POST &&
                "plaintext".equals(contentType) && plaintextValue != null) {
                bodyValue = BodyInserters.fromValue(plaintextValue);;
                mediaType = MediaType.TEXT_PLAIN_VALUE;
            } else if ("formdata".equals(contentType) &&
                    body.keySet().stream().noneMatch(k -> k.startsWith("file:"))) {
                mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
                MultiValueMap<String, String> bp = new LinkedMultiValueMap<>(
                    body.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> Arrays.asList(e.getValue().toString()))));
                bodyValue = BodyInserters.fromFormData(bp);
            } else if ("formdata".equals(contentType)) {
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE;
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                for (Map.Entry<String, Object> e : body.entrySet()) {
                    if (e.getKey().startsWith("file:")) {
                        byte[] bytes = ((String) e.getValue()).getBytes();
                        String fieldname = e.getKey().split(":")[1];
                        String filename = e.getKey().split(":")[2];

                        filename = scriptingHelper.evaluateScripts(filename, instance).toString();

                        builder.part(fieldname, new ByteArrayResource(bytes)).filename(filename);
                    }
                    else {
                        builder.part(e.getKey(), e.getValue());
                    }
                }
                MultiValueMap<String, HttpEntity<?>> bodyparts = builder.build();
                bodyValue = BodyInserters.fromMultipartData(bodyparts);
            } else if (body == null) {
                bodyValue = BodyInserters.empty();
                mediaType = MediaType.APPLICATION_JSON_VALUE;
            } else {
                if (dynamicBody) {
                    log.warn("Sending dynamic body request is considered unsecure");
                    bodyValue = BodyInserters.fromValue(body.get("dynamicBody"));
                } else {
                    bodyValue = BodyInserters.fromValue(body);
                }
                mediaType = MediaType.APPLICATION_JSON_VALUE;
            }

            Integer finalLimit = limit == null ? properties.getHttpResponseSizeLimit() : limit;
            return WebClient.builder()
                .exchangeStrategies(
                    ExchangeStrategies.builder().codecs(
                        configurer -> configurer.defaultCodecs().maxInMemorySize(finalLimit * 1024 )).build())
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
            log.error("Failed HTTP request: ", e);
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
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofMillis(15000))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(15000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(15000, TimeUnit.MILLISECONDS)));
    }
}
