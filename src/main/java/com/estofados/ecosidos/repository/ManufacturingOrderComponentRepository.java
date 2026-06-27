package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.ManufacturingOrderComponent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturingOrderComponentRepository extends JpaRepository<ManufacturingOrderComponent, Long> {

    List<ManufacturingOrderComponent> findByParentManufacturingOrder_IdAndDeletedAtIsNull(Long parentManufacturingOrderId);

    List<ManufacturingOrderComponent> findByComponentManufacturingOrder_IdInAndDeletedAtIsNull(List<Long> componentManufacturingOrderIds);

    boolean existsByComponentManufacturingOrder_IdAndDeletedAtIsNull(Long componentManufacturingOrderId);
}
