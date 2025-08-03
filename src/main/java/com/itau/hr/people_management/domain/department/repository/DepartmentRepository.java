package com.itau.hr.people_management.domain.department.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.itau.hr.people_management.domain.department.entity.Department;

public interface DepartmentRepository {
    Department save(Department department);
    Optional<Department> findById(UUID id);
    void delete(Department department);
    List<Department> findAll();
    Optional<Department> findByCostCenterCode(String costCenterCode);
}