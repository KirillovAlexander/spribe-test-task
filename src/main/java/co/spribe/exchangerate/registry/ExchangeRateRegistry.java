package co.spribe.exchangerate.registry;

import co.spribe.exchangerate.dto.ExchangeRateDto;

public interface ExchangeRateRegistry {

    ExchangeRateDto get(String baseCurrency);

    void save(ExchangeRateDto exchangeRateDto);
}