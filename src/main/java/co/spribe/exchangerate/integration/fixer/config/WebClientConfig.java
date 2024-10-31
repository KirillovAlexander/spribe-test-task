package co.spribe.exchangerate.integration.fixer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient fixerWebClient(FixerApiProperties fixerApiProperties) {
        return WebClient.builder()
                .baseUrl(fixerApiProperties.baseUrl())
                .build();
    }
}
