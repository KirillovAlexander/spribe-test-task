package co.spribe.exchangerate.schedule;

import co.spribe.exchangerate.service.CurrencyService;
import co.spribe.exchangerate.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "${scheduler.update-exchange-rates-cron}")
    public void updateExchangeRatesForAllCurrencies() {
        currencyService.getAll()
                .flatMap(currency -> exchangeRateService.updateExchangeRates(currency.code())
                        .onErrorResume(e -> {
                            log.error("Failed to update exchange rates for currency {}: {}", currency.code(), e.getMessage());
                            return Mono.empty();
                        })
                )
                .subscribe();
    }
}
