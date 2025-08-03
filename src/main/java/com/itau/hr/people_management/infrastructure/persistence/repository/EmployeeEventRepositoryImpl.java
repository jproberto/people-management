package com.itau.hr.people_management.infrastructure.persistence.repository;

import org.springframework.stereotype.Component;

import com.itau.hr.people_management.domain.employee.history.EmployeeEvent;
import com.itau.hr.people_management.domain.employee.repository.EmployeeEventRepository;
import com.itau.hr.people_management.infrastructure.persistence.entity.EmployeeEventJpaEntity;

@Component
public class EmployeeEventRepositoryImpl implements EmployeeEventRepository {

    private final JpaEmployeeEventRepository jpaRepository;
 
    public EmployeeEventRepositoryImpl(JpaEmployeeEventRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(EmployeeEvent employeeEvent) {
        EmployeeEventJpaEntity entity = EmployeeEventJpaEntity.builder()
                .id(employeeEvent.getId()) 
                .employeeId(employeeEvent.getEmployeeId())
                .eventType(employeeEvent.getEventType())
                .occurredOn(employeeEvent.getOccurredOn())
                .description(employeeEvent.getDescription())
                .eventData(employeeEvent.getEventData())
                .build();
        jpaRepository.save(entity);
    }

}
