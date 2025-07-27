package com.itau.hr.people_management.application.employee.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.application.employee.dto.CreateEmployeeRequest;
import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;
import com.itau.hr.people_management.domain.employee.Employee;
import com.itau.hr.people_management.domain.employee.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;
import com.itau.hr.people_management.domain.shared.DomainMessageSource;
import com.itau.hr.people_management.domain.shared.Email;

@Service
@Transactional
public class CreateEmployeeUseCase {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final DomainMessageSource messageSource;

    public CreateEmployeeUseCase(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, PositionRepository positionRepository, DomainMessageSource messageSource) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.messageSource = messageSource;
    }

    public EmployeeResponse execute(CreateEmployeeRequest request) {
        EmployeeStatus status;
        try {
            status = EmployeeStatus.valueOf(request.getEmployeeStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.status.invalid"));
        }

        var department = findOrThrow(
            departmentRepository.findById(request.getDepartmentId()),
            "validation.employee.department.notfound",
            request.getDepartmentId()
        );

        var position = findOrThrow(
            positionRepository.findById(request.getPositionId()),
            "validation.employee.position.notfound",
            request.getPositionId()
        );

        var email = Email.create(request.getEmail());

        Employee employee = Employee.create(
                                    UUID.randomUUID(),
                                    request.getName(),
                                    email,
                                    request.getHireDate(),
                                    status,
                                    department,
                                    position
                            );

        Employee savedEmployee = employeeRepository.save(employee);

        return new EmployeeResponse(savedEmployee);
    }

    private <T> T findOrThrow(java.util.Optional<T> optional, String messageKey, Object... args) {
        return optional.orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage(messageKey, args)));
    }
}
