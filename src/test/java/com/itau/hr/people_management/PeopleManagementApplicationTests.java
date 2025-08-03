package com.itau.hr.people_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@ExtendWith(MockitoExtension.class)
@DisplayName("PeopleManagementApplication Unit Tests")
class PeopleManagementApplicationTest {

    @Nested
    @DisplayName("Class Annotations Tests")
    class ClassAnnotationsTests {

        @Test
        @DisplayName("Should have SpringBootApplication annotation")
        void shouldHaveSpringBootApplicationAnnotation() {
            // Act & Assert
            assertThat(PeopleManagementApplication.class.isAnnotationPresent(SpringBootApplication.class), is(true));
        }

        @Test
        @DisplayName("Should have EnableScheduling annotation")
        void shouldHaveEnableSchedulingAnnotation() {
            // Act & Assert
            assertThat(PeopleManagementApplication.class.isAnnotationPresent(EnableScheduling.class), is(true));
        }

        @Test
        @DisplayName("Should be a public class")
        void shouldBeAPublicClass() {
            // Act & Assert
            assertThat(PeopleManagementApplication.class.getModifiers() & java.lang.reflect.Modifier.PUBLIC, is(not(0)));
        }
    }

    @Nested
    @DisplayName("Main Method Tests")
    class MainMethodTests {

        @Test
        @DisplayName("Should call SpringApplication.run with correct parameters")
        void shouldCallSpringApplicationRunWithCorrectParameters() {
            try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
                // Arrange
                String[] args = {"--spring.profiles.active=test"};
                ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
                
                springApplicationMock.when(() -> SpringApplication.run(PeopleManagementApplication.class, args))
                    .thenReturn(mockContext);

                // Act
                PeopleManagementApplication.main(args);

                // Assert
                springApplicationMock.verify(() -> SpringApplication.run(PeopleManagementApplication.class, args));
            }
        }

