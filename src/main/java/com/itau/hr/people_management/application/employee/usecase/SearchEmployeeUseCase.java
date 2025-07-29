package com.itau.hr.people_management.application.employee.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.domain.employee.criteria.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;

@Service
@Transactional(readOnly = true)
public class SearchEmployeeUseCase {
    private final EmployeeRepository employeeRepository;

    public SearchEmployeeUseCase(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeResponse> execute(EmployeeSearchCriteria criteria) {
        return employeeRepository.search(criteria)
                                    .stream()
                                    .map(EmployeeResponse::new)
                                    .toList();
    }
}
