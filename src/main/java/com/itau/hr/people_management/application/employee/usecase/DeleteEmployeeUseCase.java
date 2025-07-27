package com.itau.hr.people_management.application.employee.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.domain.employee.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.DomainMessageSource;

@Service
@Transactional
public class DeleteEmployeeUseCase {
    private final EmployeeRepository employeeRepository;
    private final DomainMessageSource messageSource;

    public DeleteEmployeeUseCase(EmployeeRepository employeeRepository, DomainMessageSource messageSource) {
        this.employeeRepository = employeeRepository;
        this.messageSource = messageSource;
    }

    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.id.null"));
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.employee.not.found", id)));

        employeeRepository.delete(employee);
    }
}
