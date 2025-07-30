package com.itau.hr.people_management.application.department.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.department.entity.Department;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentResponse DTO Tests")
class DepartmentResponseTest {

    @Mock
    private Department departmentMock;

    private UUID departmentId;
    private String departmentName;
    private String costCenterCode;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        departmentName = "IT Department";
        costCenterCode = "IT001";
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create empty instance with no-args constructor")
        void shouldCreateEmptyInstanceWithNoArgsConstructor() {
            // Act
            DepartmentResponse response = new DepartmentResponse();

            // Assert
            assertThat(response, is(notNullValue()));
            assertThat(response.getId(), is(nullValue()));
            assertThat(response.getName(), is(nullValue()));
            assertThat(response.getCostCenterCode(), is(nullValue()));
        }

        @Test
        @DisplayName("Should create instance with all-args constructor")
        void shouldCreateInstanceWithAllArgsConstructor() {
            // Act
            DepartmentResponse response = new DepartmentResponse(departmentId, departmentName, costCenterCode);

            // Assert
            assertThat(response, is(notNullValue()));
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo(departmentName)));
            assertThat(response.getCostCenterCode(), is(equalTo(costCenterCode)));
        }

        @Test
        @DisplayName("Should handle null parameters in all-args constructor")
        void shouldHandleNullParametersInAllArgsConstructor() {
            // Act
            DepartmentResponse response = new DepartmentResponse(null, null, null);

            // Assert
            assertThat(response, is(notNullValue()));
            assertThat(response.getId(), is(nullValue()));
            assertThat(response.getName(), is(nullValue()));
            assertThat(response.getCostCenterCode(), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Department Entity Constructor Tests")
    class DepartmentEntityConstructorTests {

        @Test
        @DisplayName("Should create response from valid department entity")
        void shouldCreateResponseFromValidDepartmentEntity() {
            // Arrange
            when(departmentMock.getId()).thenReturn(departmentId);
            when(departmentMock.getName()).thenReturn(departmentName);
            when(departmentMock.getCostCenterCode()).thenReturn(costCenterCode);

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response, is(notNullValue()));
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo(departmentName)));
            assertThat(response.getCostCenterCode(), is(equalTo(costCenterCode)));

            verify(departmentMock).getId();
            verify(departmentMock).getName();
            verify(departmentMock).getCostCenterCode();
        }

        @Test
        @DisplayName("Should handle department entity with null values")
        void shouldHandleDepartmentEntityWithNullValues() {
            // Arrange
            when(departmentMock.getId()).thenReturn(null);
            when(departmentMock.getName()).thenReturn(null);
            when(departmentMock.getCostCenterCode()).thenReturn(null);

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response, is(notNullValue()));
            assertThat(response.getId(), is(nullValue()));
            assertThat(response.getName(), is(nullValue()));
            assertThat(response.getCostCenterCode(), is(nullValue()));

            verify(departmentMock).getId();
            verify(departmentMock).getName();
            verify(departmentMock).getCostCenterCode();
        }

        @Test
        @DisplayName("Should handle department entity with empty strings")
        void shouldHandleDepartmentEntityWithEmptyStrings() {
            // Arrange
            when(departmentMock.getId()).thenReturn(departmentId);
            when(departmentMock.getName()).thenReturn("");
            when(departmentMock.getCostCenterCode()).thenReturn("");

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response, is(notNullValue()));
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo("")));
            assertThat(response.getCostCenterCode(), is(equalTo("")));
        }

        @Test
        @DisplayName("Should throw exception when department entity is null")
        void shouldThrowExceptionWhenDepartmentEntityIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                new DepartmentResponse(null);
            });
        }

        @Test
        @DisplayName("Should handle department with special characters")
        void shouldHandleDepartmentWithSpecialCharacters() {
            // Arrange
            String specialName = "R&D - Research & Development (Special Projects)";
            String specialCode = "R&D-001_SPECIAL.DEPT";
            
            when(departmentMock.getId()).thenReturn(departmentId);
            when(departmentMock.getName()).thenReturn(specialName);
            when(departmentMock.getCostCenterCode()).thenReturn(specialCode);

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo(specialName)));
            assertThat(response.getCostCenterCode(), is(equalTo(specialCode)));
        }

        @Test
        @DisplayName("Should handle department with unicode characters")
        void shouldHandleDepartmentWithUnicodeCharacters() {
            // Arrange
            String unicodeName = "Departamento de Tecnología e Inovação";
            String unicodeCode = "TECH-BR-ÇÑÁ";
            
            when(departmentMock.getId()).thenReturn(departmentId);
            when(departmentMock.getName()).thenReturn(unicodeName);
            when(departmentMock.getCostCenterCode()).thenReturn(unicodeCode);

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo(unicodeName)));
            assertThat(response.getCostCenterCode(), is(equalTo(unicodeCode)));
        }

        @Test
        @DisplayName("Should handle department with long values")
        void shouldHandleDepartmentWithLongValues() {
            // Arrange
            String longName = "Very Long Department Name ".repeat(10); // ~250+ characters
            String longCode = "VERY-LONG-CODE-".repeat(5); // ~75+ characters
            
            when(departmentMock.getId()).thenReturn(departmentId);
            when(departmentMock.getName()).thenReturn(longName);
            when(departmentMock.getCostCenterCode()).thenReturn(longCode);

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo(longName)));
            assertThat(response.getCostCenterCode(), is(equalTo(longCode)));
        }

        @Test
        @DisplayName("Should handle department with whitespace values")
        void shouldHandleDepartmentWithWhitespaceValues() {
            // Arrange
            String nameWithSpaces = "  IT Department  ";
            String codeWithSpaces = "  IT001  ";
            
            when(departmentMock.getId()).thenReturn(departmentId);
            when(departmentMock.getName()).thenReturn(nameWithSpaces);
            when(departmentMock.getCostCenterCode()).thenReturn(codeWithSpaces);

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo(nameWithSpaces)));
            assertThat(response.getCostCenterCode(), is(equalTo(codeWithSpaces)));
        }
    }

    @Nested
    @DisplayName("Entity Mapping Behavior Tests")
    class EntityMappingBehaviorTests {

        @Test
        @DisplayName("Should correctly map all entity properties")
        void shouldCorrectlyMapAllEntityProperties() {
            // Arrange
            when(departmentMock.getId()).thenReturn(departmentId);
            when(departmentMock.getName()).thenReturn(departmentName);
            when(departmentMock.getCostCenterCode()).thenReturn(costCenterCode);

            // Act
            new DepartmentResponse(departmentMock);

            // Assert - Verify all getters were called exactly once
            verify(departmentMock, times(1)).getId();
            verify(departmentMock, times(1)).getName();
            verify(departmentMock, times(1)).getCostCenterCode();
            
            // Verify no additional interactions
            verifyNoMoreInteractions(departmentMock);
        }

        @Test
        @DisplayName("Should create independent copy from entity")
        void shouldCreateIndependentCopyFromEntity() {
            // Arrange
            when(departmentMock.getId()).thenReturn(departmentId);
            when(departmentMock.getName()).thenReturn(departmentName);
            when(departmentMock.getCostCenterCode()).thenReturn(costCenterCode);

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Modify response (this should not affect the original entity)
            response.setName("Modified Name");
            response.setCostCenterCode("MODIFIED");

            // Assert - Response should have modified values
            assertThat(response.getName(), is(equalTo("Modified Name")));
            assertThat(response.getCostCenterCode(), is(equalTo("MODIFIED")));
            assertThat(response.getId(), is(equalTo(departmentId))); // UUID remains the same

            // Original entity interactions should only be from constructor
            verify(departmentMock, times(1)).getId();
            verify(departmentMock, times(1)).getName();
            verify(departmentMock, times(1)).getCostCenterCode();
        }

        @Test
        @DisplayName("Should handle entity method exceptions gracefully")
        void shouldHandleEntityMethodExceptionsGracefully() {
            // Arrange
            when(departmentMock.getId()).thenThrow(new RuntimeException("ID error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                new DepartmentResponse(departmentMock);
            });

            assertThat(exception.getMessage(), is(equalTo("ID error")));
            verify(departmentMock).getId();
        }

        @Test
        @DisplayName("Should preserve UUID reference from entity")
        void shouldPreserveUuidReferenceFromEntity() {
            // Arrange
            UUID specificUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            when(departmentMock.getId()).thenReturn(specificUuid);
            when(departmentMock.getName()).thenReturn(departmentName);
            when(departmentMock.getCostCenterCode()).thenReturn(costCenterCode);

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response.getId(), is(sameInstance(specificUuid)));
            assertThat(response.getId().toString(), is(equalTo("123e4567-e89b-12d3-a456-426614174000")));
        }
    }

    @Nested
    @DisplayName("DTO Conversion Scenarios Tests")
    class DtoConversionScenariosTests {

        @Test
        @DisplayName("Should handle typical department conversion")
        void shouldHandleTypicalDepartmentConversion() {
            // Arrange
            when(departmentMock.getId()).thenReturn(UUID.randomUUID());
            when(departmentMock.getName()).thenReturn("Human Resources");
            when(departmentMock.getCostCenterCode()).thenReturn("HR001");

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response.getId(), is(notNullValue()));
            assertThat(response.getName(), is(equalTo("Human Resources")));
            assertThat(response.getCostCenterCode(), is(equalTo("HR001")));
        }

        @Test
        @DisplayName("Should handle department with minimum valid data")
        void shouldHandleDepartmentWithMinimumValidData() {
            // Arrange
            when(departmentMock.getId()).thenReturn(UUID.randomUUID());
            when(departmentMock.getName()).thenReturn("IT");
            when(departmentMock.getCostCenterCode()).thenReturn("IT");

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response.getId(), is(notNullValue()));
            assertThat(response.getName(), is(equalTo("IT")));
            assertThat(response.getCostCenterCode(), is(equalTo("IT")));
        }

        @Test
        @DisplayName("Should handle department with maximum length data")
        void shouldHandleDepartmentWithMaximumLengthData() {
            // Arrange
            String maxLengthName = "A".repeat(100); // Assuming max 100 chars
            String maxLengthCode = "B".repeat(50);  // Assuming max 50 chars
            
            when(departmentMock.getId()).thenReturn(UUID.randomUUID());
            when(departmentMock.getName()).thenReturn(maxLengthName);
            when(departmentMock.getCostCenterCode()).thenReturn(maxLengthCode);

            // Act
            DepartmentResponse response = new DepartmentResponse(departmentMock);

            // Assert
            assertThat(response.getId(), is(notNullValue()));
            assertThat(response.getName(), is(equalTo(maxLengthName)));
            assertThat(response.getName().length(), is(equalTo(100)));
            assertThat(response.getCostCenterCode(), is(equalTo(maxLengthCode)));
            assertThat(response.getCostCenterCode().length(), is(equalTo(50)));
        }

        @Test
        @DisplayName("Should handle multiple department conversions")
        void shouldHandleMultipleDepartmentConversions() {
            // Test that the constructor can be called multiple times with different entities
            
            // First department
            Department dept1 = mock(Department.class);
            UUID id1 = UUID.randomUUID();
            when(dept1.getId()).thenReturn(id1);
            when(dept1.getName()).thenReturn("IT Department");
            when(dept1.getCostCenterCode()).thenReturn("IT001");

            // Second department
            Department dept2 = mock(Department.class);
            UUID id2 = UUID.randomUUID();
            when(dept2.getId()).thenReturn(id2);
            when(dept2.getName()).thenReturn("HR Department");
            when(dept2.getCostCenterCode()).thenReturn("HR001");

            // Act
            DepartmentResponse response1 = new DepartmentResponse(dept1);
            DepartmentResponse response2 = new DepartmentResponse(dept2);

            // Assert
            assertThat(response1.getId(), is(equalTo(id1)));
            assertThat(response1.getName(), is(equalTo("IT Department")));
            assertThat(response1.getCostCenterCode(), is(equalTo("IT001")));

            assertThat(response2.getId(), is(equalTo(id2)));
            assertThat(response2.getName(), is(equalTo("HR Department")));
            assertThat(response2.getCostCenterCode(), is(equalTo("HR001")));

            // Responses should be independent
            assertThat(response1.getId(), is(not(equalTo(response2.getId()))));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle rapid successive entity conversions")
        void shouldHandleRapidSuccessiveEntityConversions() {
            // Act & Assert
            for (int i = 0; i < 1000; i++) {
                Department tempDept = mock(Department.class);
                UUID tempId = UUID.randomUUID();
                String tempName = "Department " + i;
                String tempCode = "DEPT" + String.format("%03d", i);

                when(tempDept.getId()).thenReturn(tempId);
                when(tempDept.getName()).thenReturn(tempName);
                when(tempDept.getCostCenterCode()).thenReturn(tempCode);

                DepartmentResponse response = new DepartmentResponse(tempDept);

                assertThat(response.getId(), is(equalTo(tempId)));
                assertThat(response.getName(), is(equalTo(tempName)));
                assertThat(response.getCostCenterCode(), is(equalTo(tempCode)));
            }
        }

        @Test
        @DisplayName("Should handle entity with exactly same UUID")
        void shouldHandleEntityWithExactlySameUuid() {
            // Arrange
            UUID sharedUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            
            Department dept1 = mock(Department.class);
            when(dept1.getId()).thenReturn(sharedUuid);
            when(dept1.getName()).thenReturn("Department 1");
            when(dept1.getCostCenterCode()).thenReturn("DEPT1");

            Department dept2 = mock(Department.class);
            when(dept2.getId()).thenReturn(sharedUuid);
            when(dept2.getName()).thenReturn("Department 2");
            when(dept2.getCostCenterCode()).thenReturn("DEPT2");

            // Act
            DepartmentResponse response1 = new DepartmentResponse(dept1);
            DepartmentResponse response2 = new DepartmentResponse(dept2);

            // Assert
            assertThat(response1.getId(), is(equalTo(response2.getId())));
            assertThat(response1.getId(), is(sameInstance(sharedUuid)));
            assertThat(response2.getId(), is(sameInstance(sharedUuid)));
            
            // But other fields should be different
            assertThat(response1.getName(), is(not(equalTo(response2.getName()))));
            assertThat(response1.getCostCenterCode(), is(not(equalTo(response2.getCostCenterCode()))));
        }

        @Test
        @DisplayName("Should handle concurrent entity conversions")
        void shouldHandleConcurrentEntityConversions() {
            // Arrange
            when(departmentMock.getId()).thenReturn(departmentId);
            when(departmentMock.getName()).thenReturn(departmentName);
            when(departmentMock.getCostCenterCode()).thenReturn(costCenterCode);

            // Act - Simulate concurrent access to the same entity
            DepartmentResponse[] responses = new DepartmentResponse[10];
            for (int i = 0; i < 10; i++) {
                responses[i] = new DepartmentResponse(departmentMock);
            }

            // Assert - All responses should be identical
            for (DepartmentResponse response : responses) {
                assertThat(response.getId(), is(equalTo(departmentId)));
                assertThat(response.getName(), is(equalTo(departmentName)));
                assertThat(response.getCostCenterCode(), is(equalTo(costCenterCode)));
            }

            // Verify entity was called the expected number of times
            verify(departmentMock, times(10)).getId();
            verify(departmentMock, times(10)).getName();
            verify(departmentMock, times(10)).getCostCenterCode();
        }
    }
}
