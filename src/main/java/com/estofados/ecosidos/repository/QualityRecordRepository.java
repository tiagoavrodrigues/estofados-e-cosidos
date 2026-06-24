package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.QualityRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityRecordRepository extends JpaRepository<QualityRecord, Long> {

    Optional<QualityRecord> findByCode(String code);
}
