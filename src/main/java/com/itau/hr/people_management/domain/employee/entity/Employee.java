package com.itau.hr.people_management.domain.employee.entity;

import java.util.UUID;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.domain.shared.vo.Email;
import com.itau.hr.people_management.infrastructure.outbox.holder.DomainEventsHolder;

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
    private EmployeeStatus status;
    private Department department;
    private Position position;
    
    public static Employee create(UUID id, String name, Email email, EmployeeStatus status, Department department, Position position) {
        validateId(id);
        validateName(name);
        validateEmail(email);
        validateStatus(status);
        validateDepartment(department);
        validatePosition(position);

        return new Employee(id, name, email, status, department, position);
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

    private static void validateStatus(EmployeeStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.status.null"));
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

    public void changeStatus(EmployeeStatus newStatus) {
        validateStatusChange(newStatus);

        EmployeeStatus oldStatus = this.status;
        this.status = newStatus;

        publishStatusChangedEvent(oldStatus, newStatus);
    }

    public void reactivate() {
        if (this.status != EmployeeStatus.TERMINATED) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.status.not.terminated"));
        }

        EmployeeStatus oldStatus = this.status;
        this.status = EmployeeStatus.ACTIVE;

        publishStatusChangedEvent(oldStatus, EmployeeStatus.ACTIVE);
    }

    private void validateStatusChange(EmployeeStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.status.null"));
        }
        if (this.status == EmployeeStatus.TERMINATED) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.old.status.terminated"));
        }
    }

    private void publishStatusChangedEvent(EmployeeStatus oldStatus, EmployeeStatus newStatus) {
        DomainEventsHolder.addEvent(new EmployeeStatusChangedEvent(
            this.id,
            oldStatus,
            newStatus
        ));
    }
}
