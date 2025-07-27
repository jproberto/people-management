package com.itau.hr.people_management.infrastructure.employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.itau.hr.people_management.domain.employee.EmployeeStatus;
import com.itau.hr.people_management.infrastructure.employee.entity.EmployeeJpaEntity;

@Repository
public interface JpaEmployeeRepository extends JpaRepository<EmployeeJpaEntity, UUID> {
    @Query("SELECT e FROM EmployeeJpaEntity e WHERE " +
           "(:name IS NULL OR lower(e.name) LIKE lower(concat('%', :name, '%'))) AND " +
           "(:email IS NULL OR lower(e.email) LIKE lower(concat('%', :email, '%'))) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:positionId IS NULL OR e.position.id = :positionId)")
    List<EmployeeJpaEntity> searchEmployees(
            @Param("name") String name,
            @Param("email") String email,
            @Param("status") EmployeeStatus status,
            @Param("departmentId") UUID departmentId,
            @Param("positionId") UUID positionId
    );

    Optional<EmployeeJpaEntity> findByEmail(String email);
}
