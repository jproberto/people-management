package com.itau.hr.people_management.infrastructure.employee.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.itau.hr.people_management.infrastructure.employee.entity.EmployeeJpaEntity;

@Repository
public interface JpaEmployeeRepository extends JpaRepository<EmployeeJpaEntity, UUID>, 
                                                JpaSpecificationExecutor<EmployeeJpaEntity> {
                                                    
    Optional<EmployeeJpaEntity> findByEmail(String email);
}
