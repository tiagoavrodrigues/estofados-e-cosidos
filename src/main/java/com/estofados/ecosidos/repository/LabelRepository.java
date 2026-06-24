package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.Label;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByCode(String code);
}
