package com.itau.hr.people_management.unit.infrastructure.shared.message;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.domain.shared.vo.Email;
import com.itau.hr.people_management.infrastructure.shared.message.DomainMessageSourceInitializer;
import com.itau.hr.people_management.interfaces.shared.exception_handler.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
@DisplayName("DomainMessageSourceInitializer Unit Tests")
class DomainMessageSourceInitializerTest {

    @Mock
    private DomainMessageSource domainMessageSource;

    private DomainMessageSourceInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new DomainMessageSourceInitializer(domainMessageSource);
    }

    @Test
    @DisplayName("Should set message source on all domain classes")
    void shouldSetMessageSourceOnAllDomainClasses() {
        try (MockedStatic<Department> deptMock = mockStatic(Department.class);
             MockedStatic<Position> posMock = mockStatic(Position.class);
             MockedStatic<Employee> empMock = mockStatic(Employee.class);
             MockedStatic<Email> emailMock = mockStatic(Email.class);
             MockedStatic<GlobalExceptionHandler> handlerMock = mockStatic(GlobalExceptionHandler.class)) {

            // Act
            initializer.init();

            // Assert
            deptMock.verify(() -> Department.setMessageSource(domainMessageSource));
            posMock.verify(() -> Position.setMessageSource(domainMessageSource));
            empMock.verify(() -> Employee.setMessageSource(domainMessageSource));
            emailMock.verify(() -> Email.setMessageSource(domainMessageSource));
            handlerMock.verify(() -> GlobalExceptionHandler.setMessageSource(domainMessageSource));
        }
    }
}