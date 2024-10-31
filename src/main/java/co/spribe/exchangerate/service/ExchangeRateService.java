package co.spribe.exchangerate.service;

import co.spribe.exchangerate.dto.ExchangeRateDto;
import co.spribe.exchangerate.integration.ExchangeRateProvider;
import co.spribe.exchangerate.mapper.ExchangeRateLogMapper;
import co.spribe.exchangerate.model.ExchangeRateLog;
import co.spribe.exchangerate.registry.ExchangeRateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateLogService exchangeRateLogService;
    private final ExchangeRateRegistry exchangeRateRegistry;
    private final ExchangeRateProvider exchangeRateProvider;

    public Mono<ExchangeRateDto> getExchangeRates(final String baseCurrency) {
        log.info("Fetching exchange rates for base currency: {}", baseCurrency);
        return Mono.justOrEmpty(exchangeRateRegistry.get(baseCurrency))
                .doOnSuccess(exchangeRateDto -> log.info("Fetched exchange rates for base currency: {}", baseCurrency))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Exchange rates not available for base currency: {}", baseCurrency);
                    return Mono.error(new RuntimeException("Exchange rates not available for currency: " + baseCurrency));
                }));
    }

    public Mono<Void> updateExchangeRates(final String baseCurrency) {
        log.info("Updating exchange rates for base currency: {}", baseCurrency);
        return exchangeRateProvider.getExchangeRates(baseCurrency)
                .doOnNext(exchangeRateDto -> {
                    log.info("Received exchange rates for base currency: {}", baseCurrency);
                    exchangeRateRegistry.save(new ExchangeRateDto(baseCurrency, exchangeRateDto.exchangeRates()));
                    List<ExchangeRateLog> logs = ExchangeRateLogMapper.toExchangeRateLogs(baseCurrency, exchangeRateDto);
                    exchangeRateLogService.saveAll(logs);
                    log.info("Exchange rates updated for base currency: {}, with {} logs saved", baseCurrency, logs.size());
                })
                .doOnError(e -> log.error("Failed to update exchange rates for {}: {}", baseCurrency, e.getMessage()))
                .then();
    }
}
