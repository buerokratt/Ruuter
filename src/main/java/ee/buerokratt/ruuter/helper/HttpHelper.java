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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class HttpHelper {

    public ResponseEntity<Object> post(String url, Map<String, Object> body, Map<String, Object> params, Map<String, String> headers) {
        WebClient client = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(getHttpClient()))
            .baseUrl(url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultUriVariables(params)
            .build();

        return client.post()
            .headers(httpHeaders -> addHeadersIfNotNull(headers, httpHeaders))
            .bodyValue(body)
            .retrieve()
            .toEntity(Object.class)
            .block();
    }

    public ResponseEntity<Object> get(String url, Map<String, Object> params, Map<String, String> headers) {
        WebClient client = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(getHttpClient()))
            .baseUrl(url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultUriVariables(params)
            .build();

        return client.get()
            .headers(httpHeaders -> addHeadersIfNotNull(headers, httpHeaders))
            .retrieve()
            .toEntity(Object.class)
            .block();
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
