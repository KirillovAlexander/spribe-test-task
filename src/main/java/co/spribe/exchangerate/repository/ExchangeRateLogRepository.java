package co.spribe.exchangerate.repository;

import co.spribe.exchangerate.model.ExchangeRateLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExchangeRateLogRepository extends CrudRepository<ExchangeRateLog, UUID> {
}