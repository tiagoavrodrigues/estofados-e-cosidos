package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.ManufacturingOrderStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturingOrderStatusRepository extends JpaRepository<ManufacturingOrderStatus, Long> {

    Optional<ManufacturingOrderStatus> findByCode(String code);
}
