package com.itau.hr.people_management.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;
import com.itau.hr.people_management.infrastructure.persistence.entity.DepartmentJpaEntity;
import com.itau.hr.people_management.infrastructure.shared.mapper.DepartmentMapper;

@Component
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final JpaDepartmentRepository jpaDepartmentRepository;

    public DepartmentRepositoryImpl(JpaDepartmentRepository jpaDepartmentRepository) {
        this.jpaDepartmentRepository = jpaDepartmentRepository;
    }

    @Override
    public Optional<Department> findById(UUID id) {
        return jpaDepartmentRepository.findById(id)
                .map(DepartmentMapper::toDomainEntity);
    }

    @Override
    public Department save(Department department) {
        DepartmentJpaEntity jpaEntity = DepartmentMapper.toJpaEntity(department);
        return DepartmentMapper.toDomainEntity(jpaDepartmentRepository.save(jpaEntity));
    }

    @Override
    public void delete(Department department) {
        jpaDepartmentRepository.delete(DepartmentMapper.toJpaEntity(department));
    }

    @Override
    public List<Department> findAll() {
        return jpaDepartmentRepository.findAll()
                .stream()
                .map(DepartmentMapper::toDomainEntity)
                .toList();
    }

    @Override
    public Optional<Department> findByCostCenterCode(String costCenterCode) {
        return jpaDepartmentRepository.findByCostCenterCode(costCenterCode)
                .map(DepartmentMapper::toDomainEntity);
    }
}
