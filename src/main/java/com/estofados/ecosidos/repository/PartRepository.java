package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.Part;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {

    Optional<Part> findByCode(String code);
}
