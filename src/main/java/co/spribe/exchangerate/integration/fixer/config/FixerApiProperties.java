package co.spribe.exchangerate.integration.fixer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fixer.api")
public record FixerApiProperties(String baseUrl, String accessKey) {
}
