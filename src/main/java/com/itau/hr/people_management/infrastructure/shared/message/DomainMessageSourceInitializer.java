package com.itau.hr.people_management.infrastructure.shared.message;

import org.springframework.stereotype.Component;

import com.itau.hr.people_management.domain.department.Department;
import com.itau.hr.people_management.domain.employee.Employee;
import com.itau.hr.people_management.domain.position.Position;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.domain.shared.vo.Email;
import com.itau.hr.people_management.interfaces.shared.exception_handler.GlobalExceptionHandler;

import jakarta.annotation.PostConstruct;

@Component
public class DomainMessageSourceInitializer {
    private final DomainMessageSource domainMessageSource;

    public DomainMessageSourceInitializer(DomainMessageSource domainMessageSource) {
        this.domainMessageSource = domainMessageSource;
    }

    @PostConstruct
    public void init() {
        Department.setMessageSource(domainMessageSource);
        Position.setMessageSource(domainMessageSource);
        Employee.setMessageSource(domainMessageSource);
        Email.setMessageSource(domainMessageSource);
        GlobalExceptionHandler.setMessageSource(domainMessageSource);
    }
}
