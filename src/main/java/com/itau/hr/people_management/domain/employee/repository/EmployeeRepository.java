package com.itau.hr.people_management.domain.employee.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.itau.hr.people_management.domain.employee.Employee;
import com.itau.hr.people_management.domain.employee.EmployeeSearchCriteria;

public interface EmployeeRepository {
    Employee save(Employee employee);
    Optional<Employee> findById(UUID id);
    void delete(Employee employee);
    List<Employee> findAll();
    List<Employee> search(EmployeeSearchCriteria criteria);
    Optional<Employee> findByEmail(String email);
}
