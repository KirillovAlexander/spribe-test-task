package co.spribe.exchangerate.service;
import co.spribe.exchangerate.model.ExchangeRateLog;
import co.spribe.exchangerate.repository.ExchangeRateLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExchangeRateLogServiceTest {

    @Mock
    private ExchangeRateLogRepository exchangeRateLogRepository;

    @InjectMocks
    private ExchangeRateLogService exchangeRateLogService;

    @Test
    void shouldSaveAllExchangeRateLogs() {
        // Given - a list of exchange rate logs to save
        ExchangeRateLog exchangeRateLog1 = new ExchangeRateLog("USD", "PLN", BigDecimal.ONE, Instant.now());
        ExchangeRateLog exchangeRateLog2 = new ExchangeRateLog("PLN", "USD", BigDecimal.ONE, Instant.now());
        List<ExchangeRateLog> logs = Arrays.asList(exchangeRateLog1, exchangeRateLog2);

        // When - calling the saveAll method
        exchangeRateLogService.saveAll(logs);

        // Then - it should call the repository's saveAll method
        verify(exchangeRateLogRepository).saveAll(logs);
    }
}
