package com.itau.hr.people_management.application.department.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.itau.hr.people_management.application.department.dto.CreateDepartmentRequest;
import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.domain.department.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;
import com.itau.hr.people_management.domain.shared.DomainMessageSource;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CreateDepartmentUseCase {
    private final DepartmentRepository departmentRepository;
    private final DomainMessageSource messageSource;

    public CreateDepartmentUseCase(DepartmentRepository departmentRepository, DomainMessageSource messageSource) {
        this.departmentRepository = departmentRepository;
        this.messageSource = messageSource;
    }

    public DepartmentResponse execute(CreateDepartmentRequest request) {
        if (departmentRepository.findByCostCenterCode(request.getCostCenterCode()).isPresent()) {
            throw new IllegalArgumentException(messageSource.getMessage("error.department.costcenter.exists", request.getCostCenterCode()));
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
