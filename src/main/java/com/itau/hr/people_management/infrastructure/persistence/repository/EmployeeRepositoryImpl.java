package com.itau.hr.people_management.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.itau.hr.people_management.domain.employee.criteria.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.infrastructure.persistence.entity.EmployeeJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.specification.EmployeeSpecification;
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
        List<EmployeeJpaEntity> jpaEntities = jpaEmployeeRepository.findAll(EmployeeSpecification.search(criteria));
        return jpaEntities.stream()
                .map(EmployeeMapper::toDomainEntity)
                .toList();
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        return jpaEmployeeRepository.findByEmail(email)
                .map(EmployeeMapper::toDomainEntity);
    }

}
