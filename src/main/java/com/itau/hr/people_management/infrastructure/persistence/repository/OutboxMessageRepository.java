package com.itau.hr.people_management.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;
import com.itau.hr.people_management.infrastructure.persistence.entity.OutboxMessage;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {
    List<OutboxMessage> findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
        List<OutboxMessageStatus> statuses, Instant now, Pageable pageable
    );
}
