package co.spribe.exchangerate.controller;

import co.spribe.exchangerate.base.IntegrationTestBase;
import co.spribe.exchangerate.dto.ExchangeRateDto;
import co.spribe.exchangerate.model.Currency;
import co.spribe.exchangerate.repository.CurrencyRepository;
import co.spribe.exchangerate.registry.ExchangeRateRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateRegistry exchangeRateRegistry;

    @BeforeEach
    void setUp() {
        currencyRepository.deleteAll();
    }

    @Test
    void shouldGetExchangeRatesForBaseCurrency() {
        // Given - set up a base currency in the database and store the exchange rates
        final String baseCurrency = "EUR";
        final Map<String, BigDecimal> exchangeRates = Map.of("PLN", BigDecimal.TEN, "GBP", BigDecimal.TWO);

        currencyRepository.save(new Currency(baseCurrency));
        exchangeRateRegistry.save(new ExchangeRateDto(baseCurrency, exchangeRates));

        // When - a GET request is made to retrieve exchange rates for the specified base currency
        webTestClient.get()
                .uri("/api/exchange-rates/" + baseCurrency)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()

                // Then - verify that the response contains the correct exchange rates
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExchangeRateDto.class)
                .value(response -> {
                    assertThat(response.baseCurrency()).isEqualTo(baseCurrency);
                    assertThat(response.exchangeRates()).containsExactlyInAnyOrderEntriesOf(exchangeRates);
                });
    }
}
