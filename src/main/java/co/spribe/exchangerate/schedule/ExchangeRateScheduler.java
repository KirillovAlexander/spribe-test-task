package co.spribe.exchangerate.schedule;

import co.spribe.exchangerate.service.CurrencyService;
import co.spribe.exchangerate.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "${scheduler.update-exchange-rates-cron}")
    public void updateExchangeRatesForAllCurrencies() {
        log.info("Starting scheduled update of exchange rates for all currencies.");

        currencyService.getAll()
                .flatMap(currency -> {
                    log.info("Updating exchange rates for currency: {}", currency.code());
                    return exchangeRateService.updateExchangeRates(currency.code())
                            .doOnSuccess(aVoid -> log.info("Successfully updated exchange rates for currency: {}", currency.code()))
                            .doOnError(e -> log.error("Error updating exchange rates for currency {}: {}", currency.code(), e.getMessage()));
                })
                .doOnComplete(() -> log.info("Completed updating exchange rates for all currencies."))
                .subscribe();
    }
}
