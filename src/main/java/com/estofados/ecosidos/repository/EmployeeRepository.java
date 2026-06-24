package com.estofados.ecosidos.repository;

import com.estofados.ecosidos.domain.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByCode(String code);
}
