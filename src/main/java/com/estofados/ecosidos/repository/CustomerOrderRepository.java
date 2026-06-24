package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.CustomerOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByCode(String code);
}
