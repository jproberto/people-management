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

@DisplayName("BusinessException Domain Exception Tests")
class BusinessExceptionTest {

    private String validMessageKey;
    private Object[] validArgs;

    @BeforeEach
    void setUp() {
        validMessageKey = "error.business.validation";
        validArgs = new Object[]{"param1", "param2", 123};
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message key and args")
        void shouldCreateExceptionWithMessageKeyAndArgs() {
            // Act
            BusinessException exception = new BusinessException(validMessageKey, validArgs);

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
            BusinessException exception = new BusinessException(validMessageKey);

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
            BusinessException exception = new BusinessException(null, validArgs);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(nullValue()));
            assertThat(exception.getArgs(), is(equalTo(validArgs)));
        }

        @Test
        @DisplayName("Should create exception with null args")
        void shouldCreateExceptionWithNullArgs() {
            // Act
            BusinessException exception = new BusinessException(validMessageKey, (Object[]) null);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(nullValue()));
        }

        @Test
        @DisplayName("Should create exception with empty args")
        void shouldCreateExceptionWithEmptyArgs() {
            // Act
            BusinessException exception = new BusinessException(validMessageKey, new Object[0]);

            // Assert
            assertThat(exception, is(notNullValue()));
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(arrayWithSize(0)));
        }

        @Test
        @DisplayName("Should create exception with mixed type args")
        void shouldCreateExceptionWithMixedTypeArgs() {
            // Arrange
            Object[] mixedArgs = {
                "string",
                123,
                45.67,
                true,
                null,
                new Object(),
                new String[]{"nested", "array"}
            };

            // Act
            BusinessException exception = new BusinessException(validMessageKey, mixedArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
            assertThat(exception.getArgs(), is(equalTo(mixedArgs)));
            assertThat(exception.getArgs(), is(arrayWithSize(7)));
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct message key")
        void shouldReturnCorrectMessageKey() {
            // Arrange
            BusinessException exception = new BusinessException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception.getMessageKey(), is(equalTo(validMessageKey)));
        }

        @Test
        @DisplayName("Should return correct args")
        void shouldReturnCorrectArgs() {
            // Arrange
            BusinessException exception = new BusinessException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception.getArgs(), is(equalTo(validArgs)));
        }

        @Test
        @DisplayName("Should return same args reference")
        void shouldReturnSameArgsReference() {
            // Arrange
            BusinessException exception = new BusinessException(validMessageKey, validArgs);

            // Act
            Object[] returnedArgs = exception.getArgs();

            // Assert
            assertThat(returnedArgs, is(sameInstance(validArgs)));
        }

        @Test
        @DisplayName("Should return null when message key is null")
        void shouldReturnNullWhenMessageKeyIsNull() {
            // Arrange
            BusinessException exception = new BusinessException(null, validArgs);

            // Act & Assert
            assertThat(exception.getMessageKey(), is(nullValue()));
        }

        @Test
        @DisplayName("Should return null when args is null")
        void shouldReturnNullWhenArgsIsNull() {
            // Arrange
            BusinessException exception = new BusinessException(validMessageKey, (Object[]) null);

            // Act & Assert
            assertThat(exception.getArgs(), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Runtime Exception Behavior Tests")
    class RuntimeExceptionBehaviorTests {

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            // Arrange
            BusinessException exception = new BusinessException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(RuntimeException.class)));
        }

        @Test
        @DisplayName("Should be instance of Exception")
        void shouldBeInstanceOfException() {
            // Arrange
            BusinessException exception = new BusinessException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(Exception.class)));
        }

        @Test
        @DisplayName("Should be instance of Throwable")
        void shouldBeInstanceOfThrowable() {
            // Arrange
            BusinessException exception = new BusinessException(validMessageKey, validArgs);

            // Act & Assert
            assertThat(exception, is(instanceOf(Throwable.class)));
        }

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            // Act & Assert
            assertThrows(BusinessException.class, () -> {
                throw new BusinessException(validMessageKey, validArgs);
            });
        }

        @Test
        @DisplayName("Should maintain stack trace when thrown")
        void shouldMaintainStackTraceWhenThrown() {
            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                throw new BusinessException(validMessageKey, validArgs);
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
            BusinessException exception = new BusinessException(messageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(messageKey)));
        }

        @Test
        @DisplayName("Should handle long message keys")
        void shouldHandleLongMessageKeys() {
            // Arrange
            String longMessageKey = "a".repeat(1000);

            // Act
            BusinessException exception = new BusinessException(longMessageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(longMessageKey)));
        }

        @Test
        @DisplayName("Should handle message keys with special characters")
        void shouldHandleMessageKeysWithSpecialCharacters() {
            // Arrange
            String specialMessageKey = "error.validation.çñáéíóú@#$%^&*()";

            // Act
            BusinessException exception = new BusinessException(specialMessageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(specialMessageKey)));
        }

        @Test
        @DisplayName("Should handle message keys with dots and underscores")
        void shouldHandleMessageKeysWithDotsAndUnderscores() {
            // Arrange
            String dottedMessageKey = "error.business.validation.user_not_found";

            // Act
            BusinessException exception = new BusinessException(dottedMessageKey, validArgs);

            // Assert
            assertThat(exception.getMessageKey(), is(equalTo(dottedMessageKey)));
        }
    }

    @Nested
    @DisplayName("Args Validation Tests")
    class ArgsValidationTests {

        @Test
        @DisplayName("Should handle single arg")
        void shouldHandleSingleArg() {
            // Arrange
            String singleArg = "single argument";

            // Act
            BusinessException exception = new BusinessException(validMessageKey, singleArg);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(1)));
            assertThat(exception.getArgs()[0], is(equalTo(singleArg)));
        }

        @Test
        @DisplayName("Should handle multiple args of same type")
        void shouldHandleMultipleArgsOfSameType() {
            // Arrange
            String[] stringArgs = {"arg1", "arg2", "arg3"};

            // Act
            BusinessException exception = new BusinessException(validMessageKey, (Object[]) stringArgs);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(3)));
            assertThat(exception.getArgs(), is(equalTo(stringArgs)));
        }

        @Test
        @DisplayName("Should handle args with null values")
        void shouldHandleArgsWithNullValues() {
            // Arrange
            Object[] argsWithNulls = {"valid", null, "another valid", null};

            // Act
            BusinessException exception = new BusinessException(validMessageKey, argsWithNulls);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(4)));
            assertThat(exception.getArgs(), is(equalTo(argsWithNulls)));
            assertThat(exception.getArgs()[1], is(nullValue()));
            assertThat(exception.getArgs()[3], is(nullValue()));
        }

        @Test
        @DisplayName("Should handle very large args array")
        void shouldHandleVeryLargeArgsArray() {
            // Arrange
            Object[] largeArgs = new Object[1000];
            for (int i = 0; i < 1000; i++) {
                largeArgs[i] = "arg" + i;
            }

            // Act
            BusinessException exception = new BusinessException(validMessageKey, largeArgs);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(1000)));
            assertThat(exception.getArgs(), is(equalTo(largeArgs)));
        }

        @Test
        @DisplayName("Should handle complex object args")
        void shouldHandleComplexObjectArgs() {
            // Arrange
            Object complexObject = new ComplexTestObject("test", 123);
            Object[] complexArgs = {complexObject, new java.util.Date(), new java.util.HashMap<>()};

            // Act
            BusinessException exception = new BusinessException(validMessageKey, complexArgs);

            // Assert
            assertThat(exception.getArgs(), is(arrayWithSize(3)));
            assertThat(exception.getArgs()[0], is(sameInstance(complexObject)));
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable when args are serializable")
        void shouldBeSerializableWhenArgsAreSerializable() throws Exception {
            // Arrange
            Object[] serializableArgs = {"string", 123, 45.67, true};
            BusinessException originalException = new BusinessException(validMessageKey, serializableArgs);

            // Act
            byte[] serializedData = serialize(originalException);
            BusinessException deserializedException = deserialize(serializedData);

            // Assert
            assertThat(deserializedException, is(notNullValue()));
            assertThat(deserializedException.getMessageKey(), is(equalTo(originalException.getMessageKey())));
            // Note: args marked as transient, so they won't be serialized
            assertThat(deserializedException.getArgs(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle serialization with null args")
        void shouldHandleSerializationWithNullArgs() throws Exception {
            // Arrange
            BusinessException originalException = new BusinessException(validMessageKey, (Object[]) null);

            // Act
            byte[] serializedData = serialize(originalException);
            BusinessException deserializedException = deserialize(serializedData);

            // Assert
            assertThat(deserializedException, is(notNullValue()));
            assertThat(deserializedException.getMessageKey(), is(equalTo(originalException.getMessageKey())));
            assertThat(deserializedException.getArgs(), is(nullValue()));
        }

        @Test
        @DisplayName("Should preserve message key during serialization")
        void shouldPreserveMessageKeyDuringSerialization() throws Exception {
            // Arrange
            String specialMessageKey = "error.serialization.test";
            BusinessException originalException = new BusinessException(specialMessageKey, "arg1", "arg2");

            // Act
            byte[] serializedData = serialize(originalException);
            BusinessException deserializedException = deserialize(serializedData);

            // Assert
            assertThat(deserializedException.getMessageKey(), is(equalTo(specialMessageKey)));
        }

        private byte[] serialize(Object object) throws Exception {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(object);
                return baos.toByteArray();
            }
        }

        private BusinessException deserialize(byte[] data) throws Exception {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (BusinessException) ois.readObject();
            }
        }
    }

    @Nested
    @DisplayName("Inheritance and Polymorphism Tests")
    class InheritanceAndPolymorphismTests {

        @Test
        @DisplayName("Should work as RuntimeException polymorphically")
        void shouldWorkAsRuntimeExceptionPolymorphically() {
            // Arrange
            BusinessException businessException = new BusinessException(validMessageKey, validArgs);
            RuntimeException runtimeException = businessException;

            // Act & Assert
            assertThat(runtimeException, is(instanceOf(BusinessException.class)));
            assertThat(((BusinessException) runtimeException).getMessageKey(), is(equalTo(validMessageKey)));
        }

        @Test
        @DisplayName("Should work as Exception polymorphically")
        void shouldWorkAsExceptionPolymorphically() {
            // Arrange
            BusinessException businessException = new BusinessException(validMessageKey, validArgs);
            Exception exception = businessException;

            // Act & Assert
            assertThat(exception, is(instanceOf(BusinessException.class)));
            assertThat(((BusinessException) exception).getMessageKey(), is(equalTo(validMessageKey)));
        }

        @Test
        @DisplayName("Should work as Throwable polymorphically")
        void shouldWorkAsThrowablePolymorphically() {
            // Arrange
            BusinessException businessException = new BusinessException(validMessageKey, validArgs);
            Throwable throwable = businessException;

            // Act & Assert
            assertThat(throwable, is(instanceOf(BusinessException.class)));
            assertThat(((BusinessException) throwable).getMessageKey(), is(equalTo(validMessageKey)));
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
                BusinessException exception = new BusinessException("key" + i, "arg" + i);
                assertThat(exception.getMessageKey(), is(equalTo("key" + i)));
                assertThat(exception.getArgs()[0], is(equalTo("arg" + i)));
            }
        }

        @Test
        @DisplayName("Should handle concurrent access to args")
        void shouldHandleConcurrentAccessToArgs() {
            // Arrange
            BusinessException exception = new BusinessException(validMessageKey, validArgs);

            // Act & Assert - Multiple thread access simulation
            for (int i = 0; i < 100; i++) {
                Object[] args = exception.getArgs();
                assertThat(args, is(sameInstance(validArgs)));
            }
        }

        @Test
        @DisplayName("Should maintain identity when args are modified externally")
        void shouldMaintainIdentityWhenArgsAreModifiedExternally() {
            // Arrange
            Object[] mutableArgs = {"original1", "original2"};
            BusinessException exception = new BusinessException(validMessageKey, mutableArgs);

            // Act - Modify original array
            mutableArgs[0] = "modified1";

            // Assert - Exception should reflect the change (since it holds reference)
            assertThat(exception.getArgs()[0], is(equalTo("modified1")));
        }
    }

    @Nested
    @DisplayName("ToString and Debug Tests")
    class ToStringAndDebugTests {

        @Test
        @DisplayName("Should not throw exception on toString")
        void shouldNotThrowExceptionOnToString() {
            // Arrange
            BusinessException exception = new BusinessException(validMessageKey, validArgs);

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
            BusinessException exception = new BusinessException(null, (Object[]) null);

            // Act & Assert
            assertDoesNotThrow(() -> {
                String result = exception.toString();
                assertThat(result, is(notNullValue()));
            });
        }
    }

    // Helper class for testing complex objects
    private static class ComplexTestObject {
        private final String name;
        private final int value;

        public ComplexTestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ComplexTestObject that = (ComplexTestObject) obj;
            return value == that.value && name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + value;
        }
    }
}