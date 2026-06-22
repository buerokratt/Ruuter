package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.domain.DslInstance;
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

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.message.ParameterizedMessage.deepToString;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpHelper {

    final private ApplicationProperties properties;

    final private ScriptingHelper scriptingHelper;

    public ResponseEntity<Object> doPost(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, DslInstance di, boolean dynamicBody, Integer timeout) {
        return doPost(url, body, query, headers, this.getClass().getName(), di, dynamicBody, timeout);
    }

    public ResponseEntity<Object> doPost(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType, DslInstance di, boolean dynamicBody, Integer timeout ) {
        return doMethod(POST, url, query, body,headers, contentType, null, null, di, dynamicBody, true, timeout);
    }

    public ResponseEntity<Object> doPostPlaintext(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String plaintext, DslInstance di) {
        return doMethod(POST, url, body, query, headers, "plaintext", plaintext, null, di, false, true, null);
    }

    public ResponseEntity<Object> doGet(String url, Map<String, Object> query, Map<String, String> headers, DslInstance di, Integer timeout) {
        return doMethod(HttpMethod.GET, url, query, null, headers, null, null, null, di, false, true, timeout );
    }

    public ResponseEntity<Object> doPut(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType, DslInstance di, boolean dynamicBody, boolean blockResult) {
        return doMethod(HttpMethod.PUT, url, query, body,headers, contentType, null, null, di, dynamicBody, true, null);
    }

    public ResponseEntity<Object> doDelete(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers, String contentType, DslInstance di) {
        return doMethod(HttpMethod.DELETE, url, query, body, headers, contentType, null, null, di, false, true, null);
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
                                           boolean dynamicBody,
                                           boolean blockResult,
                                           Integer timeout) {
        try {
            MultiValueMap<String, String> qp = new LinkedMultiValueMap<>(
                query.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e-> Arrays.asList(e.getValue().toString()))));

            BodyInserter bodyValue;
            String mediaType;

            log.debug("HTTP headers:" + deepToString(headers));

            if (method == POST &&
                "plaintext".equals(contentType) && plaintextValue != null) {
                bodyValue = BodyInserters.fromValue(plaintextValue);
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
                    log.info("REQUEST BODY: " + deepToString(body));
                    bodyValue = BodyInserters.fromValue(body);
                }
                mediaType = MediaType.APPLICATION_JSON_VALUE;
            }

            Integer finalLimit = limit == null ? properties.getHttpResponseSizeLimit() : limit;
            Mono<ResponseEntity<Object>> retrieve = WebClient.builder()
                .filter((request, next) -> !("json_override".equals(contentType)) ? next.exchange(request)
                    : next.exchange(request)
                    .flatMap(response -> Mono.just(response.mutate()
                        .headers(httpHeaders -> httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .build())))
                .exchangeStrategies(
                    ExchangeStrategies.builder().codecs(

                        configurer -> configurer.defaultCodecs().maxInMemorySize(finalLimit * 1024)).build())
                .clientConnector(new ReactorClientHttpConnector(getHttpClient(timeout))).build()
                .method(method)
                .uri(url, uriBuilder -> uriBuilder.queryParams(qp).build())
                .headers(httpHeaders -> {
                    addHeadersIfNotNull(headers, httpHeaders);
                    if (!hasContentType(headers))
                        httpHeaders.add(HttpHeaders.CONTENT_TYPE, mediaType);
                })
                .body(bodyValue)
                .retrieve()
                .toEntity(Object.class);
            if (blockResult)
                return retrieve.block();
            else {
                Disposable dis = retrieve.subscribe();
                return ResponseEntity.ok(null);
            }
        } catch (WebClientResponseException e) {
            log.error("Failed HTTP request: ", e);
            log.error("CAUSE:" + e.getResponseBodyAsString());
            return new ResponseEntity<>(e.getStatusText(), e.getStatusCode());
        }
    }

    private void addHeadersIfNotNull(Map<String, String> headers, HttpHeaders httpHeaders) {
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }
    }

    private HttpClient getHttpClient(Integer timeout) {
        final Integer finalTimeout = timeout != null ? timeout :
            properties.getHttpRequestTimeout() != null ? properties.getHttpRequestTimeout() :
                15000;

        log.debug("HTTP request effective timeout: "+ finalTimeout);

        return HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, finalTimeout)
                    .responseTimeout(Duration.ofMillis(finalTimeout))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(finalTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(finalTimeout, TimeUnit.MILLISECONDS)));
    }

    // Sadly, Java default Map does not have a case-insensitive containsKey
    private boolean hasContentType(Map<String, String> headers) {
        return headers.containsKey("Content-type") ||
            headers.containsKey("Content-Type") ||
            headers.containsKey("content-type") ||
            headers.containsKey("content-Type");
    }
}
