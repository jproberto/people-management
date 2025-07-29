package com.itau.hr.people_management.infrastructure.employee.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.itau.hr.people_management.infrastructure.employee.entity.EmployeeEventJpaEntity;

@Repository
public interface JpaEmployeeEventRepository extends JpaRepository<EmployeeEventJpaEntity, UUID> {
    
}
