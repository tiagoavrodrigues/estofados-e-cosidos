package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.PartRawMaterialRequirement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRawMaterialRequirementRepository extends JpaRepository<PartRawMaterialRequirement, Long> {

    List<PartRawMaterialRequirement> findByPartIdAndActiveTrue(Long partId);
}
