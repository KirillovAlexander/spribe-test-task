package co.spribe.exchangerate.controller;

import co.spribe.exchangerate.dto.ExchangeRateDto;
import co.spribe.exchangerate.service.CurrencyService;
import co.spribe.exchangerate.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
@Tag(name = "Exchange Rate API", description = "API for managing exchange rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final CurrencyService currencyService;

    @Operation(
            summary = "Get exchange rates for a base currency",
            description = "Retrieves the current exchange rates for a given base currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange rates retrieved successfully")
    })
    @GetMapping("/{baseCurrency}")
    public Mono<ExchangeRateDto> getExchangeRates(@Parameter(description = "3-letter currency code (e.g., USD)") @PathVariable final String baseCurrency) {
        log.info(">> getExchangeRates, baseCurrency: {}", baseCurrency);
        return currencyService.get(baseCurrency)
                .flatMap(currency -> exchangeRateService.getExchangeRates(currency.code()));
    }
}
