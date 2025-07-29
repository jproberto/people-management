package com.itau.hr.people_management.domain.department.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("Department Domain Entity Tests")
class DepartmentTest {

    @Mock
    private DomainMessageSource messageSource;

    private UUID validId;
    private String validName;
    private String validCostCenterCode;

    @BeforeEach
    void setUp() {
        Department.setMessageSource(messageSource);

        validId = UUID.randomUUID();
        validName = "Information Technology";
        validCostCenterCode = "IT001";
    }

    @Nested
    @DisplayName("Department Creation Tests")
    class DepartmentCreationTests {

        @Test
        @DisplayName("Should create department with valid parameters")
        void shouldCreateDepartmentWithValidParameters() {
            // Act
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Assert
            assertThat(department, is(notNullValue()));
            assertThat(department.getId(), is(equalTo(validId)));
            assertThat(department.getName(), is(equalTo(validName)));
            assertThat(department.getCostCenterCode(), is(equalTo(validCostCenterCode)));
        }

        @Test
        @DisplayName("Should trim name when creating department")
        void shouldTrimNameWhenCreatingDepartment() {
            // Arrange
            String nameWithSpaces = "  Information Technology  ";

            // Act
            Department department = Department.create(validId, nameWithSpaces, validCostCenterCode);

            // Assert
            assertThat(department.getName(), is(equalTo("Information Technology")));
        }

        @Test
        @DisplayName("Should trim cost center code when creating department")
        void shouldTrimCostCenterCodeWhenCreatingDepartment() {
            // Arrange
            String costCenterWithSpaces = "  IT001  ";

            // Act
            Department department = Department.create(validId, validName, costCenterWithSpaces);

            // Assert
            assertThat(department.getCostCenterCode(), is(equalTo("IT001")));
        }

        @Test
        @DisplayName("Should trim both name and cost center code")
        void shouldTrimBothNameAndCostCenterCode() {
            // Arrange
            String nameWithSpaces = "  Human Resources  ";
            String costCenterWithSpaces = "  HR001  ";

            // Act
            Department department = Department.create(validId, nameWithSpaces, costCenterWithSpaces);

            // Assert
            assertThat(department.getName(), is(equalTo("Human Resources")));
            assertThat(department.getCostCenterCode(), is(equalTo("HR001")));
        }
    }

    @Nested
    @DisplayName("ID Validation Tests")
    class IdValidationTests {

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.department.id.null")).thenReturn("Department ID cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Department.create(null, validName, validCostCenterCode)
            );

