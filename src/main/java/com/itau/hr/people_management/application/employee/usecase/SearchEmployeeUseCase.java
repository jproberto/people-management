package com.itau.hr.people_management.application.employee.usecase;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.application.employee.dto.SearchEmployeeRequest;
import com.itau.hr.people_management.domain.employee.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.DomainMessageSource;

@Service
@Transactional(readOnly = true)
public class SearchEmployeeUseCase {
    private final EmployeeRepository employeeRepository;
    private final DomainMessageSource messageSource;

    public SearchEmployeeUseCase(EmployeeRepository employeeRepository, DomainMessageSource messageSource) {
        this.employeeRepository = employeeRepository;
        this.messageSource = messageSource;
    }

    public List<EmployeeResponse> execute(SearchEmployeeRequest request) {
        EmployeeStatus status = null;
        if (request.getEmployeeStatus() != null) {
            try {
                status = EmployeeStatus.valueOf(request.getEmployeeStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(messageSource.getMessage(
                                                                    "validation.employee.status.invalid", 
                                                                    String.join(", ", Arrays.stream(EmployeeStatus.values())
                                                                                             .map(Enum::name)
                                                                                             .toArray(String[]::new)
                                                        )));
            }
        }

        var criteria = EmployeeSearchCriteria.builder()
                .name(request.getName())
                .emailAddress(request.getEmailAddress())
                .status(status)
                .departmentId(request.getDepartmentId())
                .departmentName(request.getDepartmentName())
                .positionId(request.getPositionId())
                .positionTitle(request.getPositionTitle())
                .build();

        return employeeRepository.search(criteria)
                                    .stream()
                                    .map(EmployeeResponse::new)
                                    .toList();
    }
}
