package com.itau.hr.people_management.infrastructure.persistence.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.itau.hr.people_management.domain.employee.criteria.EmployeeSearchCriteria;
import com.itau.hr.people_management.infrastructure.persistence.entity.DepartmentJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.entity.EmployeeJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.entity.PositionJpaEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class EmployeeSpecification {

    private EmployeeSpecification() {}

    public static Specification<EmployeeJpaEntity> search(EmployeeSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            if (criteria == null) {
                return criteriaBuilder.conjunction();
            }
            
            List<Predicate> predicates = new ArrayList<>();

            addNamePredicate(criteria, root, criteriaBuilder, predicates);
            addDepartmentPredicate(criteria, root, criteriaBuilder, predicates);
            addPositionPredicate(criteria, root, criteriaBuilder, predicates);
            addEmailPredicate(criteria, root, criteriaBuilder, predicates);
            addStatusPredicate(criteria, root, criteriaBuilder, predicates);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    private static void addNamePredicate(EmployeeSearchCriteria criteria, Root<EmployeeJpaEntity> root, 
                                       CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        criteria.getName()
            .filter(name -> !name.isBlank())
            .ifPresent(name -> predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
                )
            ));
    }

    private static void addDepartmentPredicate(EmployeeSearchCriteria criteria, Root<EmployeeJpaEntity> root,
                                             CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        criteria.getDepartmentId()
            .ifPresent(deptId -> {
                Join<EmployeeJpaEntity, DepartmentJpaEntity> departmentJoin = root.join("department");
                predicates.add(criteriaBuilder.equal(departmentJoin.get("id"), deptId));
            });

        criteria.getDepartmentName()
            .filter(deptName -> !deptName.isBlank())
            .ifPresent(deptName -> {
                Join<EmployeeJpaEntity, DepartmentJpaEntity> departmentJoin = root.join("department");
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(departmentJoin.get("name")),
                    "%" + deptName.toLowerCase() + "%"
                ));
            });
    }

    private static void addPositionPredicate(EmployeeSearchCriteria criteria, Root<EmployeeJpaEntity> root,
                                           CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        String position = "position";

        criteria.getPositionId()
            .ifPresent(posId -> {
                Join<EmployeeJpaEntity, PositionJpaEntity> positionJoin = root.join(position);
                predicates.add(criteriaBuilder.equal(positionJoin.get("id"), posId));
            });

        criteria.getPositionTitle()
            .filter(posTitle -> !posTitle.isBlank())
            .ifPresent(posTitle -> {
                Join<EmployeeJpaEntity, PositionJpaEntity> positionJoin = root.join(position);
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(positionJoin.get("title")),
                    "%" + posTitle.toLowerCase() + "%"
                ));
            });

        criteria.getPositionLevel()
            .ifPresent(posLevel -> {
                Join<EmployeeJpaEntity, PositionJpaEntity> positionJoin = root.join(position);
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(positionJoin.get("positionLevel")), 
                    posLevel.toLowerCase()
                    ));
            });
    }

    private static void addEmailPredicate(EmployeeSearchCriteria criteria, Root<EmployeeJpaEntity> root,
                                        CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        criteria.getEmailAddress()
            .filter(emailAddress -> !emailAddress.isBlank())
            .ifPresent(emailAddress -> predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + emailAddress.toLowerCase() + "%"
                )
            ));
    }

    private static void addStatusPredicate(EmployeeSearchCriteria criteria, Root<EmployeeJpaEntity> root,
                                         CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        criteria.getEmployeeStatus()
            .ifPresent(status -> predicates.add(criteriaBuilder.equal(root.get("status"), status.name())));
    }
}
