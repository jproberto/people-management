package com.itau.hr.people_management.domain.employee.repository;

import com.itau.hr.people_management.domain.employee.history.EmployeeEvent;

public interface EmployeeEventRepository {
    void save(EmployeeEvent employeeEvent);
}
