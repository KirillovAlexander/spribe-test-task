package co.spribe.exchangerate.integration;

import co.spribe.exchangerate.integration.dto.ExchangeRateDto;
import reactor.core.publisher.Mono;

public interface ExchangeRateProvider {

    Mono<ExchangeRateDto> getExchangeRates(String baseCurrency);
}