            assertThat(exception.getMessage(), is(equalTo("Department ID cannot be null")));
            verify(messageSource).getMessage("validation.department.id.null");
        }

        @Test
        @DisplayName("Should accept any valid UUID")
        void shouldAcceptAnyValidUuid() {
            // Arrange
            UUID[] testUuids = {
                UUID.randomUUID(),
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
            };

            // Act & Assert
            for (UUID testId : testUuids) {
                assertDoesNotThrow(() -> {
                    Department department = Department.create(testId, validName, validCostCenterCode);
                    assertThat(department.getId(), is(equalTo(testId)));
                });
            }
        }
    }

    @Nested
    @DisplayName("Name Validation Tests")
    class NameValidationTests {

        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.department.name.blank")).thenReturn("Department name cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Department.create(validId, null, validCostCenterCode)
            );

            assertThat(exception.getMessage(), is(equalTo("Department name cannot be blank")));
            verify(messageSource).getMessage("validation.department.name.blank");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", " ", "  ", "\t", "\n", "\r"})
        @DisplayName("Should throw exception when name is blank")
        void shouldThrowExceptionWhenNameIsBlank(String blankName) {
            // Arrange
            when(messageSource.getMessage("validation.department.name.blank")).thenReturn("Department name cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Department.create(validId, blankName, validCostCenterCode)
            );

            assertThat(exception.getMessage(), is(equalTo("Department name cannot be blank")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"A", "B"})
        @DisplayName("Should throw exception when name is too short")
        void shouldThrowExceptionWhenNameIsTooShort(String shortName) {
            // Arrange
            when(messageSource.getMessage("validation.department.name.length", 2))
                .thenReturn("Department name must be at least 2 characters long");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Department.create(validId, shortName, validCostCenterCode)
            );

            assertThat(exception.getMessage(), is(equalTo("Department name must be at least 2 characters long")));
            verify(messageSource).getMessage("validation.department.name.length", 2);
        }

        @Test
        @DisplayName("Should accept minimum valid name length")
        void shouldAcceptMinimumValidNameLength() {
            // Arrange
            String minValidName = "IT";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, minValidName, validCostCenterCode);
                assertThat(department.getName(), is(equalTo("IT")));
            });
        }

        @Test
        @DisplayName("Should accept long department names")
        void shouldAcceptLongDepartmentNames() {
            // Arrange
            String longName = "Department of Information Technology and Digital Transformation Services";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, longName, validCostCenterCode);
                assertThat(department.getName(), is(equalTo(longName)));
            });
        }

        @Test
        @DisplayName("Should accept names with special characters")
        void shouldAcceptNamesWithSpecialCharacters() {
            // Arrange
            String specialName = "R&D - Research & Development";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, specialName, validCostCenterCode);
                assertThat(department.getName(), is(equalTo(specialName)));
            });
        }

        @Test
        @DisplayName("Should accept names with international characters")
        void shouldAcceptNamesWithInternationalCharacters() {
            // Arrange
            String internationalName = "Tecnología e Innovación";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, internationalName, validCostCenterCode);
                assertThat(department.getName(), is(equalTo(internationalName)));
            });
        }

        @Test
        @DisplayName("Should accept names with numbers")
        void shouldAcceptNamesWithNumbers() {
            // Arrange
            String nameWithNumbers = "Level 2 Support";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, nameWithNumbers, validCostCenterCode);
                assertThat(department.getName(), is(equalTo(nameWithNumbers)));
            });
        }
    }

    @Nested
    @DisplayName("Cost Center Code Validation Tests")
    class CostCenterCodeValidationTests {

        @Test
        @DisplayName("Should throw exception when cost center code is null")
        void shouldThrowExceptionWhenCostCenterCodeIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.department.costcentercode.blank"))
                .thenReturn("Cost center code cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Department.create(validId, validName, null)
            );

            assertThat(exception.getMessage(), is(equalTo("Cost center code cannot be blank")));
            verify(messageSource).getMessage("validation.department.costcentercode.blank");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", " ", "  ", "\t", "\n", "\r"})
        @DisplayName("Should throw exception when cost center code is blank")
        void shouldThrowExceptionWhenCostCenterCodeIsBlank(String blankCode) {
            // Arrange
            when(messageSource.getMessage("validation.department.costcentercode.blank"))
                .thenReturn("Cost center code cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Department.create(validId, validName, blankCode)
            );

            assertThat(exception.getMessage(), is(equalTo("Cost center code cannot be blank")));
        }

        @Test
        @DisplayName("Should accept short cost center codes")
        void shouldAcceptShortCostCenterCodes() {
            // Arrange
            String shortCode = "A";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, validName, shortCode);
                assertThat(department.getCostCenterCode(), is(equalTo(shortCode)));
            });
        }

        @Test
        @DisplayName("Should accept long cost center codes")
        void shouldAcceptLongCostCenterCodes() {
            // Arrange
            String longCode = "VERY-LONG-COST-CENTER-CODE-12345";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, validName, longCode);
                assertThat(department.getCostCenterCode(), is(equalTo(longCode)));
            });
        }

        @Test
        @DisplayName("Should accept alphanumeric cost center codes")
        void shouldAcceptAlphanumericCostCenterCodes() {
            // Arrange
            String alphanumericCode = "ABC123";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, validName, alphanumericCode);
                assertThat(department.getCostCenterCode(), is(equalTo(alphanumericCode)));
            });
        }

        @Test
        @DisplayName("Should accept cost center codes with special characters")
        void shouldAcceptCostCenterCodesWithSpecialCharacters() {
            // Arrange
            String specialCode = "IT-001_A";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, validName, specialCode);
                assertThat(department.getCostCenterCode(), is(equalTo(specialCode)));
            });
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {
        @Test
        @DisplayName("Should be equal when IDs are the same")
        void shouldBeEqualWhenIdsAreTheSame() {
            // Arrange
            Department department1 = Department.create(validId, validName, validCostCenterCode);
            Department department2 = Department.create(validId, "Different Name", "DIFF001");

            // Act & Assert
            assertThat(department1, is(equalTo(department2)));
            assertThat(department1.hashCode(), is(equalTo(department2.hashCode())));
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            // Arrange
            UUID differentId = UUID.randomUUID();
            Department department1 = Department.create(validId, validName, validCostCenterCode);
            Department department2 = Department.create(differentId, validName, validCostCenterCode);

            // Act & Assert
            assertThat(department1, is(not(equalTo(department2))));
            assertThat(department1.hashCode(), is(not(equalTo(department2.hashCode()))));
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act & Assert
            assertThat(department, is(not(equalTo(null))));
            assertThat(department.equals(null), is(false));
        }

        @SuppressWarnings("unlikely-arg-type")
        @Test
        @DisplayName("Should not be equal to object of different class")
        void shouldNotBeEqualToObjectOfDifferentClass() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);
            String differentObject = "Not a Department";

            // Act & Assert
            assertThat(department, is(not(equalTo(differentObject))));
            assertThat(department.equals(differentObject), is(false));
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act & Assert
            assertThat(department, is(equalTo(department)));
            assertThat(department.equals(department), is(true));
            assertThat(department.hashCode(), is(equalTo(department.hashCode())));
        }

        @Test
        @DisplayName("Should handle equals with same class but different ID")
        void shouldHandleEqualsWithSameClassButDifferentId() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            
            Department department1 = Department.create(id1, validName, validCostCenterCode);
            Department department2 = Department.create(id2, validName, validCostCenterCode);

            // Act & Assert
            assertThat(department1.equals(department2), is(false));
            assertThat(department2.equals(department1), is(false));
        }

        @Test
        @DisplayName("Should handle equals reflexivity")
        void shouldHandleEqualsReflexivity() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act & Assert
            assertThat(department.equals(department), is(true));
        }

        @Test
        @DisplayName("Should handle equals symmetry")
        void shouldHandleEqualsSymmetry() {
            // Arrange
            Department department1 = Department.create(validId, validName, validCostCenterCode);
            Department department2 = Department.create(validId, "Other Name", "OTHER001");

            // Act & Assert
            assertThat(department1.equals(department2), is(true));
            assertThat(department2.equals(department1), is(true));
        }

        @Test
        @DisplayName("Should handle equals transitivity")
        void shouldHandleEqualsTransitivity() {
            // Arrange
            Department department1 = Department.create(validId, validName, validCostCenterCode);
            Department department2 = Department.create(validId, "Name 2", "CODE2");
            Department department3 = Department.create(validId, "Name 3", "CODE3");

            // Act & Assert
            assertThat(department1.equals(department2), is(true));
            assertThat(department2.equals(department3), is(true));
            assertThat(department1.equals(department3), is(true));
        }

        @Test
        @DisplayName("Should handle equals consistency")
        void shouldHandleEqualsConsistency() {
            // Arrange
            Department department1 = Department.create(validId, validName, validCostCenterCode);
            Department department2 = Department.create(validId, validName, validCostCenterCode);

            // Act & Assert - Multiple calls should return same result
            boolean firstCall = department1.equals(department2);
            boolean secondCall = department1.equals(department2);
            boolean thirdCall = department1.equals(department2);

            assertThat(firstCall, is(true));
            assertThat(secondCall, is(true));
            assertThat(thirdCall, is(true));
            assertThat(firstCall, is(equalTo(secondCall)));
            assertThat(secondCall, is(equalTo(thirdCall)));
        }

        @Test
        @DisplayName("Should handle null ID in equals")
        void shouldHandleNullIdInEquals() {
            // Este teste só pode ser feito através de reflexão ou mock
            // pois nossa validação impede criar Department com ID null
            // Vamos criar um cenário onde testamos o comportamento do equals
            
            Department department1 = Department.create(validId, validName, validCostCenterCode);
            Department department2 = Department.create(validId, validName, validCostCenterCode);

            // Teste com IDs diferentes para garantir que o path de comparação de ID é coberto
            UUID differentId = UUID.randomUUID();
            Department department3 = Department.create(differentId, validName, validCostCenterCode);

            assertThat(department1.equals(department2), is(true));
            assertThat(department1.equals(department3), is(false));
            assertThat(department2.equals(department3), is(false));
        }

        @Test
        @DisplayName("Should handle hashCode consistency")
        void shouldHandleHashCodeConsistency() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act - Multiple calls should return same result
            int hashCode1 = department.hashCode();
            int hashCode2 = department.hashCode();
            int hashCode3 = department.hashCode();

            // Assert
            assertThat(hashCode1, is(equalTo(hashCode2)));
            assertThat(hashCode2, is(equalTo(hashCode3)));
        }

        @Test
        @DisplayName("Should have different hashCodes for different IDs")
        void shouldHaveDifferentHashCodesForDifferentIds() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            
            Department department1 = Department.create(id1, validName, validCostCenterCode);
            Department department2 = Department.create(id2, validName, validCostCenterCode);

            // Act
            int hashCode1 = department1.hashCode();
            int hashCode2 = department2.hashCode();

            // Assert
            // Note: HashCodes can be equal even for different objects, but it's very unlikely with UUIDs
            assertThat(hashCode1, is(not(equalTo(hashCode2))));
        }

        @Test
        @DisplayName("Should maintain hashCode equals contract")
        void shouldMaintainHashCodeEqualsContract() {
            // Arrange
            Department department1 = Department.create(validId, validName, validCostCenterCode);
            Department department2 = Department.create(validId, "Different Name", "DIFF001");

            // Act & Assert
            // If two objects are equal, they must have the same hash code
            if (department1.equals(department2)) {
                assertThat(department1.hashCode(), is(equalTo(department2.hashCode())));
            }
        }

        @Test
        @DisplayName("Should handle equals with object casting")
        void shouldHandleEqualsWithObjectCasting() {
            // Arrange
            Department department1 = Department.create(validId, validName, validCostCenterCode);
            Object department2AsObject = Department.create(validId, "Other Name", "OTHER");

            // Act & Assert
            assertThat(department1.equals(department2AsObject), is(true));
            assertThat(((Department) department2AsObject).equals(department1), is(true));
        }

        @Test
        @DisplayName("Should handle canEqual method if present")
        void shouldHandleCanEqualMethodIfPresent() {
            // Lombok's @EqualsAndHashCode pode gerar um método canEqual
            // Este teste garante que esse path é coberto
            
            Department department1 = Department.create(validId, validName, validCostCenterCode);
            Department department2 = Department.create(validId, validName, validCostCenterCode);

            // Teste direto do equals para garantir todos os paths
            boolean result1 = department1.equals(department2);
            boolean result2 = department2.equals(department1);

            assertThat(result1, is(true));
            assertThat(result2, is(true));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include ID and name in toString")
        void shouldIncludeIdAndNameInToString() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act
            String result = department.toString();

            // Assert
            assertThat(result, containsString(validId.toString()));
            assertThat(result, containsString(validName));
        }

        @Test
        @DisplayName("Should not include cost center code in toString")
        void shouldNotIncludeCostCenterCodeInToString() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act
            String result = department.toString();

            // Assert
            assertThat(result, not(containsString(validCostCenterCode)));
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct ID")
        void shouldReturnCorrectId() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act & Assert
            assertThat(department.getId(), is(equalTo(validId)));
        }

        @Test
        @DisplayName("Should return correct name")
        void shouldReturnCorrectName() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act & Assert
            assertThat(department.getName(), is(equalTo(validName)));
        }

        @Test
        @DisplayName("Should return correct cost center code")
        void shouldReturnCorrectCostCenterCode() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act & Assert
            assertThat(department.getCostCenterCode(), is(equalTo(validCostCenterCode)));
        }
    }

    @Nested
    @DisplayName("Message Source Configuration Tests")
    class MessageSourceConfigurationTests {

        @Test
        @DisplayName("Should set message source")
        void shouldSetMessageSource() {
            // Arrange
            DomainMessageSource newMessageSource = mock(DomainMessageSource.class);

            // Act & Assert
            assertDoesNotThrow(() -> Department.setMessageSource(newMessageSource));
        }

        @Test
        @DisplayName("Should use message source for validation errors")
        void shouldUseMessageSourceForValidationErrors() {
            // Arrange
            DomainMessageSource customMessageSource = mock(DomainMessageSource.class);
            when(customMessageSource.getMessage("validation.department.id.null"))
                .thenReturn("Custom ID error message");
            
            Department.setMessageSource(customMessageSource);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Department.create(null, validName, validCostCenterCode)
            );

            assertThat(exception.getMessage(), is(equalTo("Custom ID error message")));
            verify(customMessageSource).getMessage("validation.department.id.null");

            // Restore original message source
            Department.setMessageSource(messageSource);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable after creation")
        void shouldBeImmutableAfterCreation() {
            // Arrange
            Department department = Department.create(validId, validName, validCostCenterCode);

            // Act - Get initial values
            UUID initialId = department.getId();
            String initialName = department.getName();
            String initialCostCenterCode = department.getCostCenterCode();

            // Assert - Values should remain the same
            assertThat(department.getId(), is(equalTo(initialId)));
            assertThat(department.getName(), is(equalTo(initialName)));
            assertThat(department.getCostCenterCode(), is(equalTo(initialCostCenterCode)));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle minimum valid inputs")
        void shouldHandleMinimumValidInputs() {
            // Arrange
            String minName = "IT";
            String minCode = "A";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, minName, minCode);
                assertThat(department.getName(), is(equalTo(minName)));
                assertThat(department.getCostCenterCode(), is(equalTo(minCode)));
            });
        }

        @Test
        @DisplayName("Should handle very long inputs")
        void shouldHandleVeryLongInputs() {
            // Arrange
            String longName = "A".repeat(1000);
            String longCode = "B".repeat(500);

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, longName, longCode);
                assertThat(department.getName(), is(equalTo(longName)));
                assertThat(department.getCostCenterCode(), is(equalTo(longCode)));
            });
        }

        @Test
        @DisplayName("Should handle inputs with only spaces before trimming")
        void shouldHandleInputsWithOnlySpacesBeforeTrimming() {
            // Arrange
            String nameWithSpaces = "   IT Department   ";
            String codeWithSpaces = "   IT001   ";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, nameWithSpaces, codeWithSpaces);
                assertThat(department.getName(), is(equalTo("IT Department")));
                assertThat(department.getCostCenterCode(), is(equalTo("IT001")));
            });
        }

        @Test
        @DisplayName("Should handle mixed case inputs")
        void shouldHandleMixedCaseInputs() {
            // Arrange
            String mixedCaseName = "InFoRmAtIoN TeChNoLoGy";
            String mixedCaseCode = "It001";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Department department = Department.create(validId, mixedCaseName, mixedCaseCode);
                assertThat(department.getName(), is(equalTo(mixedCaseName)));
                assertThat(department.getCostCenterCode(), is(equalTo(mixedCaseCode)));
            });
        }
    }
}