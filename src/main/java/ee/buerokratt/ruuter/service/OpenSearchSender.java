package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@Service
public class OpenSearchSender  {

    @RequiredArgsConstructor
    @Data
    public static class RuuterEvent {
        Long timestamp = Instant.now().toEpochMilli();
        final String level;

        final String dslName;
        final String dslMethod;

        final String stepName;

        final Integer statusCode;
        final Integer errorCode;

        final Map<String, Object> requestParams;
        final Map<String, String> requestHeaders;
        final Map<String, Object> requestBody;

        final String message;

        final StackTraceElement[] stackTrace;
    }

    private ApplicationProperties properties;

    private WebClient webClient;
    private String indexName;

    private boolean loggingEnabled ;

    public OpenSearchSender(ApplicationProperties properties) {
        this.properties = properties;
        loggingEnabled = properties.getOpenSearchConfiguration() != null;
        if (!loggingEnabled) {
            log.warn("OpenSearch logging disabled");
        }
    }

    private void createClient() {
        webClient = WebClient.create(properties.getOpenSearchConfiguration().getUrl());
        indexName = properties.getOpenSearchConfiguration().getIndex();
    }

    public void log(RuuterEvent ruuterEvent) {
        if (properties.getOpenSearchConfiguration() == null) {
            return;
        }

        if (webClient == null)
            createClient();

        webClient.post()
            .uri("/{logIndex}/_doc", indexName)
            .bodyValue(ruuterEvent)
            .retrieve()
            .bodyToMono(Void.class)
            .onErrorResume(exception -> {
                log.error("Unable to send log to OpenSearch", exception);
                return Mono.empty();
            })
            .subscribe();
    }
}
