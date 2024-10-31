package co.spribe.exchangerate.store;

import co.spribe.exchangerate.dto.ExchangeRateDto;

public interface ExchangeRateStore {

    ExchangeRateDto get(String baseCurrency);

    void save(ExchangeRateDto exchangeRateDto);
}