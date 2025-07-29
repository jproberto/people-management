package com.itau.hr.people_management.domain.shared.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("NotFoundException Domain Exception Tests")
class NotFoundExceptionTest {

    private String validMessageKey;
    private Object[] validArgs;

    @BeforeEach
    void setUp() {
        validMessageKey = "error.notfound.resource";
        validArgs = new Object[]{"User", "id", "123e4567-e89b-12d3-a456-426614174000"};
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message key and args")
        void shouldCreateExceptionWithMessageKeyAndArgs() {
            // Act
            NotFoundException exception = new NotFoundException(validMessageKey, validArgs);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(equalTo(validArgs)));
            assertThat(exception.getMessage(), is(nullValue()));
            assertThat(exception.getCause(), is(nullValue()));
        }

        @Test
        @DisplayName("Should create exception with message key only")
        void shouldCreateExceptionWithMessageKeyOnly() {
            // Act
            NotFoundException exception = new NotFoundException(validMessageKey);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(arrayWithSize(0)));
            assertThat(exception.getMessage(), is(nullValue()));
        }

        @Test
        @DisplayName("Should create exception with null message key")
        void shouldCreateExceptionWithNullMessageKey() {
            // Act
            NotFoundException exception = new NotFoundException(null, validArgs);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(nullValue()));
            assertThat(exception.getArgs(), is(equalTo(validArgs)));
        }

        @Test
        @DisplayName("Should create exception with null args")
        void shouldCreateExceptionWithNullArgs() {
            // Act
            NotFoundException exception = new NotFoundException(validMessageKey, (Object[]) null);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(nullValue()));
        }

        @Test
        @DisplayName("Should create exception with empty args")
        void shouldCreateExceptionWithEmptyArgs() {
            // Act
            NotFoundException exception = new NotFoundException(validMessageKey, new Object[0]);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(arrayWithSize(0)));
        }

        @Test
        @DisplayName("Should create exception with single arg")
        void shouldCreateExceptionWithSingleArg() {
            // Arrange
            String singleArg = "User not found";

            // Act
            NotFoundException exception = new NotFoundException(validMessageKey, singleArg);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(arrayWithSize(1)));
            assertThat(exception.getArgs()[0], is(equalTo(singleArg)));
        }

        @Test
        @DisplayName("Should create exception with multiple args of different types")
        void shouldCreateExceptionWithMultipleArgsOfDifferentTypes() {
            // Arrange
            Object[] mixedArgs = {
                "Department",
                UUID.randomUUID(),
                404,
                false,
                null
            };

            // Act
            NotFoundException exception = new NotFoundException(validMessageKey, mixedArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(equalTo(mixedArgs)));
            assertThat(exception.getArgs(), is(arrayWithSize(5)));
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should extend BusinessException")
        void shouldExtendBusinessException() {
            // Arrange
            NotFoundException exception = new NotFoundException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(BusinessException.class)));
        }

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            // Arrange
            NotFoundException exception = new NotFoundException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(RuntimeException.class)));
        }

        @Test
        @DisplayName("Should be instance of Exception")
        void shouldBeInstanceOfException() {
            // Arrange
            NotFoundException exception = new NotFoundException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(Exception.class)));
        }

        @Test
        @DisplayName("Should be instance of Throwable")
        void shouldBeInstanceOfThrowable() {
            // Arrange
            NotFoundException exception = new NotFoundException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(Throwable.class)));
        }

        @Test
        @DisplayName("Should inherit BusinessException behavior")
        void shouldInheritBusinessExceptionBehavior() {
            // Arrange
            NotFoundException exception = new NotFoundException(validMessageKey, validArgs);
            BusinessException businessException = exception;

            // Act & Assert
            assertThat(businessException.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(businessException.getArgs(), is(equalTo(validArgs)));
        }
    }

    @Nested
    @DisplayName("Throwable Behavior Tests")
    class ThrowableBehaviorTests {

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                throw new NotFoundException(validMessageKey, validArgs);
            });

            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(equalTo(validArgs)));
        }

        @Test
        @DisplayName("Should be catchable as BusinessException")
        void shouldBeCatchableAsBusinessException() {
            // Act & Assert
            BusinessException caughtException = assertThrows(BusinessException.class, () -> {
                throw new NotFoundException(validMessageKey, validArgs);
            });

            assertThat(caughtException, is(instanceOf(NotFoundException.class)));
            assertThat(caughtException.getMessageKey(), is(equalTo(validMessageKey)));
        }

        @Test
        @DisplayName("Should be catchable as RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            // Act & Assert
            RuntimeException caughtException = assertThrows(RuntimeException.class, () -> {
                throw new NotFoundException(validMessageKey, validArgs);
            });

            assertThat(caughtException, is(instanceOf(NotFoundException.class)));
        }

        @Test
        @DisplayName("Should maintain stack trace when thrown")
        void shouldMaintainStackTraceWhenThrown() {
            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                throw new NotFoundException(validMessageKey, validArgs);
            });

            assertThat(exception.getStackTrace(), is(notNullValue()));
            assertThat(exception.getStackTrace().length, is(greaterThan(0)));
        }
    }

    @Nested
    @DisplayName("Message Key Validation Tests")
    class MessageKeyValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
        @DisplayName("Should handle various message key values")
        void shouldHandleVariousMessageKeyValues(String messageKey) {
            // Act
            NotFoundException exception = new NotFoundException(messageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
        }

        @Test
        @DisplayName("Should handle not-found-specific message keys")
        void shouldHandleNotFoundSpecificMessageKeys() {
            // Arrange
            String[] notFoundMessageKeys = {
                "error.notfound.user.id",
                "error.notfound.department.code",
                "error.notfound.position.title",
                "error.notfound.employee.email",
                "error.notfound.resource.general"
            };

            // Act & Assert
            for (String messageKey : notFoundMessageKeys) {
                NotFoundException exception = new NotFoundException(messageKey, "test-resource");
                assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
            }
        }

        @Test
        @DisplayName("Should handle long message keys")
        void shouldHandleLongMessageKeys() {
            // Arrange
            String longMessageKey = "error.notfound." + "a".repeat(1000);

            // Act
            NotFoundException exception = new NotFoundException(longMessageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(longMessageKey)));
        }

        @Test
        @DisplayName("Should handle message keys with special characters")
        void shouldHandleMessageKeysWithSpecialCharacters() {
            // Arrange
            String specialMessageKey = "error.notfound.çñáéíóú@#$%^&*()";

            // Act
            NotFoundException exception = new NotFoundException(specialMessageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(specialMessageKey)));
        }
    }

    @Nested
    @DisplayName("Args Validation Tests")
    class ArgsValidationTests {

        @Test
        @DisplayName("Should handle not-found-related args")
        void shouldHandleNotFoundRelatedArgs() {
            // Arrange
            Object[] notFoundArgs = {
                "User",
                "id",
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "database"
            };

            // Act
            NotFoundException exception = new NotFoundException(validMessageKey, notFoundArgs);

            // Assert
            assertThat(exception.getArgs(), is(equalTo(notFoundArgs)));
            assertThat(exception.getArgs(), is(arrayWithSize(4)));
        }

        @Test
        @DisplayName("Should handle resource identification args")
        void shouldHandleResourceIdentificationArgs() {
            // Arrange
            Object[] resourceArgs = {
                "Department",
                "costCenter",
                "IT001",
                "active departments"
            };

            // Act
            NotFoundException exception = new NotFoundException("error.notfound.department", resourceArgs);

            // Assert
            assertThat(exception.getArgs(), is(equalTo(resourceArgs)));
        }

        @Test
        @DisplayName("Should handle args with null values")
        void shouldHandleArgsWithNullValues() {
            // Arrange
            Object[] argsWithNulls = {"resource", null, "field", null};

            // Act
            NotFoundException exception = new NotFoundException(validMessageKey, argsWithNulls);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(4)));
            assertThat(exception.getArgs()[1], is(nullValue()));
            assertThat(exception.getArgs()[3], is(nullValue()));
        }

        @Test
        @DisplayName("Should handle search criteria args")
        void shouldHandleSearchCriteriaArgs() {
            // Arrange
            Object[] searchArgs = {
                "Employee",
                "email",
                "john.doe@example.com",
                "active employees",
                "HR department"
            };

            // Act
            NotFoundException exception = new NotFoundException("error.notfound.employee", searchArgs);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(5)));
            assertThat(exception.getArgs()[2], is(equalTo("john.doe@example.com")));
        }

        @Test
        @DisplayName("Should handle complex object args")
        void shouldHandleComplexObjectArgs() {
            // Arrange
            Object complexObject = new SearchCriteria("User", "active", "HR");
            Object[] complexArgs = {complexObject, "not found in system"};

            // Act
            NotFoundException exception = new NotFoundException(validMessageKey, complexArgs);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(2)));
            assertThat(exception.getArgs()[0], is(sameInstance(complexObject)));
        }
    }

    @Nested
    @DisplayName("Polymorphism Tests")
    class PolymorphismTests {

        @Test
        @DisplayName("Should work as BusinessException polymorphically")
        void shouldWorkAsBusinessExceptionPolymorphically() {
            // Arrange
            NotFoundException notFoundException = new NotFoundException(validMessageKey, validArgs);
            BusinessException businessException = notFoundException;

            // Act & Assert
            assertThat(businessException, is(instanceOf(NotFoundException.class)));
            assertThat(businessException.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(businessException.getArgs(), is(equalTo(validArgs)));
        }

        @Test
        @DisplayName("Should work as RuntimeException polymorphically")
        void shouldWorkAsRuntimeExceptionPolymorphically() {
            // Arrange
            NotFoundException notFoundException = new NotFoundException(validMessageKey, validArgs);
            RuntimeException runtimeException = notFoundException;

            // Act & Assert
            assertThat(runtimeException, is(instanceOf(NotFoundException.class)));
            NotFoundException castBack = (NotFoundException) runtimeException;
            assertThat(castBack.getMessageKey(), is(equalTo(validMessageKey)));
        }

        @Test
        @DisplayName("Should work as Exception polymorphically")
        void shouldWorkAsExceptionPolymorphically() {
            // Arrange
            NotFoundException notFoundException = new NotFoundException(validMessageKey, validArgs);
            Exception exception = notFoundException;

            // Act & Assert
            assertThat(exception, is(instanceOf(NotFoundException.class)));
            NotFoundException castBack = (NotFoundException) exception;
            assertThat(castBack.getMessageKey(), is(equalTo(validMessageKey)));
        }

        @Test
        @DisplayName("Should work as Throwable polymorphically")
        void shouldWorkAsThrowablePolymorphically() {
            // Arrange
            NotFoundException notFoundException = new NotFoundException(validMessageKey, validArgs);
            Throwable throwable = notFoundException;

            // Act & Assert
            assertThat(throwable, is(instanceOf(NotFoundException.class)));
            NotFoundException castBack = (NotFoundException) throwable;
            assertThat(castBack.getMessageKey(), is(equalTo(validMessageKey)));
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable")
        void shouldBeSerializable() throws Exception {
            // Arrange
            Object[] serializableArgs = {"string", 123, true};
            NotFoundException originalException = new NotFoundException(validMessageKey, serializableArgs);

            // Act
            byte[] serializedData = serialize(originalException);
            NotFoundException deserializedException = deserialize(serializedData);

            // Assert
            assertThat(deserializedException, is(notNullValue()));
            assertThat(deserializedException.getMessageKey(), is(equalTo(originalException.getMessageKey())));
            // Args are transient in BusinessException, so they won't be serialized
            assertThat(deserializedException.getArgs(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle serialization with null values")
        void shouldHandleSerializationWithNullValues() throws Exception {
            // Arrange
            NotFoundException originalException = new NotFoundException(null, (Object[]) null);

            // Act
            byte[] serializedData = serialize(originalException);
            NotFoundException deserializedException = deserialize(serializedData);

            // Assert
            assertThat(deserializedException, is(notNullValue()));
            assertThat(deserializedException.getMessageKey(), is(nullValue()));
            assertThat(deserializedException.getArgs(), is(nullValue()));
        }

        @Test
        @DisplayName("Should preserve type after deserialization")
        void shouldPreserveTypeAfterDeserialization() throws Exception {
            // Arrange
            NotFoundException originalException = new NotFoundException(validMessageKey, validArgs);

            // Act
            byte[] serializedData = serialize(originalException);
            Object deserializedObject = deserializeAsObject(serializedData);

            // Assert
            assertThat(deserializedObject, is(instanceOf(NotFoundException.class)));
            assertThat(deserializedObject, is(instanceOf(BusinessException.class)));
            assertThat(deserializedObject, is(instanceOf(RuntimeException.class)));
        }

        private byte[] serialize(Object object) throws Exception {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(object);
                return baos.toByteArray();
            }
        }

        private NotFoundException deserialize(byte[] data) throws Exception {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (NotFoundException) ois.readObject();
            }
        }

        private Object deserializeAsObject(byte[] data) throws Exception {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return ois.readObject();
            }
        }
    }

    @Nested
    @DisplayName("Exception Scenarios Tests")
    class ExceptionScenariosTests {

        @Test
        @DisplayName("Should handle user not found scenario")
        void shouldHandleUserNotFoundScenario() {
            // Arrange
            String messageKey = "error.notfound.user.id";
            Object[] args = {UUID.randomUUID()};

            // Act
            NotFoundException exception = new NotFoundException(messageKey, args);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
            assertThat(exception.getArgs(), is(equalTo(args)));
        }

        @Test
        @DisplayName("Should handle department not found scenario")
        void shouldHandleDepartmentNotFoundScenario() {
            // Arrange
            String messageKey = "error.notfound.department.code";
            Object[] args = {"IT001"};

            // Act
            NotFoundException exception = new NotFoundException(messageKey, args);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
            assertThat(exception.getArgs(), is(equalTo(args)));
        }

        @Test
        @DisplayName("Should handle position not found scenario")
        void shouldHandlePositionNotFoundScenario() {
            // Arrange
            String messageKey = "error.notfound.position.title";
            Object[] args = {"Senior Developer", "IT"};

            // Act
            NotFoundException exception = new NotFoundException(messageKey, args);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
            assertThat(exception.getArgs(), is(equalTo(args)));
        }

        @Test
        @DisplayName("Should handle resource with criteria not found scenario")
        void shouldHandleResourceWithCriteriaNotFoundScenario() {
            // Arrange
            String messageKey = "error.notfound.resource.criteria";
            Object[] args = {"Employee", "department", "IT", "status", "ACTIVE"};

            // Act
            NotFoundException exception = new NotFoundException(messageKey, args);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
            assertThat(exception.getArgs(), is(equalTo(args)));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle rapid successive creations")
        void shouldHandleRapidSuccessiveCreations() {
            // Act & Assert
            for (int i = 0; i < 1000; i++) {
                NotFoundException exception = new NotFoundException("notfound" + i, "resource" + i);
                assertThat(exception.getMessageKey(), is(equalTo("notfound" + i)));
                assertThat(exception.getArgs()[0], is(equalTo("resource" + i)));
            }
        }

        @Test
        @DisplayName("Should handle very large args array")
        void shouldHandleVeryLargeArgsArray() {
            // Arrange
            Object[] largeArgs = new Object[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "notfound-arg" + i;
            }

            // Act
            NotFoundException exception = new NotFoundException(validMessageKey, largeArgs);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(1000)));
            assertThat(exception.getArgs(), is(equalTo(largeArgs)));
        }

        @Test
        @DisplayName("Should not throw exception on toString")
        void shouldNotThrowExceptionOnToString() {
            // Arrange
            NotFoundException exception = new NotFoundException(validMessageKey, validArgs);

            // Act & Assert
            assertDoesNotThrow(() -> {
                String result = exception.toString();
                assertThat(result, is(notNullValue()));
            });
        }

        @Test
        @DisplayName("Should handle toString with null values")
        void shouldHandleToStringWithNullValues() {
            // Arrange
            NotFoundException exception = new NotFoundException(null, (Object[]) null);

            // Act & Assert
            assertDoesNotThrow(() -> {
                String result = exception.toString();
                assertThat(result, is(notNullValue()));
            });
        }

        @Test
        @DisplayName("Should handle concurrent access")
        void shouldHandleConcurrentAccess() {
            // Arrange
            NotFoundException exception = new NotFoundException(validMessageKey, validArgs);

            // Act & Assert - Simulate concurrent access
            for (int i = 0; i < 100; i++) {
                String messageKey = exception.getMessageKey();
                Object[] args = exception.getArgs();
                
                assertThat(messageKey, is(equalTo(validMessageKey)));
                assertThat(args, is(equalTo(validArgs)));
            }
        }
    }

    @Nested
    @DisplayName("HTTP Status Semantic Tests")
    class HttpStatusSemanticTests {

        @Test
        @DisplayName("Should semantically represent 404 Not Found")
        void shouldSemanticallyRepresent404NotFound() {
            // This exception typically maps to HTTP 404
            // Testing the semantic meaning through exception creation
            
            String messageKey = "error.notfound.user.id";
            UUID userId = UUID.randomUUID();
            
            NotFoundException exception = new NotFoundException(messageKey, userId);
            
            assertThat(exception.getMessageKey(), containsString("notfound"));
            assertThat(exception.getArgs()[0], is(equalTo(userId)));
        }

        @Test
        @DisplayName("Should handle typical REST resource not found scenarios")
        void shouldHandleTypicalRestResourceNotFoundScenarios() {
            // Arrange & Act & Assert
            String[] resourceTypes = {"user", "department", "position", "employee", "team"};
            
            for (String resourceType : resourceTypes) {
                String messageKey = "error.notfound." + resourceType + ".id";
                UUID resourceId = UUID.randomUUID();
                
                NotFoundException exception = new NotFoundException(messageKey, resourceId);
                
                assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
                assertThat(exception.getArgs()[0], is(equalTo(resourceId)));
            }
        }
    }

    // Helper class for testing complex objects
    private static class SearchCriteria {
        private final String resourceType;
        private final String status;
        private final String department;

        public SearchCriteria(String resourceType, String status, String department) {
            this.resourceType = resourceType;
            this.status = status;
            this.department = department;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SearchCriteria that = (SearchCriteria) obj;
            return resourceType.equals(that.resourceType) && 
                   status.equals(that.status) && 
                   department.equals(that.department);
        }

        @Override
        public int hashCode() {
            return resourceType.hashCode() + status.hashCode() + department.hashCode();
        }

        @Override
        public String toString() {
            return resourceType + ":" + status + ":" + department;
        }
    }
}