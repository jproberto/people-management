package com.itau.hr.people_management.infrastructure.department.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "departments") 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false, unique = true)
    private java.util.UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "cost_center_code", nullable = false, length = 50, unique = true)
    private String costCenterCode;
}
