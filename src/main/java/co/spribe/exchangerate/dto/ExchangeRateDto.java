package co.spribe.exchangerate.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateDto(String baseCurrency, Map<String, BigDecimal> exchangeRates) {
}
