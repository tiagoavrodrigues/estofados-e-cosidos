package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.ManufacturingOrder;
import com.estofados.ecosidos.domain.ManufacturingOrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturingOrderRepository extends JpaRepository<ManufacturingOrder, Long> {

    Optional<ManufacturingOrder> findByCode(String code);

    List<ManufacturingOrder> findByCustomerOrderIdOrderByIdAsc(Long customerOrderId);

    Page<ManufacturingOrder> findByStatus(ManufacturingOrderStatus status, Pageable pageable);
}
