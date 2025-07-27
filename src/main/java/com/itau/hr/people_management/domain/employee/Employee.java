package com.itau.hr.people_management.domain.employee;

import java.time.LocalDate;
import java.util.UUID;

import com.itau.hr.people_management.domain.department.Department;
import com.itau.hr.people_management.domain.position.Position;
import com.itau.hr.people_management.domain.shared.DomainMessageSource;
import com.itau.hr.people_management.domain.shared.Email;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
public class Employee {
    private static DomainMessageSource messageSource;
    public static void setMessageSource(DomainMessageSource ms) {
        Employee.messageSource = ms;
    }
    
    private UUID id;
    private String name;
    private Email email;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private Department department;
    private Position position;
    
    public static Employee create(UUID id, String name, Email email, LocalDate hireDate, EmployeeStatus status, Department department, Position position) {
        validateId(id);
        validateName(name);
        validateEmail(email);
        validateHireDate(hireDate);
        validateStatus(status, hireDate);
        validateDepartment(department);
        validatePosition(position);

        return new Employee(id, name, email, hireDate, status, department, position);
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.id.null"));
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.name.blank"));
        }
        
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.name.length", 2, 100));
        }
    }

    private static void validateEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.email.null"));
        }
    }

    private static void validateHireDate(LocalDate hireDate) {
        if (hireDate == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.hiredate.null"));
        }
        if (hireDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.hiredate.future"));
        }
    }

    private static void validateStatus(EmployeeStatus status, LocalDate hireDate) {
        if (status == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.status.null"));
        }
        if (status == EmployeeStatus.TERMINATED && hireDate != null && hireDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.status.terminated.hiredate.future"));
        }
    }

    private static void validateDepartment(Department department) {
        if (department == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.department.null"));
        }
    }

    private static void validatePosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.position.null"));
        }
    }
}
