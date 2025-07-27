package com.itau.hr.people_management.application.employee.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.domain.employee.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.DomainMessageSource;

@Service
@Transactional(readOnly = true)
public class GetEmployeeUseCase {
    private final EmployeeRepository employeeRepository;
    private final DomainMessageSource messageSource;

    public GetEmployeeUseCase(EmployeeRepository employeeRepository, DomainMessageSource messageSource) {
        this.employeeRepository = employeeRepository;
        this.messageSource = messageSource;
    }

    public EmployeeResponse getById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.id.null"));
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.employee.not.found", id)));

        return new EmployeeResponse(employee);
    }

    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeResponse::new)
                .toList();
    }
}
