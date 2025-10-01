package com.example.musicGenie.config.webclient;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public Map<String, WebClient> providerWebClients(OAuth2AuthorizedClientManager manager) {
        return Map.of(
                "spotify", buildWebClient(manager, "spotify", "https://api.spotify.com/v1")
        );
    }

    private WebClient buildWebClient(OAuth2AuthorizedClientManager manager, String registrationId, String baseUrl) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);

        oauth2Client.setDefaultClientRegistrationId(registrationId);

        return WebClient.builder()
                        .baseUrl(baseUrl)
                        .codecs(configurer ->
                                configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16 MB
                        .filter(oauth2Client)
                        .build();
    }
}

