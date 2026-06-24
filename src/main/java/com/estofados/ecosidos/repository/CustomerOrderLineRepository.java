package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.CustomerOrderLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderLineRepository extends JpaRepository<CustomerOrderLine, Long> {

    List<CustomerOrderLine> findByCustomerOrderId(Long customerOrderId);
}
