package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.StockStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockStatusRepository extends JpaRepository<StockStatus, Long> {

    Optional<StockStatus> findByCode(String code);
}
