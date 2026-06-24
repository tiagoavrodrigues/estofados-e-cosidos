package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCode(String code);
}
