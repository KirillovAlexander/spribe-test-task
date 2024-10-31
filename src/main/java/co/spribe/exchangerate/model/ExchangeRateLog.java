package co.spribe.exchangerate.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("exchange_rate_log")
public record ExchangeRateLog(
        @Id UUID id,
        String baseCurrency,
        String targetCurrency,
        BigDecimal exchangeRate,
        Instant timestamp
) {
    public ExchangeRateLog(String baseCurrency,
                           String targetCurrency,
                           BigDecimal exchangeRate,
                           Instant timestamp) {
        this(null, baseCurrency, targetCurrency, exchangeRate, timestamp);
    }
}