package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.RawMaterialOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawMaterialOrderRepository extends JpaRepository<RawMaterialOrder, Long> {

    Optional<RawMaterialOrder> findByCode(String code);
}
