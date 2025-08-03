package com.itau.hr.people_management.infrastructure.shared.mapper;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.infrastructure.persistence.entity.DepartmentJpaEntity;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DepartmentMapper {
    public static DepartmentJpaEntity toJpaEntity(Department domainDepartment) {
        if (domainDepartment == null) {
            return null;
        }
        
        return DepartmentJpaEntity.builder()
                .id(domainDepartment.getId())
                .name(domainDepartment.getName())
                .costCenterCode(domainDepartment.getCostCenterCode())
                .build();
    }

    public static Department toDomainEntity(DepartmentJpaEntity jpaDeparment) {
        if (jpaDeparment == null) {
            return null;
        }
        
        return Department.create(
                jpaDeparment.getId(),
                jpaDeparment.getName(),
                jpaDeparment.getCostCenterCode()
        );
    }
}
