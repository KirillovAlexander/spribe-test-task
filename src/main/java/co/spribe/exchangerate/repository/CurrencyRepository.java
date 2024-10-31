package co.spribe.exchangerate.repository;

import co.spribe.exchangerate.model.Currency;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends CrudRepository<Currency, String> {

    Currency findByCode(final String code);
}