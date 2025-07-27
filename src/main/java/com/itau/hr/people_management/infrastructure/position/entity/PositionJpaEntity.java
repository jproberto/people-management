package com.itau.hr.people_management.infrastructure.position.entity;

import java.util.UUID;

import com.itau.hr.people_management.domain.position.PositionLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    @Column(name = "title", unique = true, nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_level", nullable = false, length = 50)
    private PositionLevel positionLevel;
}
