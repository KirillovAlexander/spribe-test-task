package co.spribe.exchangerate.registry;

import co.spribe.exchangerate.dto.ExchangeRateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ConditionalOnProperty(name = "registry.concurrentHashMap.enabled", havingValue = "true")
public class ExchangeRateInMemoryConcurrentHashMapRegistry implements ExchangeRateRegistry {
    private final Map<String, Map<String, BigDecimal>> rates = new ConcurrentHashMap<>();

    @Override
    public ExchangeRateDto get(final String baseCurrency) {
        log.info("Retrieving rates for base currency: {}", baseCurrency);
        return new ExchangeRateDto(baseCurrency, rates.get(baseCurrency));
    }

    @Override
    public void save(final ExchangeRateDto exchangeRateDto) {
        log.info("Saving rates for base currency: {}", exchangeRateDto.baseCurrency());
        rates.put(exchangeRateDto.baseCurrency(), exchangeRateDto.exchangeRates());
    }
}
