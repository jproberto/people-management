package com.itau.hr.people_management.domain.employee.criteria;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;

@DisplayName("EmployeeSearchCriteria Domain Tests")
class EmployeeSearchCriteriaTest {

    private UUID validDepartmentId;
    private UUID validPositionId;
    private String validName;
    private String validEmailAddress;
    private String validDepartmentName;
    private String validPositionTitle;
    private String validPositionLevel;
    private EmployeeStatus validEmployeeStatus;

    @BeforeEach
    void setUp() {
        validDepartmentId = UUID.randomUUID();
        validPositionId = UUID.randomUUID();
        validName = "John Doe";
        validEmailAddress = "john.doe@example.com";
        validDepartmentName = "Engineering";
        validPositionTitle = "Software Developer";
        validPositionLevel = "Senior";
        validEmployeeStatus = EmployeeStatus.ACTIVE;
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create criteria with all fields using builder")
        void shouldCreateCriteriaWithAllFieldsUsingBuilder() {
            // Act
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .name(validName)
                .emailAddress(validEmailAddress)
                .employeeStatus(validEmployeeStatus)
                .departmentId(validDepartmentId)
                .departmentName(validDepartmentName)
                .positionId(validPositionId)
                .positionTitle(validPositionTitle)
                .positionLevel(validPositionLevel)
                .build();

            // Assert
            assertThat(criteria, is(notNullValue()));
            assertThat(criteria.getName().orElse(null), is(equalTo(validName)));
            assertThat(criteria.getEmailAddress().orElse(null), is(equalTo(validEmailAddress)));
            assertThat(criteria.getEmployeeStatus().orElse(null), is(equalTo(validEmployeeStatus)));
            assertThat(criteria.getDepartmentId().orElse(null), is(equalTo(validDepartmentId)));
            assertThat(criteria.getDepartmentName().orElse(null), is(equalTo(validDepartmentName)));
            assertThat(criteria.getPositionId().orElse(null), is(equalTo(validPositionId)));
            assertThat(criteria.getPositionTitle().orElse(null), is(equalTo(validPositionTitle)));
            assertThat(criteria.getPositionLevel().orElse(null), is(equalTo(validPositionLevel)));
        }

        @Test
        @DisplayName("Should create empty criteria using builder")
        void shouldCreateEmptyCriteriaUsingBuilder() {
            // Act
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder().build();

            // Assert
            assertThat(criteria, is(notNullValue()));
            assertThat(criteria.getName().isPresent(), is(false));
            assertThat(criteria.getEmailAddress().isPresent(), is(false));
            assertThat(criteria.getEmployeeStatus().isPresent(), is(false));
            assertThat(criteria.getDepartmentId().isPresent(), is(false));
            assertThat(criteria.getDepartmentName().isPresent(), is(false));
            assertThat(criteria.getPositionId().isPresent(), is(false));
            assertThat(criteria.getPositionTitle().isPresent(), is(false));
            assertThat(criteria.getPositionLevel().isPresent(), is(false));
        }

        @Test
        @DisplayName("Should create criteria with partial fields using builder")
        void shouldCreateCriteriaWithPartialFieldsUsingBuilder() {
            // Act
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .name(validName)
                .employeeStatus(validEmployeeStatus)
                .departmentId(validDepartmentId)
                .build();

            // Assert
            assertThat(criteria.getName().isPresent(), is(true));
            assertThat(criteria.getEmployeeStatus().isPresent(), is(true));
            assertThat(criteria.getDepartmentId().isPresent(), is(true));
            assertThat(criteria.getEmailAddress().isPresent(), is(false));
            assertThat(criteria.getDepartmentName().isPresent(), is(false));
            assertThat(criteria.getPositionId().isPresent(), is(false));
            assertThat(criteria.getPositionTitle().isPresent(), is(false));
            assertThat(criteria.getPositionLevel().isPresent(), is(false));
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterAndSetterTests {

        private EmployeeSearchCriteria criteria;

        @BeforeEach
        void setUp() {
            criteria = EmployeeSearchCriteria.builder().build();
        }

        @Test
        @DisplayName("Should set and get name correctly")
        void shouldSetAndGetNameCorrectly() {
            // Act
            criteria.setName(validName);

            // Assert
            assertThat(criteria.getName().orElse(null), is(equalTo(validName)));
        }

        @Test
        @DisplayName("Should set and get email address correctly")
        void shouldSetAndGetEmailAddressCorrectly() {
            // Act
            criteria.setEmailAddress(validEmailAddress);

            // Assert
            assertThat(criteria.getEmailAddress().orElse(null), is(equalTo(validEmailAddress)));
        }

        @Test
        @DisplayName("Should set and get employee status correctly")
        void shouldSetAndGetEmployeeStatusCorrectly() {
            // Act
            criteria.setEmployeeStatus(validEmployeeStatus);

            // Assert
            assertThat(criteria.getEmployeeStatus().orElse(null), is(equalTo(validEmployeeStatus)));
        }

        @Test
        @DisplayName("Should set and get department ID correctly")
        void shouldSetAndGetDepartmentIdCorrectly() {
            // Act
            criteria.setDepartmentId(validDepartmentId);

            // Assert
            assertThat(criteria.getDepartmentId().orElse(null), is(equalTo(validDepartmentId)));
        }

        @Test
        @DisplayName("Should set and get department name correctly")
        void shouldSetAndGetDepartmentNameCorrectly() {
            // Act
            criteria.setDepartmentName(validDepartmentName);

            // Assert
            assertThat(criteria.getDepartmentName().orElse(null), is(equalTo(validDepartmentName)));
        }

        @Test
        @DisplayName("Should set and get position ID correctly")
        void shouldSetAndGetPositionIdCorrectly() {
            // Act
            criteria.setPositionId(validPositionId);

            // Assert
            assertThat(criteria.getPositionId().orElse(null), is(equalTo(validPositionId)));
        }

        @Test
        @DisplayName("Should set and get position title correctly")
        void shouldSetAndGetPositionTitleCorrectly() {
            // Act
            criteria.setPositionTitle(validPositionTitle);

            // Assert
            assertThat(criteria.getPositionTitle().orElse(null), is(equalTo(validPositionTitle)));
        }

        @Test
        @DisplayName("Should set and get position level correctly")
        void shouldSetAndGetPositionLevelCorrectly() {
            // Act
            criteria.setPositionLevel(validPositionLevel);

            // Assert
            assertThat(criteria.getPositionLevel().orElse(null), is(equalTo(validPositionLevel)));
        }
    }

    @Nested
    @DisplayName("Optional Handling Tests")
    class OptionalHandlingTests {

        @Test
        @DisplayName("Should return empty Optional when name is null")
        void shouldReturnEmptyOptionalWhenNameIsNull() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .name(null)
                .build();

            // Act
            Optional<String> result = criteria.getName();

            // Assert
            assertThat(result.isPresent(), is(false));
            assertThat(result, is(equalTo(Optional.empty())));
        }

        @Test
        @DisplayName("Should return empty Optional when email address is null")
        void shouldReturnEmptyOptionalWhenEmailAddressIsNull() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .emailAddress(null)
                .build();

            // Act
            Optional<String> result = criteria.getEmailAddress();

            // Assert
            assertThat(result.isPresent(), is(false));
            assertThat(result, is(equalTo(Optional.empty())));
        }

        @Test
        @DisplayName("Should return empty Optional when employee status is null")
        void shouldReturnEmptyOptionalWhenEmployeeStatusIsNull() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .employeeStatus(null)
                .build();

            // Act
            Optional<EmployeeStatus> result = criteria.getEmployeeStatus();

            // Assert
            assertThat(result.isPresent(), is(false));
            assertThat(result, is(equalTo(Optional.empty())));
        }

