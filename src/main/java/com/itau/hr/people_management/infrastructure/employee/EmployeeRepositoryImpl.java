package com.itau.hr.people_management.infrastructure.employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.itau.hr.people_management.domain.employee.Employee;
import com.itau.hr.people_management.domain.employee.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.infrastructure.employee.entity.EmployeeJpaEntity;
import com.itau.hr.people_management.infrastructure.shared.mapper.EmployeeMapper;

@Component
public class EmployeeRepositoryImpl implements EmployeeRepository {

    private final JpaEmployeeRepository jpaEmployeeRepository;

    public EmployeeRepositoryImpl(JpaEmployeeRepository jpaEmployeeRepository) {
        this.jpaEmployeeRepository = jpaEmployeeRepository;
    }

    @Override
    public Optional<Employee> findById(UUID id) {
        return jpaEmployeeRepository.findById(id)
                .map(EmployeeMapper::toDomainEntity);
    }

    @Override
    public List<Employee> findAll() {
        return jpaEmployeeRepository.findAll()
                .stream()
                .map(EmployeeMapper::toDomainEntity)
                .toList();
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeJpaEntity jpaEntity = EmployeeMapper.toJpaEntity(employee);
        return EmployeeMapper.toDomainEntity(jpaEmployeeRepository.save(jpaEntity));
    }

    @Override
    public void delete(Employee employee) {
        jpaEmployeeRepository.delete(EmployeeMapper.toJpaEntity(employee));
    }

    @Override
    public List<Employee> search(EmployeeSearchCriteria criteria) {
        return jpaEmployeeRepository.searchEmployees(
                criteria.getName().orElse(null),
                criteria.getEmailAddress().orElse(null),
                criteria.getStatus().orElse(null),
                criteria.getDepartmentId().orElse(null),
                criteria.getPositionId().orElse(null)
        ).stream()
         .map(EmployeeMapper::toDomainEntity)
         .toList()
        ;
    }

}
