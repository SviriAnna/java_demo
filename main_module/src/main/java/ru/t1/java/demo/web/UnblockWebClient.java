package ru.t1.java.demo.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class UnblockWebClient {

    private final WebClient webClient;

    public UnblockWebClient(@Value("${integration.unblock-server-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<Boolean> unblockClient(UUID clientId, String token) {
        return webClient.post()
                .uri("/client/{id}", clientId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(body -> {
                            log.error("Failed to unblock client {}: status={}, body={}", clientId, clientResponse.statusCode(), body);
                            return Mono.error(new RuntimeException("Unblock client failed"));
                        })
                )
                .bodyToMono(Boolean.class)   // <- Считаем просто булево значение
                .onErrorResume(e -> {
                    log.error("Error during unblock client {}: {}", clientId, e.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<Boolean> unblockAccount(UUID accountId, String token) {
        return webClient.post()
                .uri("/account/{id}", accountId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(body -> {
                            log.error("Failed to unblock account {}: status={}, body={}", accountId, clientResponse.statusCode(), body);
                            return Mono.error(new RuntimeException("Unblock account failed"));
                        })
                )
                .bodyToMono(Boolean.class)   // <- Считаем просто булево значение
                .onErrorResume(e -> {
                    log.error("Error during unblock account {}: {}", accountId, e.getMessage());
                    return Mono.just(false);
                });
    }
}