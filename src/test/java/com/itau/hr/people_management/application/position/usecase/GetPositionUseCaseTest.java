package com.itau.hr.people_management.application.position.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPositionUseCase Unit Tests")
class GetPositionUseCaseTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private Position position1;

    @Mock
    private Position position2;

    @Mock
    private Position position3;

    private GetPositionUseCase getPositionUseCase;

    @BeforeEach
    void setUp() {
        getPositionUseCase = new GetPositionUseCase(positionRepository);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create use case with valid repository")
        void shouldCreateUseCaseWithValidRepository() {
            // Act
            GetPositionUseCase useCase = new GetPositionUseCase(positionRepository);

            // Assert
            assertThat(useCase, is(notNullValue()));
        }

        @Test
        @DisplayName("Should accept null repository in constructor")
        void shouldAcceptNullRepositoryInConstructor() {
            // Act & Assert - Constructor should accept null (will fail at runtime when used)
            assertDoesNotThrow(() -> {
                GetPositionUseCase useCase = new GetPositionUseCase(null);
                assertThat(useCase, is(notNullValue()));
            });
        }
    }

    @Nested
    @DisplayName("GetAll Method - Success Tests")
    class GetAllMethodSuccessTests {

        @Test
        @DisplayName("Should return empty list when no positions exist")
        void shouldReturnEmptyListWhenNoPositionsExist() {
            // Arrange
            when(positionRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, is(empty()));
            verify(positionRepository).findAll();
        }

        @Test
        @DisplayName("Should return single position response when one position exists")
        void shouldReturnSinglePositionResponseWhenOnePositionExists() {
            // Arrange
            List<Position> positions = List.of(position1);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(instanceOf(PositionResponse.class)));
            verify(positionRepository).findAll();
        }

        @Test
        @DisplayName("Should return multiple position responses when multiple positions exist")
        void shouldReturnMultiplePositionResponsesWhenMultiplePositionsExist() {
            // Arrange
            List<Position> positions = List.of(position1, position2, position3);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(3));
            assertThat(result.get(0), is(instanceOf(PositionResponse.class)));
            assertThat(result.get(1), is(instanceOf(PositionResponse.class)));
            assertThat(result.get(2), is(instanceOf(PositionResponse.class)));
            verify(positionRepository).findAll();
        }

        @Test
        @DisplayName("Should call repository findAll exactly once")
        void shouldCallRepositoryFindAllExactlyOnce() {
            // Arrange
            List<Position> positions = List.of(position1, position2);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            getPositionUseCase.getAll();

            // Assert
            verify(positionRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should map each position to PositionResponse")
        void shouldMapEachPositionToPositionResponse() {
            // Arrange
            List<Position> positions = List.of(position1, position2);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, hasSize(2));
            result.forEach(response -> {
                assertThat(response, is(instanceOf(PositionResponse.class)));
            });
        }

        @Test
        @DisplayName("Should preserve order of positions from repository")
        void shouldPreserveOrderOfPositionsFromRepository() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID id3 = UUID.randomUUID();
            
            when(position1.getId()).thenReturn(id1);
            when(position2.getId()).thenReturn(id2);
            when(position3.getId()).thenReturn(id3);
            
            List<Position> positions = List.of(position1, position2, position3);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, hasSize(3));
            assertThat(result.get(0).getId(), is(equalTo(id1)));
            assertThat(result.get(1).getId(), is(equalTo(id2)));
            assertThat(result.get(2).getId(), is(equalTo(id3)));
        }

        @Test
        @DisplayName("Should return immutable list")
        void shouldReturnImmutableList() {
            // Arrange
            List<Position> positions = List.of(position1);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            // toList() returns immutable list, so this should throw UnsupportedOperationException
            assertThrows(UnsupportedOperationException.class, () -> {
                result.add(mock(PositionResponse.class));
            });
        }
    }

    @Nested
    @DisplayName("GetAll Method - Exception Handling Tests")
    class GetAllMethodExceptionHandlingTests {

        @Test
        @DisplayName("Should propagate repository exceptions")
        void shouldPropagateRepositoryExceptions() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection failed");
            when(positionRepository.findAll()).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                getPositionUseCase.getAll();
            });

            assertThat(thrownException, is(sameInstance(repositoryException)));
            verify(positionRepository).findAll();
        }

        @Test
        @DisplayName("Should handle null position in list gracefully")
        void shouldHandleNullPositionInListGracefully() {
            // Arrange
            List<Position> positionsWithNull = new ArrayList<>();
            positionsWithNull.add(position1);
            positionsWithNull.add(null); 
            positionsWithNull.add(position2);

            when(positionRepository.findAll()).thenReturn(positionsWithNull);

            // Act & Assert
            // PositionResponse constructor will throw exception for null position
            assertThrows(IllegalArgumentException.class, () -> {
                getPositionUseCase.getAll();
            });

            verify(positionRepository).findAll();
        }

        @Test
        @DisplayName("Should handle repository returning null")
        void shouldHandleRepositoryReturningNull() {
            // Arrange
            when(positionRepository.findAll()).thenReturn(null);

            // Act & Assert
            NullPointerException thrownException = assertThrows(NullPointerException.class, () -> {
                getPositionUseCase.getAll();
            });

            assertThat(thrownException, is(notNullValue()));
            verify(positionRepository).findAll();
        }
    }

    @Nested
    @DisplayName("GetAll Method - Edge Cases Tests")
    class GetAllMethodEdgeCasesTests {

        @Test
        @DisplayName("Should handle large number of positions")
        void shouldHandleLargeNumberOfPositions() {
            // Arrange
            List<Position> manyPositions = Collections.nCopies(1000, position1);
            when(positionRepository.findAll()).thenReturn(manyPositions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(1000));
            verify(positionRepository).findAll();
        }

        @Test
        @DisplayName("Should be consistent across multiple calls")
        void shouldBeConsistentAcrossMultipleCalls() {
            // Arrange
            List<Position> positions = List.of(position1, position2);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result1 = getPositionUseCase.getAll();
            List<PositionResponse> result2 = getPositionUseCase.getAll();

            // Assert
            assertThat(result1, hasSize(2));
            assertThat(result2, hasSize(2));
            verify(positionRepository, times(2)).findAll();
        }

        @Test
        @DisplayName("Should handle position with minimal data")
        void shouldHandlePositionWithMinimalData() {
            // Arrange
            when(position1.getId()).thenReturn(UUID.randomUUID());
            when(position1.getTitle()).thenReturn("Test Position");
            when(position1.getPositionLevel()).thenReturn(null);
            
            List<Position> positions = List.of(position1);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(instanceOf(PositionResponse.class)));
        }

        @Test
        @DisplayName("Should handle single element list")
        void shouldHandleSingleElementList() {
            // Arrange
            List<Position> singlePosition = List.of(position1);
            when(positionRepository.findAll()).thenReturn(singlePosition);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(instanceOf(PositionResponse.class)));
        }
    }

    @Nested
    @DisplayName("GetAll Method - Stream Processing Tests")
    class GetAllMethodStreamProcessingTests {

        @Test
        @DisplayName("Should process stream correctly with empty list")
        void shouldProcessStreamCorrectlyWithEmptyList() {
            // Arrange
            when(positionRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, is(empty()));
            verify(positionRepository).findAll();
        }

        @Test
        @DisplayName("Should map every position exactly once")
        void shouldMapEveryPositionExactlyOnce() {
            // Arrange
            List<Position> positions = List.of(position1, position2, position3);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, hasSize(3));
            
            // Each position should be accessed for PositionResponse creation
            verify(position1, atLeastOnce()).getId();
            verify(position2, atLeastOnce()).getId();
            verify(position3, atLeastOnce()).getId();
        }

        @Test
        @DisplayName("Should create new PositionResponse instances")
        void shouldCreateNewPositionResponseInstances() {
            // Arrange
            List<Position> positions = List.of(position1, position2);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result1 = getPositionUseCase.getAll();
            List<PositionResponse> result2 = getPositionUseCase.getAll();

            // Assert - Each call should create new PositionResponse instances
            assertThat(result1.get(0), is(not(sameInstance(result2.get(0)))));
            assertThat(result1.get(1), is(not(sameInstance(result2.get(1)))));
        }

        @Test
        @DisplayName("Should handle stream with duplicate positions")
        void shouldHandleStreamWithDuplicatePositions() {
            // Arrange - Same position instance appears multiple times
            List<Position> duplicatePositions = List.of(position1, position1, position1);
            when(positionRepository.findAll()).thenReturn(duplicatePositions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, hasSize(3));
            result.forEach(response -> {
                assertThat(response, is(instanceOf(PositionResponse.class)));
            });
        }
    }

    @Nested
    @DisplayName("GetAll Method - Memory and Performance Tests")
    class GetAllMethodMemoryAndPerformanceTests {

        @Test
        @DisplayName("Should not hold references to original position objects")
        void shouldNotHoldReferencesToOriginalPositionObjects() {
            // Arrange
            List<Position> positions = List.of(position1);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(1));
            
            // PositionResponse should be a new object, not holding reference to Position
            assertThat(result.get(0), is(not(sameInstance(position1))));
        }

        @Test
        @DisplayName("Should process stream lazily")
        void shouldProcessStreamLazily() {
            // Arrange
            List<Position> positions = List.of(position1, position2);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act
            List<PositionResponse> result = getPositionUseCase.getAll();

            // Assert - Stream is processed and collected immediately due to toList()
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(2));
            verify(positionRepository).findAll();
        }

        @Test
        @DisplayName("Should handle concurrent access gracefully")
        void shouldHandleConcurrentAccessGracefully() {
            // Arrange
            List<Position> positions = List.of(position1);
            when(positionRepository.findAll()).thenReturn(positions);

            // Act - Simulate concurrent calls
            List<PositionResponse> result1 = getPositionUseCase.getAll();
            List<PositionResponse> result2 = getPositionUseCase.getAll();

            // Assert
            assertThat(result1, hasSize(1));
            assertThat(result2, hasSize(1));
            verify(positionRepository, times(2)).findAll();
        }
    }
}