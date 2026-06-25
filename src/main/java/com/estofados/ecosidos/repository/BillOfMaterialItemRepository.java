package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.BillOfMaterialItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillOfMaterialItemRepository extends JpaRepository<BillOfMaterialItem, Long> {

    List<BillOfMaterialItem> findByParentPartIdAndActiveTrue(Long parentPartId);
}
