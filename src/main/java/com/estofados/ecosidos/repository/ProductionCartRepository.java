package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.ProductionCart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionCartRepository extends JpaRepository<ProductionCart, Long> {

    Optional<ProductionCart> findByCode(String code);
}
