package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.ManufacturingOrder;
import com.estofados.ecosidos.domain.Rack;
import com.estofados.ecosidos.domain.RackLocation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RackLocationRepository extends JpaRepository<RackLocation, Long> {

    Optional<RackLocation> findByManufacturingOrderAndRemovedAtIsNull(ManufacturingOrder manufacturingOrder);

    Optional<RackLocation> findByRackAndRemovedAtIsNull(Rack rack);

    List<RackLocation> findByRemovedAtIsNullOrderByIdAsc();
}
