package com.itau.hr.people_management.domain.employee.event;

import com.itau.hr.people_management.domain.shared.event.DomainEvent;

public interface EventPublisher {
     void publish(DomainEvent event);
}
