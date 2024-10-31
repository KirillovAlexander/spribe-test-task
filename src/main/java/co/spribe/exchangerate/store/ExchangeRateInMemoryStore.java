package co.spribe.exchangerate.store;

import co.spribe.exchangerate.dto.ExchangeRateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Component
public class ExchangeRateInMemoryStore implements ExchangeRateStore {
    private final Map<String, Map<String, BigDecimal>> rates = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public ExchangeRateDto get(final String baseCurrency) {
        lock.readLock().lock();
        try {
            log.info("Retrieving rates for base currency: {}", baseCurrency);
            return new ExchangeRateDto(baseCurrency, rates.get(baseCurrency));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(final ExchangeRateDto exchangeRateDto) {
        lock.writeLock().lock();
        try {
            log.info("Saving rates for base currency: {}", exchangeRateDto.baseCurrency());
            rates.put(exchangeRateDto.baseCurrency(), exchangeRateDto.exchangeRates());
        } finally {
            lock.writeLock().unlock();
        }
    }
}