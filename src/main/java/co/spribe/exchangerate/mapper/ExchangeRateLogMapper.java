package co.spribe.exchangerate.mapper;

import co.spribe.exchangerate.dto.ExchangeRateDto;
import co.spribe.exchangerate.model.ExchangeRateLog;

import java.time.Instant;
import java.util.List;

public class ExchangeRateLogMapper {

    public static List<ExchangeRateLog> toExchangeRateLogs(final String baseCurrency, final ExchangeRateDto exchangeRateDto) {
        return exchangeRateDto.exchangeRates().entrySet().stream()
                .map(entry -> new ExchangeRateLog(
                        baseCurrency,
                        entry.getKey(),
                        entry.getValue(),
                        Instant.now()
                ))
                .toList();
    }
}