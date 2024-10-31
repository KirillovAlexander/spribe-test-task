package co.spribe.exchangerate.integration.fixer;

import co.spribe.exchangerate.integration.ExchangeRateProvider;
import co.spribe.exchangerate.integration.dto.ExchangeRateDto;
import co.spribe.exchangerate.integration.fixer.api.FixerApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FixerExchangeRateProvider implements ExchangeRateProvider {

    private static final String EUR = "EUR";

    private final FixerApiClient fixerApiClient;

    @Override
    public Mono<ExchangeRateDto> getExchangeRates(String baseCurrency) {
        //Just to avoid problems with Fixer free-plan restrictions
        if (!EUR.equals(baseCurrency)) {
            baseCurrency = EUR;
        }
        return fixerApiClient.getLatestRates(baseCurrency)
                .map(response -> new ExchangeRateDto(response.base(), response.rates()));
    }
}
