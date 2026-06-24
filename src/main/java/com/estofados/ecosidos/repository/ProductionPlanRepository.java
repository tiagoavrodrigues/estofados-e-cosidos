package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {
}
