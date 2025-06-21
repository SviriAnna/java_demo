package ru.t1.java.demo.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.t1.java.demo.dto.MessageResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.util.UUID;

@Component
public class Service2Client {

    private final WebClient webClient;

    public Service2Client(@Value("${integration.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientRequest);
        });
    }

    public Mono<MessageResponse> checkClientStatusWithToken(UUID clientId, UUID accountId, String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/status/check")
                        .queryParam("clientId", clientId)
                        .queryParam("accountId", accountId)
                        .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(MessageResponse.class);
    }
}
