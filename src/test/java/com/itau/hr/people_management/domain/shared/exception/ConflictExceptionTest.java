package com.itau.hr.people_management.domain.shared.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ConflictException Domain Exception Tests")
class ConflictExceptionTest {

    private String validMessageKey;
    private Object[] validArgs;

    @BeforeEach
    void setUp() {
        validMessageKey = "error.conflict.resource";
        validArgs = new Object[]{"user", "email", "john@example.com"};
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message key and args")
        void shouldCreateExceptionWithMessageKeyAndArgs() {
            // Act
            ConflictException exception = new ConflictException(validMessageKey, validArgs);

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
            ConflictException exception = new ConflictException(validMessageKey);

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
            ConflictException exception = new ConflictException(null, validArgs);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(nullValue()));
            assertThat(exception.getArgs(), is(equalTo(validArgs)));
        }

        @Test
        @DisplayName("Should create exception with null args")
        void shouldCreateExceptionWithNullArgs() {
            // Act
            ConflictException exception = new ConflictException(validMessageKey, (Object[]) null);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(nullValue()));
        }

        @Test
        @DisplayName("Should create exception with empty args")
        void shouldCreateExceptionWithEmptyArgs() {
            // Act
            ConflictException exception = new ConflictException(validMessageKey, new Object[0]);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(arrayWithSize(0)));
        }

        @Test
        @DisplayName("Should create exception with single arg")
        void shouldCreateExceptionWithSingleArg() {
            // Arrange
            String singleArg = "conflicting resource";

            // Act
            ConflictException exception = new ConflictException(validMessageKey, singleArg);

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
                "resource-name",
                42,
                true,
                123.45,
                null
            };

            // Act
            ConflictException exception = new ConflictException(validMessageKey, mixedArgs);

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
            ConflictException exception = new ConflictException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(BusinessException.class)));
        }

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            // Arrange
            ConflictException exception = new ConflictException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(RuntimeException.class)));
        }

        @Test
        @DisplayName("Should be instance of Exception")
        void shouldBeInstanceOfException() {
            // Arrange
            ConflictException exception = new ConflictException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(Exception.class)));
        }

        @Test
        @DisplayName("Should be instance of Throwable")
        void shouldBeInstanceOfThrowable() {
            // Arrange
            ConflictException exception = new ConflictException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(Throwable.class)));
        }

        @Test
        @DisplayName("Should inherit BusinessException behavior")
        void shouldInheritBusinessExceptionBehavior() {
            // Arrange
            ConflictException exception = new ConflictException(validMessageKey, validArgs);
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
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                throw new ConflictException(validMessageKey, validArgs);
            });

            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(equalTo(validArgs)));
        }

        @Test
        @DisplayName("Should be catchable as BusinessException")
        void shouldBeCatchableAsBusinessException() {
            // Act & Assert
            BusinessException caughtException = assertThrows(BusinessException.class, () -> {
                throw new ConflictException(validMessageKey, validArgs);
            });

            assertThat(caughtException, is(instanceOf(ConflictException.class)));
            assertThat(caughtException.getMessageKey(), is(equalTo(validMessageKey)));
        }

        @Test
        @DisplayName("Should be catchable as RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            // Act & Assert
            RuntimeException caughtException = assertThrows(RuntimeException.class, () -> {
                throw new ConflictException(validMessageKey, validArgs);
            });

            assertThat(caughtException, is(instanceOf(ConflictException.class)));
        }

        @Test
        @DisplayName("Should maintain stack trace when thrown")
        void shouldMaintainStackTraceWhenThrown() {
            // Act & Assert
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                throw new ConflictException(validMessageKey, validArgs);
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
            ConflictException exception = new ConflictException(messageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
        }

        @Test
        @DisplayName("Should handle conflict-specific message keys")
        void shouldHandleConflictSpecificMessageKeys() {
            // Arrange
            String[] conflictMessageKeys = {
                "error.conflict.duplicate.email",
                "error.conflict.duplicate.username",
                "error.conflict.resource.locked",
                "error.conflict.version.mismatch",
                "error.conflict.unique.constraint"
            };

            // Act & Assert
            for (String messageKey : conflictMessageKeys) {
                ConflictException exception = new ConflictException(messageKey, "test-resource");
                assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
            }
        }

        @Test
        @DisplayName("Should handle long message keys")
        void shouldHandleLongMessageKeys() {
            // Arrange
            String longMessageKey = "error.conflict." + "a".repeat(1000);

            // Act
            ConflictException exception = new ConflictException(longMessageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(longMessageKey)));
        }

        @Test
        @DisplayName("Should handle message keys with special characters")
        void shouldHandleMessageKeysWithSpecialCharacters() {
            // Arrange
            String specialMessageKey = "error.conflict.çñáéíóú@#$%^&*()";

            // Act
            ConflictException exception = new ConflictException(specialMessageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(specialMessageKey)));
        }
    }

    @Nested
    @DisplayName("Args Validation Tests")
    class ArgsValidationTests {

        @Test
        @DisplayName("Should handle conflict-related args")
        void shouldHandleConflictRelatedArgs() {
            // Arrange
            Object[] conflictArgs = {
                "User",
                "email",
                "john.doe@example.com",
                "already exists"
            };

            // Act
            ConflictException exception = new ConflictException(validMessageKey, conflictArgs);

            // Assert
            assertThat(exception.getArgs(), is(equalTo(conflictArgs)));
            assertThat(exception.getArgs(), is(arrayWithSize(4)));
        }

        @Test
        @DisplayName("Should handle resource identification args")
        void shouldHandleResourceIdentificationArgs() {
            // Arrange
            Object[] resourceArgs = {
                "Department",
                "IT",
                "costCenter",
                "IT001"
            };

            // Act
            ConflictException exception = new ConflictException("error.conflict.department", resourceArgs);

            // Assert
            assertThat(exception.getArgs(), is(equalTo(resourceArgs)));
        }

        @Test
        @DisplayName("Should handle args with null values")
        void shouldHandleArgsWithNullValues() {
            // Arrange
            Object[] argsWithNulls = {"resource", null, "field", null};

            // Act
            ConflictException exception = new ConflictException(validMessageKey, argsWithNulls);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(4)));
            assertThat(exception.getArgs()[1], is(nullValue()));
            assertThat(exception.getArgs()[3], is(nullValue()));
        }

        @Test
        @DisplayName("Should handle complex object args")
        void shouldHandleComplexObjectArgs() {
            // Arrange
            Object complexObject = new ConflictResource("User", "john@example.com");
            Object[] complexArgs = {complexObject, "duplicate email"};

            // Act
            ConflictException exception = new ConflictException(validMessageKey, complexArgs);

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
            ConflictException conflictException = new ConflictException(validMessageKey, validArgs);
            BusinessException businessException = conflictException;

            // Act & Assert
            assertThat(businessException, is(instanceOf(ConflictException.class)));
            assertThat(businessException.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(businessException.getArgs(), is(equalTo(validArgs)));
        }

        @Test
        @DisplayName("Should work as RuntimeException polymorphically")
        void shouldWorkAsRuntimeExceptionPolymorphically() {
            // Arrange
            ConflictException conflictException = new ConflictException(validMessageKey, validArgs);
            RuntimeException runtimeException = conflictException;

            // Act & Assert
            assertThat(runtimeException, is(instanceOf(ConflictException.class)));
            ConflictException castBack = (ConflictException) runtimeException;
            assertThat(castBack.getMessageKey(), is(equalTo(validMessageKey)));
        }

        @Test
        @DisplayName("Should work as Exception polymorphically")
        void shouldWorkAsExceptionPolymorphically() {
            // Arrange
            ConflictException conflictException = new ConflictException(validMessageKey, validArgs);
            Exception exception = conflictException;

            // Act & Assert
            assertThat(exception, is(instanceOf(ConflictException.class)));
            ConflictException castBack = (ConflictException) exception;
            assertThat(castBack.getMessageKey(), is(equalTo(validMessageKey)));
        }

        @Test
        @DisplayName("Should work as Throwable polymorphically")
        void shouldWorkAsThrowablePolymorphically() {
            // Arrange
            ConflictException conflictException = new ConflictException(validMessageKey, validArgs);
            Throwable throwable = conflictException;

            // Act & Assert
            assertThat(throwable, is(instanceOf(ConflictException.class)));
            ConflictException castBack = (ConflictException) throwable;
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
            ConflictException originalException = new ConflictException(validMessageKey, serializableArgs);

            // Act
            byte[] serializedData = serialize(originalException);
            ConflictException deserializedException = deserialize(serializedData);

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
            ConflictException originalException = new ConflictException(null, (Object[]) null);

            // Act
            byte[] serializedData = serialize(originalException);
            ConflictException deserializedException = deserialize(serializedData);

            // Assert
            assertThat(deserializedException, is(notNullValue()));
            assertThat(deserializedException.getMessageKey(), is(nullValue()));
            assertThat(deserializedException.getArgs(), is(nullValue()));
        }

        @Test
        @DisplayName("Should preserve type after deserialization")
        void shouldPreserveTypeAfterDeserialization() throws Exception {
            // Arrange
            ConflictException originalException = new ConflictException(validMessageKey, validArgs);

            // Act
            byte[] serializedData = serialize(originalException);
            Object deserializedObject = deserializeAsObject(serializedData);

            // Assert
            assertThat(deserializedObject, is(instanceOf(ConflictException.class)));
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

        private ConflictException deserialize(byte[] data) throws Exception {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (ConflictException) ois.readObject();
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
        @DisplayName("Should handle duplicate resource conflict")
        void shouldHandleDuplicateResourceConflict() {
            // Arrange
            String messageKey = "error.conflict.duplicate.user";
            Object[] args = {"email", "john.doe@example.com"};

            // Act
            ConflictException exception = new ConflictException(messageKey, args);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
            assertThat(exception.getArgs(), is(equalTo(args)));
        }

        @Test
        @DisplayName("Should handle version mismatch conflict")
        void shouldHandleVersionMismatchConflict() {
            // Arrange
            String messageKey = "error.conflict.version.mismatch";
            Object[] args = {"User", 1, 2};

            // Act
            ConflictException exception = new ConflictException(messageKey, args);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
            assertThat(exception.getArgs(), is(equalTo(args)));
        }

        @Test
        @DisplayName("Should handle concurrent modification conflict")
        void shouldHandleConcurrentModificationConflict() {
            // Arrange
            String messageKey = "error.conflict.concurrent.modification";
            Object[] args = {"Department", "IT", "user123"};

            // Act
            ConflictException exception = new ConflictException(messageKey, args);

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
                ConflictException exception = new ConflictException("conflict" + i, "resource" + i);
                assertThat(exception.getMessageKey(), is(equalTo("conflict" + i)));
                assertThat(exception.getArgs()[0], is(equalTo("resource" + i)));
            }
        }

        @Test
        @DisplayName("Should handle very large args array")
        void shouldHandleVeryLargeArgsArray() {
            // Arrange
            Object[] largeArgs = new Object[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "conflict-arg" + i;
            }

            // Act
            ConflictException exception = new ConflictException(validMessageKey, largeArgs);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(1000)));
            assertThat(exception.getArgs(), is(equalTo(largeArgs)));
        }

        @Test
        @DisplayName("Should not throw exception on toString")
        void shouldNotThrowExceptionOnToString() {
            // Arrange
            ConflictException exception = new ConflictException(validMessageKey, validArgs);

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
            ConflictException exception = new ConflictException(null, (Object[]) null);

            // Act & Assert
            assertDoesNotThrow(() -> {
                String result = exception.toString();
                assertThat(result, is(notNullValue()));
            });
        }
    }

    // Helper class for testing complex objects
    private static class ConflictResource {
        private final String type;
        private final String identifier;

        public ConflictResource(String type, String identifier) {
            this.type = type;
            this.identifier = identifier;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ConflictResource that = (ConflictResource) obj;
            return type.equals(that.type) && identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            return type.hashCode() + identifier.hashCode();
        }

        @Override
        public String toString() {
            return type + ":" + identifier;
        }
    }
}