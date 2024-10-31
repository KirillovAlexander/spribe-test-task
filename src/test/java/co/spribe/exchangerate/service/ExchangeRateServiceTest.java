package co.spribe.exchangerate.service;

import co.spribe.exchangerate.dto.ExchangeRateDto;
import co.spribe.exchangerate.integration.ExchangeRateProvider;
import co.spribe.exchangerate.model.ExchangeRateLog;
import co.spribe.exchangerate.store.ExchangeRateStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateLogService exchangeRateLogService;

    @Mock
    private ExchangeRateStore exchangeRateStore;

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Captor
    private ArgumentCaptor<List<ExchangeRateLog>> captor;

    @Test
    void shouldReturnExchangeRates_whenAvailable() {
        // Given - a base currency and corresponding exchange rates
        String baseCurrency = "EUR";
        ExchangeRateDto exchangeRateDto = new ExchangeRateDto(baseCurrency, Map.of("PLN", BigDecimal.TEN, "GBP", BigDecimal.TWO));
        when(exchangeRateStore.get(baseCurrency)).thenReturn(exchangeRateDto);

        // When - calling getExchangeRates
        Mono<ExchangeRateDto> result = exchangeRateService.getExchangeRates(baseCurrency);

        // Then - it should return the exchange rates
        result.subscribe(dto -> {
            assertThat(dto.baseCurrency()).isEqualTo(baseCurrency);
            assertThat(dto.exchangeRates()).containsEntry("PLN", BigDecimal.TEN);
            assertThat(dto.exchangeRates()).containsEntry("GBP", BigDecimal.TWO);
        });

        verify(exchangeRateStore).get(baseCurrency);
    }

    @Test
    void shouldThrowError_whenExchangeRatesNotAvailable() {
        // Given - a base currency
        String baseCurrency = "EUR";
        when(exchangeRateStore.get(baseCurrency)).thenReturn(null);

        // When - calling getExchangeRates
        Mono<ExchangeRateDto> result = exchangeRateService.getExchangeRates(baseCurrency);

        // Then - it should return an error
        result.subscribe(
                dto -> Assertions.fail("Expected error but got a result: " + dto),
                error -> assertThat(error).hasMessage("Exchange rates not available for currency: " + baseCurrency)
        );

        verify(exchangeRateStore).get(baseCurrency);
    }

    @Test
    void shouldUpdateExchangeRates_andSaveLogs() {
        // Given - a base currency and exchange rates from the provider
        String baseCurrency = "EUR";
        ExchangeRateDto exchangeRateDto = new ExchangeRateDto(baseCurrency, Map.of("PLN", BigDecimal.TEN, "GBP", BigDecimal.TWO));
        doReturn(Mono.just(exchangeRateDto)).when(exchangeRateProvider).getExchangeRates(baseCurrency);

        // When - calling updateExchangeRates
        Mono<Void> result = exchangeRateService.updateExchangeRates(baseCurrency);

        // Then - it should save the exchange rates and logs
        result.subscribe();

        verify(exchangeRateProvider).getExchangeRates(baseCurrency);
        verify(exchangeRateStore).save(exchangeRateDto);

        // Capture the logs passed to saveAll
        verify(exchangeRateLogService).saveAll(captor.capture());

        // Assert that the logs have the expected properties
        List<ExchangeRateLog> capturedLogs = captor.getValue();
        assertThat(capturedLogs).isNotNull();
        assertThat(capturedLogs).hasSize(2); // Adjust based on the expected number of logs
        assertThat(capturedLogs).anyMatch(log -> log.baseCurrency().equals(baseCurrency) && log.exchangeRate().equals(BigDecimal.TEN));
        assertThat(capturedLogs).anyMatch(log -> log.baseCurrency().equals(baseCurrency) && log.exchangeRate().equals(BigDecimal.TWO));
    }


    @Test
    void shouldLogError_whenUpdatingExchangeRatesFails() {
        // Given - a base currency
        String baseCurrency = "EUR";
        RuntimeException providerError = new RuntimeException("Something went wrong on the provider side");
        when(exchangeRateProvider.getExchangeRates(baseCurrency)).thenReturn(Mono.error(providerError));

        // When - calling updateExchangeRates
        Mono<Void> result = exchangeRateService.updateExchangeRates(baseCurrency);

        // Create a variable to hold the error message
        AtomicReference<String> errorMessage = new AtomicReference<>();

        // Then - it should log an error
        result.subscribe(
                null,
                error -> errorMessage.set(error.getMessage())  // Capture the error message
        );

        // Verify that the error message is as expected
        assertThat(errorMessage.get()).isEqualTo("Something went wrong on the provider side");

        // Verify that the provider method was called
        verify(exchangeRateProvider).getExchangeRates(baseCurrency);
        // Verify that save methods were never called
        verify(exchangeRateStore, never()).save(any());
        verify(exchangeRateLogService, never()).saveAll(any());
    }


}

