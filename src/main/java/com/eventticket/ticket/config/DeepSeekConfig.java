package com.eventticket.ticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DeepSeekConfig {

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Bean
    public WebClient deepSeekWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
