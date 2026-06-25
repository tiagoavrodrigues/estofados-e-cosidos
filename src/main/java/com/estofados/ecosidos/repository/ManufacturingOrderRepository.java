package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.ManufacturingOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturingOrderRepository extends JpaRepository<ManufacturingOrder, Long> {

    Optional<ManufacturingOrder> findByCode(String code);

    List<ManufacturingOrder> findByCustomerOrderIdOrderByIdAsc(Long customerOrderId);
}
