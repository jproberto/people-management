package com.itau.hr.people_management.application.employee.usecase;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.infrastructure.outbox.listener.TransactionCompletedEvent;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ReactivateEmployeeUseCase {
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DomainMessageSource messageSource;

    @Transactional
    public void execute(UUID employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.employee.id.null"));
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("error.employee.notfound", employeeId));

        employee.reactivate();
        employeeRepository.save(employee);

        eventPublisher.publishEvent(new TransactionCompletedEvent());
    }
}
