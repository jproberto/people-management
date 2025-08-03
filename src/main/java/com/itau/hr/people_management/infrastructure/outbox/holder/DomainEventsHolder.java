package com.itau.hr.people_management.infrastructure.outbox.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.itau.hr.people_management.domain.shared.event.DomainEvent;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainEventsHolder {
    private static final ThreadLocal<List<DomainEvent>> currentEvents = ThreadLocal.withInitial(ArrayList::new);

    public static void addEvent(DomainEvent event) {
        currentEvents.get().add(event);
    }

    public static List<DomainEvent> getAndClearEvents() {
        List<DomainEvent> events = new ArrayList<>(currentEvents.get());
        currentEvents.remove();
        return events;
    }

    public static List<DomainEvent> peekEvents() {
        List<DomainEvent> currentEventsPeek = new ArrayList<>(currentEvents.get());
        return (Collections.unmodifiableList(currentEventsPeek));
    }
}