        @Test
        @DisplayName("Should return empty Optional when department ID is null")
        void shouldReturnEmptyOptionalWhenDepartmentIdIsNull() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .departmentId(null)
                .build();

            // Act
            Optional<UUID> result = criteria.getDepartmentId();

            // Assert
            assertThat(result.isPresent(), is(false));
            assertThat(result, is(equalTo(Optional.empty())));
        }

        @Test
        @DisplayName("Should return empty Optional when department name is null")
        void shouldReturnEmptyOptionalWhenDepartmentNameIsNull() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .departmentName(null)
                .build();

            // Act
            Optional<String> result = criteria.getDepartmentName();

            // Assert
            assertThat(result.isPresent(), is(false));
            assertThat(result, is(equalTo(Optional.empty())));
        }

        @Test
        @DisplayName("Should return empty Optional when position ID is null")
        void shouldReturnEmptyOptionalWhenPositionIdIsNull() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .positionId(null)
                .build();

            // Act
            Optional<UUID> result = criteria.getPositionId();

            // Assert
            assertThat(result.isPresent(), is(false));
            assertThat(result, is(equalTo(Optional.empty())));
        }

        @Test
        @DisplayName("Should return empty Optional when position title is null")
        void shouldReturnEmptyOptionalWhenPositionTitleIsNull() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .positionTitle(null)
                .build();

            // Act
            Optional<String> result = criteria.getPositionTitle();

            // Assert
            assertThat(result.isPresent(), is(false));
            assertThat(result, is(equalTo(Optional.empty())));
        }

        @Test
        @DisplayName("Should return empty Optional when position level is null")
        void shouldReturnEmptyOptionalWhenPositionLevelIsNull() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .positionLevel(null)
                .build();

            // Act
            Optional<String> result = criteria.getPositionLevel();

            // Assert
            assertThat(result.isPresent(), is(false));
            assertThat(result, is(equalTo(Optional.empty())));
        }
    }

    @Nested
    @DisplayName("Employee Status Enum Tests")
    class EmployeeStatusEnumTests {

        @ParameterizedTest
        @EnumSource(EmployeeStatus.class)
        @DisplayName("Should handle all EmployeeStatus enum values")
        void shouldHandleAllEmployeeStatusEnumValues(EmployeeStatus status) {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .employeeStatus(status)
                .build();

            // Act
            Optional<EmployeeStatus> result = criteria.getEmployeeStatus();

            // Assert
            assertThat(result.isPresent(), is(true));
            assertThat(result.get(), is(equalTo(status)));
        }
    }

    @Nested
    @DisplayName("String Field Edge Cases Tests")
    class StringFieldEdgeCasesTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n", "\r"})
        @DisplayName("Should handle empty and whitespace name values")
        void shouldHandleEmptyAndWhitespaceNameValues(String nameValue) {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .name(nameValue)
                .build();

            // Act
            Optional<String> result = criteria.getName();

            // Assert
            if (nameValue == null) {
                assertThat(result.isPresent(), is(false));
            } else {
                assertThat(result.isPresent(), is(true));
                assertThat(result.get(), is(equalTo(nameValue)));
            }
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n", "\r"})
        @DisplayName("Should handle empty and whitespace email address values")
        void shouldHandleEmptyAndWhitespaceEmailAddressValues(String emailValue) {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .emailAddress(emailValue)
                .build();

            // Act
            Optional<String> result = criteria.getEmailAddress();

            // Assert
            if (emailValue == null) {
                assertThat(result.isPresent(), is(false));
            } else {
                assertThat(result.isPresent(), is(true));
                assertThat(result.get(), is(equalTo(emailValue)));
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "A", "VeryLongDepartmentNameThatExceedsNormalLimits"})
        @DisplayName("Should handle various department name values")
        void shouldHandleVariousDepartmentNameValues(String departmentName) {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .departmentName(departmentName)
                .build();

            // Act
            Optional<String> result = criteria.getDepartmentName();

            // Assert
            assertThat(result.isPresent(), is(true));
            assertThat(result.get(), is(equalTo(departmentName)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "Dev", "Senior Software Engineer", "C-Level Executive"})
        @DisplayName("Should handle various position title values")
        void shouldHandleVariousPositionTitleValues(String positionTitle) {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .positionTitle(positionTitle)
                .build();

            // Act
            Optional<String> result = criteria.getPositionTitle();

            // Assert
            assertThat(result.isPresent(), is(true));
            assertThat(result.get(), is(equalTo(positionTitle)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "Jr", "Senior", "Principal", "Staff", "Director"})
        @DisplayName("Should handle various position level values")
        void shouldHandleVariousPositionLevelValues(String positionLevel) {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .positionLevel(positionLevel)
                .build();

            // Act
            Optional<String> result = criteria.getPositionLevel();

            // Assert
            assertThat(result.isPresent(), is(true));
            assertThat(result.get(), is(equalTo(positionLevel)));
        }
    }

    @Nested
    @DisplayName("UUID Field Tests")
    class UuidFieldTests {

        @Test
        @DisplayName("Should handle different UUID values for department ID")
        void shouldHandleDifferentUuidValuesForDepartmentId() {
            // Arrange
            UUID[] uuids = {
                UUID.randomUUID(),
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                UUID.fromString("00000000-0000-0000-0000-000000000000")
            };

            for (UUID uuid : uuids) {
                EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                    .departmentId(uuid)
                    .build();

                // Act
                Optional<UUID> result = criteria.getDepartmentId();

                // Assert
                assertThat(result.isPresent(), is(true));
                assertThat(result.get(), is(equalTo(uuid)));
            }
        }

        @Test
        @DisplayName("Should handle different UUID values for position ID")
        void shouldHandleDifferentUuidValuesForPositionId() {
            // Arrange
            UUID[] uuids = {
                UUID.randomUUID(),
                UUID.fromString("987fcdeb-51a2-43d1-9c67-123456789abc"),
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
            };

            for (UUID uuid : uuids) {
                EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                    .positionId(uuid)
                    .build();

                // Act
                Optional<UUID> result = criteria.getPositionId();

                // Assert
                assertThat(result.isPresent(), is(true));
                assertThat(result.get(), is(equalTo(uuid)));
            }
        }
    }

    @Nested
    @DisplayName("Mutability Tests")
    class MutabilityTests {

        @Test
        @DisplayName("Should allow changing values after creation")
        void shouldAllowChangingValuesAfterCreation() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .name("Initial Name")
                .build();

            // Act
            criteria.setName("Updated Name");
            criteria.setEmailAddress("new.email@example.com");
            criteria.setEmployeeStatus(EmployeeStatus.INACTIVE);

            // Assert
            assertThat(criteria.getName().get(), is(equalTo("Updated Name")));
            assertThat(criteria.getEmailAddress().get(), is(equalTo("new.email@example.com")));
            assertThat(criteria.getEmployeeStatus().get(), is(equalTo(EmployeeStatus.INACTIVE)));
        }

        @Test
        @DisplayName("Should allow setting values to null")
        void shouldAllowSettingValuesToNull() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .name(validName)
                .emailAddress(validEmailAddress)
                .employeeStatus(validEmployeeStatus)
                .departmentId(validDepartmentId)
                .departmentName(validDepartmentName)
                .positionId(validPositionId)
                .positionTitle(validPositionTitle)
                .positionLevel(validPositionLevel)
                .build();

            // Act
            criteria.setName(null);
            criteria.setEmailAddress(null);
            criteria.setEmployeeStatus(null);
            criteria.setDepartmentId(null);
            criteria.setDepartmentName(null);
            criteria.setPositionId(null);
            criteria.setPositionTitle(null);
            criteria.setPositionLevel(null);

            // Assert
            assertThat(criteria.getName().isPresent(), is(false));
            assertThat(criteria.getEmailAddress().isPresent(), is(false));
            assertThat(criteria.getEmployeeStatus().isPresent(), is(false));
            assertThat(criteria.getDepartmentId().isPresent(), is(false));
            assertThat(criteria.getDepartmentName().isPresent(), is(false));
            assertThat(criteria.getPositionId().isPresent(), is(false));
            assertThat(criteria.getPositionTitle().isPresent(), is(false));
            assertThat(criteria.getPositionLevel().isPresent(), is(false));
        }
    }

    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {

        @Test
        @DisplayName("Should not throw exception on toString")
        void shouldNotThrowExceptionOnToString() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .name(validName)
                .emailAddress(validEmailAddress)
                .employeeStatus(validEmployeeStatus)
                .departmentId(validDepartmentId)
                .departmentName(validDepartmentName)
                .positionId(validPositionId)
                .positionTitle(validPositionTitle)
                .positionLevel(validPositionLevel)
                .build();

            // Act & Assert
            assertDoesNotThrow(() -> {
                String result = criteria.toString();
                assertThat(result, is(notNullValue()));
            });
        }

        @Test
        @DisplayName("Should not throw exception on hashCode")
        void shouldNotThrowExceptionOnHashCode() {
            // Arrange
            EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
                .name(validName)
                .build();

            // Act & Assert
            assertDoesNotThrow(criteria::hashCode);
        }

        @Test
        @DisplayName("Should not throw exception on equals")
        void shouldNotThrowExceptionOnEquals() {
            // Arrange
            EmployeeSearchCriteria criteria1 = EmployeeSearchCriteria.builder()
                .name(validName)
                .build();
            EmployeeSearchCriteria criteria2 = EmployeeSearchCriteria.builder()
                .name(validName)
                .build();

            // Act & Assert
            assertDoesNotThrow(() -> {
                criteria1.equals(criteria2);
                // Result can be true or false, just ensuring no exception
            });
        }
    }
}