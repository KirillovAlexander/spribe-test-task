package co.spribe.exchangerate.service;

import co.spribe.exchangerate.dto.CurrencyDto;
import co.spribe.exchangerate.model.Currency;
import co.spribe.exchangerate.repository.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void shouldReturnCurrency_whenCurrencyExists() {
        // Given - a currency code and the mocked currency repository
        String currencyCode = "USD";
        Currency currency = new Currency(currencyCode);
        when(currencyRepository.findByCode(eq(currencyCode))).thenReturn(currency);

        // When - calling the get method
        Mono<CurrencyDto> result = currencyService.get(currencyCode);

        // Then - it should return the expected CurrencyDto
        assertThat(result.block()).isEqualTo(new CurrencyDto(currencyCode));
        verify(currencyRepository).findByCode(currencyCode);
    }

    @Test
    void shouldReturnError_whenCurrencyDoesNotExist() {
        // Given - a currency code that does not exist
        String currencyCode = "INVALID";
        when(currencyRepository.findByCode(eq(currencyCode))).thenReturn(null);

        // When - calling the get method and blocking to get the result
        Mono<CurrencyDto> result = currencyService.get(currencyCode);

        // Then - it should return an error
        assertThatThrownBy(result::block)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Currency not found: " + currencyCode);
    }

    @Test
    void shouldReturnAllCurrencies() {
        // Given - mocked currencies in the repository
        when(currencyRepository.findAll()).thenReturn(List.of(
                new Currency("USD"),
                new Currency("EUR"),
                new Currency("GBP")
        ));

        // When - calling the getAll method
        Flux<CurrencyDto> result = currencyService.getAll();

        // Then - it should return a Flux of CurrencyDto
        assertThat(result.collectList().block()).hasSize(3);
        assertThat(result.collectList().block()).contains(new CurrencyDto("USD"), new CurrencyDto("EUR"), new CurrencyDto("GBP"));
    }

    @Test
    void shouldSaveCurrency_andUpdateExchangeRates() {
        // Given - a currency DTO to save
        CurrencyDto currencyDto = new CurrencyDto("PLN");
        Currency currency = new Currency(currencyDto.code());
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);
        when(exchangeRateService.updateExchangeRates(eq(currencyDto.code()))).thenReturn(Mono.empty());

        // When - calling the save method
        Mono<CurrencyDto> result = currencyService.save(currencyDto);

        // Then - it should return the saved CurrencyDto
        assertThat(result.block()).isEqualTo(currencyDto);
        verify(currencyRepository).save(any(Currency.class));
        verify(exchangeRateService).updateExchangeRates(eq(currencyDto.code()));
    }

    @Test
    void shouldReturnError_whenSavingCurrencyFails() {
        // Given - a currency DTO to save
        CurrencyDto currencyDto = new CurrencyDto("PLN");
        when(currencyRepository.save(any(Currency.class))).thenThrow(new RuntimeException("Something went wrong"));

        // When - calling the save method
        Mono<CurrencyDto> result = currencyService.save(currencyDto);

        // Then - it should return a RuntimeException
        assertThatThrownBy(result::block)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to save currency: " + currencyDto.code());
    }
}
