package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.RawMaterial;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {

    Optional<RawMaterial> findByCode(String code);
}
