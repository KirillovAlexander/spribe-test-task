package co.spribe.exchangerate.schedule;

import co.spribe.exchangerate.base.IntegrationTestBase;
import co.spribe.exchangerate.dto.ExchangeRateDto;
import co.spribe.exchangerate.integration.fixer.FixerExchangeRateProvider;
import co.spribe.exchangerate.model.Currency;
import co.spribe.exchangerate.model.ExchangeRateLog;
import co.spribe.exchangerate.repository.CurrencyRepository;
import co.spribe.exchangerate.repository.ExchangeRateLogRepository;
import co.spribe.exchangerate.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.given;

public class ExchangeRateSchedulerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ExchangeRateScheduler exchangeRateScheduler;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateLogRepository exchangeRateLogRepository;

    @MockBean
    private FixerExchangeRateProvider fixerExchangeRateProvider;


    @BeforeEach
    void setUp() {
        currencyRepository.deleteAll();

        currencyRepository.save(new Currency("PLN"));
        currencyRepository.save(new Currency("EUR"));
    }

    @Test
    void shouldUpdateExchangeRatesForAllCurrencies() {
        // Given - Setup mock behaviors
        setupMockExchangeRates();

        // When - invoking the scheduled task
        exchangeRateScheduler.updateExchangeRatesForAllCurrencies();

        // Then - Wait for the scheduler to complete
        await().untilAsserted(this::verifyExchangeRatesUpdated);

        // And - Verify that the logs have been saved correctly by querying the database
        verifyLogsSavedCorrectly();
    }

    @Test
    void shouldContinueProcessingWhenOneCurrencyUpdateFails() {
        // Given - Setup mock behaviors
        currencyRepository.save(new Currency("USD")); // Adding another currency for testing
        setupMockExchangeRates();

        // Mock a failure for USD currency
        given(fixerExchangeRateProvider.getExchangeRates("USD"))
                .willReturn(Mono.error(new RuntimeException("Update failed for USD")));

        // When - invoking the scheduled task
        exchangeRateScheduler.updateExchangeRatesForAllCurrencies();

        // Then - Wait for the scheduler to complete
        await().untilAsserted(() -> {
            // Verify that the exchange rates for only PLN and EUR were updated successfully
            verifyExchangeRatesUpdated();

            // Verify that the logs for only PLN and EUR have been saved correctly by querying the database
            verifyLogsSavedCorrectly();
        });
    }

    private void setupMockExchangeRates() {
        given(fixerExchangeRateProvider.getExchangeRates("EUR"))
                .willReturn(Mono.just(new ExchangeRateDto("EUR", Map.of(
                        "GBP", BigDecimal.valueOf(2.22),
                        "JPY", BigDecimal.valueOf(3.33),
                        "USD", BigDecimal.valueOf(4.44)
                ))));

        given(fixerExchangeRateProvider.getExchangeRates("PLN"))
                .willReturn(Mono.just(new ExchangeRateDto("PLN", Map.of(
                        "GBP", BigDecimal.valueOf(0.15),
                        "JPY", BigDecimal.valueOf(105.001),
                        "USD", BigDecimal.valueOf(0.57)
                ))));
    }

    private void verifyExchangeRatesUpdated() {
        assertExchangeRates("PLN", Map.of(
                "GBP", BigDecimal.valueOf(0.15),
                "JPY", BigDecimal.valueOf(105.001),
                "USD", BigDecimal.valueOf(0.57)
        ));

        assertExchangeRates("EUR", Map.of(
                "GBP", BigDecimal.valueOf(2.22),
                "JPY", BigDecimal.valueOf(3.33),
                "USD", BigDecimal.valueOf(4.44)
        ));
    }

    private void assertExchangeRates(String baseCurrency, Map<String, BigDecimal> expectedRates) {
        ExchangeRateDto rates = exchangeRateService.getExchangeRates(baseCurrency).block();
        assertThat(rates).isNotNull();
        assertThat(rates.baseCurrency()).isEqualTo(baseCurrency);
        expectedRates.forEach((targetCurrency, expectedRate) -> {
            assertThat(rates.exchangeRates()).containsEntry(targetCurrency, expectedRate);
        });
    }

    private void verifyLogsSavedCorrectly() {
        List<ExchangeRateLog> logs = StreamSupport.stream(exchangeRateLogRepository.findAll().spliterator(), false).toList();
        assertThat(logs).hasSize(6);

        verifyLogsForCurrency(logs, "PLN", Map.of(
                "GBP", BigDecimal.valueOf(0.15),
                "JPY", BigDecimal.valueOf(105.001),
                "USD", BigDecimal.valueOf(0.57)
        ));

        verifyLogsForCurrency(logs, "EUR", Map.of(
                "GBP", BigDecimal.valueOf(2.22),
                "JPY", BigDecimal.valueOf(3.33),
                "USD", BigDecimal.valueOf(4.44)
        ));
    }

    private void verifyLogsForCurrency(List<ExchangeRateLog> logs, String baseCurrency, Map<String, BigDecimal> expectedRates) {
        Map<String, BigDecimal> actualRates = logs.stream()
                .filter(log -> log.baseCurrency().equals(baseCurrency))
                .collect(Collectors.toMap(ExchangeRateLog::targetCurrency, ExchangeRateLog::exchangeRate));

        expectedRates.forEach((targetCurrency, expectedRate) -> {
            assertThat(actualRates).containsKey(targetCurrency);
            assertThat(actualRates.get(targetCurrency)).isEqualByComparingTo(expectedRate);
        });
    }


}
