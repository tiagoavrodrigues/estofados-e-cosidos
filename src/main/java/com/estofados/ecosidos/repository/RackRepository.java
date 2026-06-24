package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.Rack;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RackRepository extends JpaRepository<Rack, Long> {

    Optional<Rack> findByCode(String code);
}
