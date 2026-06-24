package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.CustomerOrderStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderStatusRepository extends JpaRepository<CustomerOrderStatus, Long> {

    Optional<CustomerOrderStatus> findByCode(String code);
}
