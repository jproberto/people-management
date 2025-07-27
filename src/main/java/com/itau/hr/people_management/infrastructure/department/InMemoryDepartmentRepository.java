package com.itau.hr.people_management.infrastructure.department;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.itau.hr.people_management.domain.department.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;

@Repository
public class InMemoryDepartmentRepository implements DepartmentRepository {
    private final Map<UUID, Department> departments = new HashMap<>();

    @Override
    public Department save(Department department) {
        departments.put(department.getId(), department);
        return department;
    }

    @Override
    public Optional<Department> findById(UUID id) {
        return Optional.ofNullable(departments.get(id));
    }

    @Override
    public void delete(Department department) {
        departments.remove(department.getId());
    }

    @Override
    public List<Department> findAll() {
        return new ArrayList<>(departments.values());
    }

    @Override
    public Optional<Department> findByCostCenterCode(String costCenterCode) {
        return departments.values().stream()
                .filter(dept -> dept.getCostCenterCode().equals(costCenterCode))
                .findFirst();
    }
    
}
