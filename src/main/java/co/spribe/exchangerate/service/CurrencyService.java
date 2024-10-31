package co.spribe.exchangerate.service;

import co.spribe.exchangerate.dto.CurrencyDto;
import co.spribe.exchangerate.model.Currency;
import co.spribe.exchangerate.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {

    private final ExchangeRateService exchangeRateService;

    private final CurrencyRepository currencyRepository;

    public Mono<CurrencyDto> get(final String code) {
        log.info("Fetching currency with code: {}", code);
        return Mono.justOrEmpty(currencyRepository.findByCode(code))
                .map(currency -> new CurrencyDto(currency.code()))
                .doOnSuccess(currency -> log.info("Found currency: {}", currency))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Currency not found: {}", code);
                    return Mono.error(new RuntimeException("Currency not found: " + code));
                }));
    }

    public Flux<CurrencyDto> getAll() {
        log.info("Fetching all currencies.");
        return Flux.fromIterable(currencyRepository.findAll())
                .map(currency -> new CurrencyDto(currency.code()))
                .doOnComplete(() -> log.info("All currencies fetched successfully."));
    }

    public Mono<CurrencyDto> save(final CurrencyDto currencyDto) {
        log.info("Saving currency with code: {}", currencyDto.code());
        Currency currency = new Currency(currencyDto.code());
        return Mono.fromCallable(() -> currencyRepository.save(currency))
                .doOnSuccess(persistedCurrency -> log.info("Currency saved with code: {}", persistedCurrency.code()))
                .flatMap(persistedCurrency ->
                        exchangeRateService.updateExchangeRates(persistedCurrency.code())
                                .then(Mono.just(new CurrencyDto(persistedCurrency.code())))
                )
                .onErrorMap(e -> {
                    log.error("Failed to save currency {}: {}", currencyDto.code(), e.getMessage());
                    return new RuntimeException("Failed to save currency: " + currencyDto.code(), e);
                });
    }
}
