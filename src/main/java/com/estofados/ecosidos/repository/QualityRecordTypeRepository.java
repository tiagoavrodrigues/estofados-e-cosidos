package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.QualityRecordType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityRecordTypeRepository extends JpaRepository<QualityRecordType, Long> {

    Optional<QualityRecordType> findByCode(String code);
}
