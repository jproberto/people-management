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
import com.itau.hr.people_management.domain.shared.exception.ConflictException;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.vo.Email;

@Service
@Transactional
public class CreateEmployeeUseCase {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public CreateEmployeeUseCase(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, PositionRepository positionRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
    }

    public EmployeeResponse execute(CreateEmployeeRequest request) {
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("error.employee.email.exists", request.getEmail());
        }

        var department = findOrThrow(
            departmentRepository.findById(request.getDepartmentId()),
            "error.department.notfound",
            request.getDepartmentId()
        );

        var position = findOrThrow(
            positionRepository.findById(request.getPositionId()),
            "error.position.notfound",
            request.getPositionId()
        );

        Employee employee = Employee.create(
                                    UUID.randomUUID(),
                                    request.getName(),
                                    Email.create(request.getEmail()),
                                    request.getHireDate(),
                                    EmployeeStatus.ACTIVE,
                                    department,
                                    position
                            );

        Employee savedEmployee = employeeRepository.save(employee);

        return new EmployeeResponse(savedEmployee);
    }

    private <T> T findOrThrow(java.util.Optional<T> optional, String messageKey, Object... args) {
        return optional.orElseThrow(() -> new NotFoundException(messageKey, args));
    }
}
