package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.PartType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartTypeRepository extends JpaRepository<PartType, Long> {

    Optional<PartType> findByCode(String code);
}
