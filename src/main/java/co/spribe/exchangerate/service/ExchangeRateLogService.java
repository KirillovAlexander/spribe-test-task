package co.spribe.exchangerate.service;

import co.spribe.exchangerate.model.ExchangeRateLog;
import co.spribe.exchangerate.repository.ExchangeRateLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateLogService {

    private final ExchangeRateLogRepository exchangeRateLogRepository;

    public void saveAll(final List<ExchangeRateLog> exchangeRateLogs) {
        log.info("Saving {} exchange rate logs.", exchangeRateLogs.size());
        exchangeRateLogRepository.saveAll(exchangeRateLogs);
        log.info("Exchange rate logs saved successfully.");
    }
}
