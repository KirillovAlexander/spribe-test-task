package co.spribe.exchangerate.integration.fixer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record FixerApiResponseDto(
        boolean success,
        long timestamp,
        String base,
        LocalDate date,
        Map<String, BigDecimal> rates
) {
}