package com.example.githubapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private final String githubToken = Optional.ofNullable(System.getenv("GITHUB_TOKEN"))
                                                .orElse("");

    @Bean
    public WebClient webClient() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl("https://api.github.com")
                .filter(logRequest());

        if (githubToken == null || githubToken.isEmpty()) {
            log.warn("GITHUB_TOKEN environment variable is not set. Requests will be unauthenticated.");
        } else {
            builder.defaultHeader("Authorization", "token " + githubToken);
        }

        return builder.build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }
}