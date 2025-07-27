package com.itau.hr.people_management.infrastructure.department;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.itau.hr.people_management.infrastructure.department.entity.DepartmentJpaEntity;

@Repository
public interface JpaDepartmentRepository extends JpaRepository<DepartmentJpaEntity, UUID>  {
    Optional<DepartmentJpaEntity> findByCostCenterCode(String costCenterCode);
}
