package ee.buerokratt.ruuter.helper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HttpHelper {

    public ResponseEntity<Object> doPost(String url, Map<String, Object> body, Map<String, Object> query, Map<String, String> headers) {
        WebClient client = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(getHttpClient()))
            .baseUrl(url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultUriVariables(query)
            .build();

        try {
            return client.post()
                .headers(httpHeaders -> addHeadersIfNotNull(headers, httpHeaders))
                .bodyValue(body)
                .retrieve()
                .toEntity(Object.class)
                .block();
        } catch (WebClientResponseException e) {
            return new ResponseEntity<>(e.getStatusText(), e.getStatusCode());
        }
    }

    public ResponseEntity<Object> doGet(String url, Map<String, Object> query, Map<String, String> headers) {
        try {
            MultiValueMap<String, String> qp = new LinkedMultiValueMap<>(
                query.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e-> Arrays.asList(e.getValue().toString()))));
            return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(getHttpClient())).build()
                .get()
                .uri(url, uriBuilder -> uriBuilder.queryParams(qp).build())
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
