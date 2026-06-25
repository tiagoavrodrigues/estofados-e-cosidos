package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.StockReservation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    Optional<StockReservation> findByCode(String code);

    List<StockReservation> findByRawMaterialIdAndUnitAndStatusCodeIn(
            Long rawMaterialId,
            String unit,
            List<String> statusCodes);
}
