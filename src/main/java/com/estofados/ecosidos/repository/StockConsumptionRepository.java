package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.StockConsumption;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockConsumptionRepository extends JpaRepository<StockConsumption, Long> {

    Optional<StockConsumption> findByCode(String code);
}
