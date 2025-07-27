package com.itau.hr.people_management.application.department.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;

@Service
@Transactional(readOnly = true)
public class GetDepartmentUseCase {
    private final DepartmentRepository departmentRepository;

    public GetDepartmentUseCase(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::new)
                .toList();
    }
}
