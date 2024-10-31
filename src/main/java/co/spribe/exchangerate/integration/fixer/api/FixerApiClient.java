package co.spribe.exchangerate.integration.fixer.api;

import co.spribe.exchangerate.integration.fixer.config.FixerApiProperties;
import co.spribe.exchangerate.integration.fixer.dto.FixerApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class FixerApiClient {

    private static final String ACCESS_KEY_QUERY_PARAM = "access_key";
    private static final String BASE_CURRENCY_QUERY_PARAM = "base";

    private final WebClient webClient;
    private final FixerApiProperties fixerApiProperties;

    public FixerApiClient(final WebClient webClient, final FixerApiProperties fixerApiProperties) {
        this.webClient = webClient;
        this.fixerApiProperties = fixerApiProperties;
    }

    public Mono<FixerApiResponseDto> getLatestRates(final String baseCurrency) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/latest")
                        .queryParam(ACCESS_KEY_QUERY_PARAM, fixerApiProperties.accessKey())
                        .queryParam(BASE_CURRENCY_QUERY_PARAM, baseCurrency)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Client error on fetching rates: {}", body);
                                    return Mono.error(new RuntimeException("Client error: " + body));
                                }))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Server error on fetching rates: {}", body);
                                    return Mono.error(new RuntimeException("Server error: " + body));
                                }))
                .bodyToMono(FixerApiResponseDto.class);
    }
}
