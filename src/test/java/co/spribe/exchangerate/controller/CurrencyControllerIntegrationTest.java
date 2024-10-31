package co.spribe.exchangerate.controller;

import co.spribe.exchangerate.base.IntegrationTestBase;
import co.spribe.exchangerate.dto.CurrencyDto;
import co.spribe.exchangerate.dto.ExceptionDto;
import co.spribe.exchangerate.model.Currency;
import co.spribe.exchangerate.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        currencyRepository.deleteAll();
    }

    @Test
    void shouldGetAllCurrencies() {
        // Given - the initial state with pre-saved currencies
        currencyRepository.saveAll(List.of(
                new Currency("USD"),
                new Currency("EUR"),
                new Currency("GBP")
        ));

        // When - a GET request is made to retrieve all currencies
        webTestClient.get()
                .uri("/api/currencies")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()

                // Then - the response should contain the saved currencies in JSON format
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CurrencyDto.class)
                .hasSize(3)
                .contains(new CurrencyDto("USD"), new CurrencyDto("EUR"), new CurrencyDto("GBP"));
    }

    @Test
    void shouldAddCurrency() {
        // Given - a new currency to add and a mocked external response
        CurrencyDto currencyDto = new CurrencyDto("PLN");
        setupStub("PLN_exchange_rates_response.json");

        // When - a POST request is made to add the new currency
        webTestClient.post()
                .uri("/api/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(currencyDto)
                .exchange()

                // Then - the response should confirm the addition and the currency should be persisted
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CurrencyDto.class)
                .value(response -> assertThat(response.code()).isEqualTo("PLN"));

        // And - Verify that the currency was saved in the database
        Currency savedCurrency = currencyRepository.findByCode("PLN");
        assertThat(savedCurrency).isNotNull();
        assertThat(savedCurrency.code()).isEqualTo("PLN");
    }

    @ParameterizedTest
    @ValueSource(strings = {"PLN123", "us", "123", "EURO", "GBP1", "A", "eur", ""})
    void shouldReturnValidationErrorForInvalidCurrencyCodes(String invalidCode) {
        // Given - an invalid currency DTO with a code not matching the 3-letter pattern
        CurrencyDto invalidCurrencyDto = new CurrencyDto(invalidCode);

        // When - a POST request is made to add the currency
        webTestClient.post()
                .uri("/api/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidCurrencyDto)
                .exchange()

                // Then - a BAD_REQUEST status and validation error message should be returned
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExceptionDto.class)
                .value(response -> {
                    if (invalidCode.isEmpty()) {
                        assertThat(response.message()).contains("Currency code cannot be null");
                    } else {
                        assertThat(response.message()).contains("Currency code must be exactly 3 uppercase letters");
                    }
                });
    }

}

