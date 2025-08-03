package com.itau.hr.people_management.unit.infrastructure.outbox.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.shared.event.DomainEvent;
import com.itau.hr.people_management.infrastructure.outbox.holder.DomainEventsHolder;
import com.itau.hr.people_management.infrastructure.outbox.listener.DomainEventPublisherListener;
import com.itau.hr.people_management.infrastructure.outbox.publisher.OutboxEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("DomainEventPublisherListener Unit Tests")
class DomainEventPublisherListenerTest {

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Mock
    private DomainEvent domainEvent1;

    @Mock
    private DomainEvent domainEvent2;

    private DomainEventPublisherListener listener;

    @BeforeEach
    void setUp() {
        listener = new DomainEventPublisherListener(outboxEventPublisher);
    }

    @Nested
    @DisplayName("HandleDomainEventsAfterCommit Tests")
    class HandleDomainEventsAfterCommitTests {

        @Test
        @DisplayName("Should do nothing when no events available")
        void shouldDoNothingWhenNoEventsAvailable() {
            // Arrange
            try (MockedStatic<DomainEventsHolder> holderMock = mockStatic(DomainEventsHolder.class)) {
                holderMock.when(DomainEventsHolder::getAndClearEvents)
                    .thenReturn(Collections.emptyList());

                // Act
                listener.handleDomainEventsAfterCommit(null);

                // Assert
                verify(outboxEventPublisher, never()).publish(any());
            }
        }

        @Test
        @DisplayName("Should publish all available events")
        void shouldPublishAllAvailableEvents() {
            // Arrange
            List<DomainEvent> events = List.of(domainEvent1, domainEvent2);
            
            try (MockedStatic<DomainEventsHolder> holderMock = mockStatic(DomainEventsHolder.class)) {
                holderMock.when(DomainEventsHolder::getAndClearEvents)
                    .thenReturn(events);

                // Act
                listener.handleDomainEventsAfterCommit(null);

                // Assert
                verify(outboxEventPublisher).publish(domainEvent1);
                verify(outboxEventPublisher).publish(domainEvent2);
            }
        }

        @Test
        @DisplayName("Should continue processing when one event fails")
        void shouldContinueProcessingWhenOneEventFails() {
            // Arrange
            List<DomainEvent> events = List.of(domainEvent1, domainEvent2);
            
            doThrow(new RuntimeException("Publisher error")).when(outboxEventPublisher).publish(domainEvent1);

            try (MockedStatic<DomainEventsHolder> holderMock = mockStatic(DomainEventsHolder.class)) {
                holderMock.when(DomainEventsHolder::getAndClearEvents)
                    .thenReturn(events);

                // Act
                assertDoesNotThrow(() -> listener.handleDomainEventsAfterCommit(null));

                // Assert
                verify(outboxEventPublisher).publish(domainEvent1);
                verify(outboxEventPublisher).publish(domainEvent2);
            }
        }

        @Test
        @DisplayName("Should handle publisher exception gracefully")
        void shouldHandlePublisherExceptionGracefully() {
            // Arrange
            doThrow(new RuntimeException("Outbox error")).when(outboxEventPublisher).publish(domainEvent1);

            try (MockedStatic<DomainEventsHolder> holderMock = mockStatic(DomainEventsHolder.class)) {
                holderMock.when(DomainEventsHolder::getAndClearEvents)
                    .thenReturn(List.of(domainEvent1));

                // Act
                assertDoesNotThrow(() -> listener.handleDomainEventsAfterCommit(null));

                // Assert
                verify(outboxEventPublisher).publish(domainEvent1);
            }
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should handle holder exception")
        void shouldHandleHolderException() {
            // Arrange
            try (MockedStatic<DomainEventsHolder> holderMock = mockStatic(DomainEventsHolder.class)) {
                holderMock.when(DomainEventsHolder::getAndClearEvents)
                    .thenThrow(new RuntimeException("Holder error"));

                // Act & Assert
                assertThrows(RuntimeException.class, () -> {
                    listener.handleDomainEventsAfterCommit(null);
                });

                verify(outboxEventPublisher, never()).publish(any());
            }
        }
    }
}