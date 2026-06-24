package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByCode(String code);
}
