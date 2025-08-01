package com.itau.hr.people_management.unit.infrastructure.outbox.holder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.shared.event.DomainEvent;
import com.itau.hr.people_management.infrastructure.outbox.holder.DomainEventsHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("DomainEventsHolder Unit Tests")
class DomainEventsHolderTest {

    @Mock
    private DomainEvent domainEvent1;

    @Mock
    private DomainEvent domainEvent2;

    @AfterEach
    void tearDown() {
        DomainEventsHolder.getAndClearEvents();
    }

    @Nested
    @DisplayName("AddEvent Tests")
    class AddEventTests {

        @Test
        @DisplayName("Should add and maintain event order")
        void shouldAddAndMaintainEventOrder() {
            // Act
            DomainEventsHolder.addEvent(domainEvent1);
            DomainEventsHolder.addEvent(domainEvent2);

            // Assert
            List<DomainEvent> events = DomainEventsHolder.peekEvents();
            assertThat(events, contains(domainEvent1, domainEvent2));
        }

        @Test
        @DisplayName("Should handle null event")
        void shouldHandleNullEvent() {
            // Act
            assertDoesNotThrow(() -> DomainEventsHolder.addEvent(null));

            // Assert
            assertThat(DomainEventsHolder.peekEvents(), hasSize(1));
        }
    }

    @Nested
    @DisplayName("GetAndClearEvents Tests")
    class GetAndClearEventsTests {

        @Test
        @DisplayName("Should return events and clear holder")
        void shouldReturnEventsAndClearHolder() {
            // Arrange
            DomainEventsHolder.addEvent(domainEvent1);

            // Act
            List<DomainEvent> events = DomainEventsHolder.getAndClearEvents();

            // Assert
            assertThat(events, contains(domainEvent1));
            assertThat(DomainEventsHolder.peekEvents(), is(empty()));
        }

        @Test
        @DisplayName("Should return mutable copy")
        void shouldReturnMutableCopy() {
            // Arrange
            DomainEventsHolder.addEvent(domainEvent1);

            // Act
            List<DomainEvent> events = DomainEventsHolder.getAndClearEvents();

            // Assert
            assertDoesNotThrow(() -> events.add(domainEvent2));
            assertThat(DomainEventsHolder.peekEvents(), is(empty()));
        }
    }

    @Nested
    @DisplayName("PeekEvents Tests")
    class PeekEventsTests {

        @Test
        @DisplayName("Should return events without clearing")
        void shouldReturnEventsWithoutClearing() {
            // Arrange
            DomainEventsHolder.addEvent(domainEvent1);

            // Act
            List<DomainEvent> events = DomainEventsHolder.peekEvents();

            // Assert
            assertThat(events, contains(domainEvent1));
            assertThat(DomainEventsHolder.peekEvents(), hasSize(1));
        }

        @Test
        @DisplayName("Should return unmodifiable list")
        void shouldReturnUnmodifiableList() {
            // Arrange
            DomainEventsHolder.addEvent(domainEvent1);

            // Act
            List<DomainEvent> events = DomainEventsHolder.peekEvents();

            // Assert
            assertThrows(UnsupportedOperationException.class, () -> events.add(domainEvent2));
        }
    }

    @Nested
    @DisplayName("ThreadLocal Behavior Tests")
    class ThreadLocalBehaviorTests {

        @Test
        @DisplayName("Should maintain separate event lists per thread")
        void shouldMaintainSeparateEventListsPerThread() throws Exception {
            // Arrange
            ExecutorService executor = Executors.newFixedThreadPool(2);

            // Act
            CompletableFuture<List<DomainEvent>> thread1Future = CompletableFuture.supplyAsync(() -> {
                DomainEventsHolder.addEvent(domainEvent1);
                return DomainEventsHolder.peekEvents();
            }, executor);

            CompletableFuture<List<DomainEvent>> thread2Future = CompletableFuture.supplyAsync(() -> {
                DomainEventsHolder.addEvent(domainEvent2);
                return DomainEventsHolder.peekEvents();
            }, executor);

            List<DomainEvent> thread1Events = thread1Future.get(5, TimeUnit.SECONDS);
            List<DomainEvent> thread2Events = thread2Future.get(5, TimeUnit.SECONDS);

            // Assert
            assertThat(thread1Events, contains(domainEvent1));
            assertThat(thread2Events, contains(domainEvent2));
            assertThat(DomainEventsHolder.peekEvents(), is(empty()));

            executor.shutdown();
        }

        @Test
        @DisplayName("Should not affect other threads when clearing")
        void shouldNotAffectOtherThreadsWhenClearing() throws Exception {
            // Arrange
            DomainEventsHolder.addEvent(domainEvent1);
            ExecutorService executor = Executors.newSingleThreadExecutor();

            // Act
            CompletableFuture<List<DomainEvent>> otherThreadFuture = CompletableFuture.supplyAsync(() -> {
                DomainEventsHolder.addEvent(domainEvent2);
                return DomainEventsHolder.getAndClearEvents();
            }, executor);

            List<DomainEvent> otherThreadEvents = otherThreadFuture.get(5, TimeUnit.SECONDS);

            // Assert
            assertThat(otherThreadEvents, contains(domainEvent2));
            assertThat(DomainEventsHolder.peekEvents(), contains(domainEvent1));

            executor.shutdown();
        }
    }
}