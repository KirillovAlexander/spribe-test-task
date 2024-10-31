package co.spribe.exchangerate.controller;

import co.spribe.exchangerate.dto.CurrencyDto;
import co.spribe.exchangerate.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/currencies")
@Validated
@RequiredArgsConstructor
@Tag(name = "Currency API", description = "API for managing currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    @Operation(summary = "Get all supported currencies",
            description = "Fetches a list of all currencies supported by the application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of currencies")
    })
    public Flux<CurrencyDto> getAllCurrencies() {
        log.info(">> getAllCurrencies");
        return currencyService.getAll();
    }

    @PostMapping
    @Operation(summary = "Add a new currency",
            description = "Adds a new currency to the list of supported currencies for exchange rate fetching")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency successfully added")
    })
    public Mono<CurrencyDto> addCurrency(@Valid @RequestBody final CurrencyDto currencyDto) {
        log.info(">> addCurrency, currencyDto: {}", currencyDto);
        return currencyService.save(currencyDto);
    }
}
