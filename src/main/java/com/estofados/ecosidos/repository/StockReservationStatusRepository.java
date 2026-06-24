package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.StockReservationStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationStatusRepository extends JpaRepository<StockReservationStatus, Long> {

    Optional<StockReservationStatus> findByCode(String code);
}
