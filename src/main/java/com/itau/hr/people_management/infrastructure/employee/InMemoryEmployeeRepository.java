package com.itau.hr.people_management.infrastructure.employee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.itau.hr.people_management.domain.employee.Employee;
import com.itau.hr.people_management.domain.employee.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;

@Repository
public class InMemoryEmployeeRepository implements EmployeeRepository {
    private final Map<UUID, Employee> employees = new HashMap<>();

    @Override
    public Employee save(Employee employee) {
        employees.put(employee.getId(), employee);
        return employee;
    }

    @Override
    public Optional<Employee> findById(UUID id) {
        return Optional.ofNullable(employees.get(id));
    }

    @Override
    public void delete(Employee employee) {
        employees.remove(employee.getId()); 
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(employees.values());
    }

    @Override
    public List<Employee> search(EmployeeSearchCriteria criteria) {
        return employees.values().stream()
                .filter(employee -> matchesCriteria(employee, criteria))
                .toList();
    }

    private boolean matchesCriteria(Employee employee, EmployeeSearchCriteria criteria) {
        try {
            matchNameCriteria(employee, criteria);
            matchEmailCriteria(employee, criteria);
            matchStatusCriteria(employee, criteria);
            matchDepartmentCriteria(employee, criteria);
            matchPositionCriteria(employee, criteria);
            return true;
        } catch (CriteriaMismatchException e) {
            return false;
        }
    }

    private void matchNameCriteria(Employee employee, EmployeeSearchCriteria criteria) {
        criteria.getName().ifPresent(name -> {
            String employeeName = employee.getName();
            if (employeeName == null || !employeeName.contains(name)) {
                throw new CriteriaMismatchException();
            }
        });
    }

    private void matchEmailCriteria(Employee employee, EmployeeSearchCriteria criteria) {
        criteria.getEmailAddress().ifPresent(email -> {
            String employeeEmail = employee.getEmail() == null ? null : employee.getEmail().getAddress();
            if (employeeEmail == null || !employeeEmail.equals(email)) {
                throw new CriteriaMismatchException();
            }
        });
    }

    private void matchStatusCriteria(Employee employee, EmployeeSearchCriteria criteria) {
        criteria.getStatus().ifPresent(status -> {
            EmployeeStatus employeeStatus = employee.getStatus();
            if (employeeStatus == null || !employeeStatus.equals(status)) {
                throw new CriteriaMismatchException();
            }
        });
    }

    private void matchDepartmentCriteria(Employee employee, EmployeeSearchCriteria criteria) {
        criteria.getDepartmentId().ifPresent(departmentId -> {
            UUID employeeDepartmentId = employee.getDepartment() != null ? employee.getDepartment().getId() : null;
            if (employeeDepartmentId == null || !employeeDepartmentId.equals(departmentId)) {
                throw new CriteriaMismatchException();
            }
        });

        criteria.getDepartmentName().ifPresent(departmentName -> {
            String employeeDepartmentName = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
            if (employeeDepartmentName == null || !employeeDepartmentName.equals(departmentName)) {
                throw new CriteriaMismatchException();
            }
        });
    }

    private void matchPositionCriteria(Employee employee, EmployeeSearchCriteria criteria) {
        criteria.getPositionId().ifPresent(positionId -> {
            UUID employeePositionId = employee.getPosition() != null ? employee.getPosition().getId() : null;
            if (employeePositionId == null || !employeePositionId.equals(positionId)) {
                throw new CriteriaMismatchException();
            }
        });

        criteria.getPositionTitle().ifPresent(positionTitle -> {
            String employeePositionTitle = employee.getPosition() != null ? employee.getPosition().getTitle() : null;
            if (employeePositionTitle == null || !employeePositionTitle.equals(positionTitle)) {
                throw new CriteriaMismatchException();
            }
        });
    }

    // Custom exception to break out of Optional ifPresent
    private static class CriteriaMismatchException extends RuntimeException {}
}