        @Test
        @DisplayName("Should call SpringApplication.run with empty args")
        void shouldCallSpringApplicationRunWithEmptyArgs() {
            try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
                // Arrange
                String[] emptyArgs = {};
                ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
                
                springApplicationMock.when(() -> SpringApplication.run(PeopleManagementApplication.class, emptyArgs))
                    .thenReturn(mockContext);

                // Act
                PeopleManagementApplication.main(emptyArgs);

                // Assert
                springApplicationMock.verify(() -> SpringApplication.run(PeopleManagementApplication.class, emptyArgs));
            }
        }

        @Test
        @DisplayName("Should call SpringApplication.run with null args")
        void shouldCallSpringApplicationRunWithNullArgs() {
            try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
                // Arrange
                String[] nullArgs = null;
                ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
                
                springApplicationMock.when(() -> SpringApplication.run(PeopleManagementApplication.class, nullArgs))
                    .thenReturn(mockContext);

                // Act
                PeopleManagementApplication.main(nullArgs);

                // Assert
                springApplicationMock.verify(() -> SpringApplication.run(PeopleManagementApplication.class, nullArgs));
            }
        }

        @Test
        @DisplayName("Should propagate SpringApplication.run exceptions")
        void shouldPropagateSpringApplicationRunExceptions() {
            try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
                // Arrange
                String[] args = {"--invalid-arg"};
                RuntimeException expectedException = new RuntimeException("Failed to start application");
                
                springApplicationMock.when(() -> SpringApplication.run(PeopleManagementApplication.class, args))
                    .thenThrow(expectedException);

                // Act & Assert
                RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                    PeopleManagementApplication.main(args);
                });

                assertThat(thrownException, is(sameInstance(expectedException)));
                springApplicationMock.verify(() -> SpringApplication.run(PeopleManagementApplication.class, args));
            }
        }

        @Test
        @DisplayName("Should pass application class correctly to SpringApplication.run")
        void shouldPassApplicationClassCorrectlyToSpringApplicationRun() {
            try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
                // Arrange
                String[] args = {};
                ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);

                springApplicationMock.when(() -> SpringApplication.run(PeopleManagementApplication.class, new String[0]))
                    .thenReturn(mockContext);

                // Act
                PeopleManagementApplication.main(args);

                // Assert
                springApplicationMock.verify(() -> SpringApplication.run(PeopleManagementApplication.class, new String[0]));
            }
        }

        @Test
        @DisplayName("Should handle multiple arguments correctly")
        void shouldHandleMultipleArgumentsCorrectly() {
            try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
                // Arrange
                String[] multipleArgs = {
                    "--spring.profiles.active=test",
                    "--server.port=8080",
                    "--logging.level.root=DEBUG"
                };
                ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
                
                springApplicationMock.when(() -> SpringApplication.run(PeopleManagementApplication.class, multipleArgs))
                    .thenReturn(mockContext);

                // Act
                PeopleManagementApplication.main(multipleArgs);

                // Assert
                springApplicationMock.verify(() -> SpringApplication.run(PeopleManagementApplication.class, multipleArgs));
            }
        }
    }

    @Nested
    @DisplayName("Method Signature Tests")
    class MethodSignatureTests {

        @Test
        @DisplayName("Main method should be public static void")
        void mainMethodShouldBePublicStaticVoid() throws NoSuchMethodException {
            // Act
            var mainMethod = PeopleManagementApplication.class.getMethod("main", String[].class);

            // Assert
            assertThat(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()), is(true));
            assertThat(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()), is(true));
            assertThat(mainMethod.getReturnType(), is(equalTo(void.class)));
        }

        @Test
        @DisplayName("Main method should accept String array parameter")
        void mainMethodShouldAcceptStringArrayParameter() throws NoSuchMethodException {
            // Act
            var mainMethod = PeopleManagementApplication.class.getMethod("main", String[].class);

            // Assert
            assertThat(mainMethod.getParameterTypes().length, is(equalTo(1)));
            assertThat(mainMethod.getParameterTypes()[0], is(equalTo(String[].class)));
        }

        @Test
        @DisplayName("Should have exactly one public method")
        void shouldHaveExactlyOnePublicMethod() {
            // Act
            var publicMethods = PeopleManagementApplication.class.getDeclaredMethods();
            long publicMethodCount = java.util.Arrays.stream(publicMethods)
                .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                .count();

            // Assert
            assertThat(publicMethodCount, is(equalTo(1L)));
        }
    }

    @Nested
    @DisplayName("Class Structure Tests")
    class ClassStructureTests {

        @Test
        @DisplayName("Should have default constructor")
        void shouldHaveDefaultConstructor() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                PeopleManagementApplication application = new PeopleManagementApplication();
                assertThat(application, is(notNullValue()));
            });
        }

        @Test
        @DisplayName("Should not have any fields")
        void shouldNotHaveAnyFields() {
            // Act
            var declaredFields = PeopleManagementApplication.class.getDeclaredFields();

            // Assert
            assertThat(declaredFields.length, is(equalTo(0)));
        }

        @Test
        @DisplayName("Should not extend any class")
        void shouldNotExtendAnyClass() {
            // Act
            Class<?> superclass = PeopleManagementApplication.class.getSuperclass();

            // Assert
            assertThat(superclass, is(equalTo(Object.class)));
        }

        @Test
        @DisplayName("Should not implement any interfaces")
        void shouldNotImplementAnyInterfaces() {
            // Act
            Class<?>[] interfaces = PeopleManagementApplication.class.getInterfaces();

            // Assert
            assertThat(interfaces.length, is(equalTo(0)));
        }

        @Test
        @DisplayName("Should be in correct package")
        void shouldBeInCorrectPackage() {
            // Act & Assert
            assertThat(PeopleManagementApplication.class.getPackage().getName(), 
                      is(equalTo("com.itau.hr.people_management")));
        }
    }

    @Nested
    @DisplayName("Spring Boot Configuration Tests")
    class SpringBootConfigurationTests {

        @Test
        @DisplayName("SpringBootApplication annotation should have default values")
        void springBootApplicationAnnotationShouldHaveDefaultValues() {
            // Act
            SpringBootApplication annotation = PeopleManagementApplication.class.getAnnotation(SpringBootApplication.class);

            // Assert
            assertThat(annotation, is(notNullValue()));
            assertThat(annotation.scanBasePackages().length, is(equalTo(0)));
            assertThat(annotation.exclude().length, is(equalTo(0)));
            assertThat(annotation.excludeName().length, is(equalTo(0)));
        }

        @Test
        @DisplayName("EnableScheduling annotation should be present without parameters")
        void enableSchedulingAnnotationShouldBePresentWithoutParameters() {
            // Act
            EnableScheduling annotation = PeopleManagementApplication.class.getAnnotation(EnableScheduling.class);

            // Assert
            assertThat(annotation, is(notNullValue()));
        }

        @Test
        @DisplayName("Should have exactly two annotations")
        void shouldHaveExactlyTwoAnnotations() {
            // Act
            var annotations = PeopleManagementApplication.class.getDeclaredAnnotations();

            // Assert
            assertThat(annotations.length, is(equalTo(2)));
        }
    }
}