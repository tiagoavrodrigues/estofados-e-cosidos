package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByCode(String code);
}
