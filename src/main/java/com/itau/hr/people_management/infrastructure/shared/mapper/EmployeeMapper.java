package com.itau.hr.people_management.infrastructure.shared.mapper;

import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.shared.vo.Email;
import com.itau.hr.people_management.infrastructure.persistence.entity.EmployeeJpaEntity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmployeeMapper {
    public static EmployeeJpaEntity toJpaEntity(Employee domainEmployee) {
        if (domainEmployee == null) {
            return null;
        }
        
        return EmployeeJpaEntity.builder()
                .id(domainEmployee.getId())
                .name(domainEmployee.getName())
                .email(domainEmployee.getEmail().getAddress())
                .status(domainEmployee.getStatus())
                .department(DepartmentMapper.toJpaEntity(domainEmployee.getDepartment()))
                .position(PositionMapper.toJpaEntity(domainEmployee.getPosition()))
                .build();
    }

    public static Employee toDomainEntity(EmployeeJpaEntity jpaEmployee) {
        if (jpaEmployee == null) {
            return null;
        }

        return Employee.create(
                jpaEmployee.getId(),
                jpaEmployee.getName(),
                Email.create(jpaEmployee.getEmail()),
                jpaEmployee.getStatus(),
                DepartmentMapper.toDomainEntity(jpaEmployee.getDepartment()),
                PositionMapper.toDomainEntity(jpaEmployee.getPosition())
        );
    }
}
