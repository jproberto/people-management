package com.itau.hr.people_management.infrastructure.persistence.entity;

import java.util.UUID;

import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "positions",
        uniqueConstraints = { 
                @UniqueConstraint(columnNames = {"title", "position_level"}) 
            })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_level", nullable = false, length = 50)
    private PositionLevel positionLevel;
}
