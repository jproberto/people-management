package com.itau.hr.people_management.application.department.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.itau.hr.people_management.application.department.dto.CreateDepartmentRequest;
import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;
import com.itau.hr.people_management.domain.shared.exception.ConflictException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CreateDepartmentUseCase {
    private final DepartmentRepository departmentRepository;

    public CreateDepartmentUseCase(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentResponse execute(CreateDepartmentRequest request) {
        if (departmentRepository.findByCostCenterCode(request.getCostCenterCode()).isPresent()) {
            throw new ConflictException("error.department.costcenter.exists", request.getCostCenterCode());
        }

        Department department = Department.create(
            UUID.randomUUID(),
            request.getName(),
            request.getCostCenterCode()
        );

        Department savedDepartment = departmentRepository.save(department);

        return new DepartmentResponse(savedDepartment);
    }
}
