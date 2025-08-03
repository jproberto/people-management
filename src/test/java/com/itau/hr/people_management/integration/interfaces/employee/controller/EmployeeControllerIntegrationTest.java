package com.itau.hr.people_management.integration.interfaces.employee.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.itau.hr.people_management.application.employee.dto.ChangeEmployeeStatusRequest;
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;
import com.itau.hr.people_management.domain.shared.vo.Email;
import com.itau.hr.people_management.infrastructure.persistence.entity.EmployeeJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.repository.JpaDepartmentRepository;
import com.itau.hr.people_management.infrastructure.persistence.repository.JpaEmployeeRepository;
import com.itau.hr.people_management.infrastructure.persistence.repository.JpaPositionRepository;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeRequestDTO;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeResponseDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("EmployeeController Integration Tests")
class EmployeeControllerIntegrationTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("people_management_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/migration/V1__create_initial_tables.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private JpaEmployeeRepository employeeJpaRepository;

    @Autowired
    private JpaDepartmentRepository departmentJpaRepository;

    @Autowired
    private JpaPositionRepository positionJpaRepository;

    private Department testDepartment;
    private Position testPosition;

    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        employeeJpaRepository.deleteAll();
        departmentJpaRepository.deleteAll();
        positionJpaRepository.deleteAll();
        
        employeeJpaRepository.flush();
        departmentJpaRepository.flush();
        positionJpaRepository.flush();

        // Criar department e position para os testes
        testDepartment = Department.create(UUID.randomUUID(), "Information Technology", "IT001");
        testPosition = Position.create(UUID.randomUUID(), "Software Engineer", PositionLevel.SENIOR);

        departmentRepository.save(testDepartment);
        positionRepository.save(testPosition);
    }

    @Test
    @DisplayName("Should create employee successfully via POST /api/v1/employees")
    void shouldCreateEmployeeSuccessfully() throws Exception {
        // Arrange
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setEmail("john.doe@itau.com.br");
        requestDTO.setDepartmentId(testDepartment.getId());
        requestDTO.setPositionId(testPosition.getId());

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@itau.com.br"))
                .andExpect(jsonPath("$.employeeStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.department.id").value(testDepartment.getId().toString()))
                .andExpect(jsonPath("$.position.id").value(testPosition.getId().toString()))
                .andReturn();

        // Verificar resposta JSON
        String responseJson = result.getResponse().getContentAsString();
        EmployeeResponseDTO responseDTO = objectMapper.readValue(responseJson, EmployeeResponseDTO.class);
        
        assertThat(responseDTO.getId(), is(notNullValue()));
        assertThat(responseDTO.getName(), is("John Doe"));
        assertThat(responseDTO.getEmail(), is("john.doe@itau.com.br"));
        assertThat(responseDTO.getEmployeeStatus(), is(EmployeeStatus.ACTIVE));

        // Verificar persistência no database
        List<EmployeeJpaEntity> savedEntities = employeeJpaRepository.findAll();
        assertThat(savedEntities, hasSize(1));
        
        EmployeeJpaEntity savedEntity = savedEntities.get(0);
        assertThat(savedEntity.getId(), is(responseDTO.getId()));
        assertThat(savedEntity.getName(), is("John Doe"));
        assertThat(savedEntity.getEmail(), is("john.doe@itau.com.br"));
        assertThat(savedEntity.getStatus(), is(EmployeeStatus.ACTIVE));
        assertThat(savedEntity.getDepartment().getId(), is(testDepartment.getId()));
        assertThat(savedEntity.getPosition().getId(), is(testPosition.getId()));
    }

    @Test
    @DisplayName("Should return 400 when creating employee with invalid data")
    void shouldReturn400WhenCreatingEmployeeWithInvalidData() throws Exception {
        // Arrange - DTO com dados inválidos
        EmployeeRequestDTO invalidRequestDTO = new EmployeeRequestDTO();
        invalidRequestDTO.setName(""); // Nome vazio - inválido
        invalidRequestDTO.setEmail("invalid-email"); // Email inválido
        invalidRequestDTO.setDepartmentId(testDepartment.getId());
        invalidRequestDTO.setPositionId(testPosition.getId());

        String requestJson = objectMapper.writeValueAsString(invalidRequestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verificar que nada foi salvo no database
        List<EmployeeJpaEntity> savedEntities = employeeJpaRepository.findAll();
        assertThat(savedEntities, empty());
    }

    @Test
    @DisplayName("Should return 404 when creating employee with non-existent department")
    void shouldReturn404WhenCreatingEmployeeWithNonExistentDepartment() throws Exception {
        // Arrange
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setEmail("john.doe@itau.com.br");
        requestDTO.setDepartmentId(UUID.randomUUID()); // Department inexistente
        requestDTO.setPositionId(testPosition.getId());

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verificar que nada foi salvo no database
        List<EmployeeJpaEntity> savedEntities = employeeJpaRepository.findAll();
        assertThat(savedEntities, empty());
    }

    @Test
    @DisplayName("Should return 404 when creating employee with non-existent position")
    void shouldReturn404WhenCreatingEmployeeWithNonExistentPosition() throws Exception {
        // Arrange
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setEmail("john.doe@itau.com.br");
        requestDTO.setDepartmentId(testDepartment.getId());
        requestDTO.setPositionId(UUID.randomUUID()); // Position inexistente

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verificar que nada foi salvo no database
        List<EmployeeJpaEntity> savedEntities = employeeJpaRepository.findAll();
        assertThat(savedEntities, empty());
    }

    @Test
    @DisplayName("Should return 409 when creating employee with duplicate email")
    void shouldReturn409WhenCreatingEmployeeWithDuplicateEmail() throws Exception {
        // Arrange - Criar employee existente
        Employee existingEmployee = Employee.create(
            UUID.randomUUID(),
            "Existing User", 
            Email.create("john.doe@itau.com.br"), 
            EmployeeStatus.ACTIVE,
            testDepartment, 
            testPosition
        );
        employeeRepository.save(existingEmployee);

        // Tentar criar novo employee com mesmo email
        EmployeeRequestDTO duplicateRequestDTO = new EmployeeRequestDTO();
        duplicateRequestDTO.setName("John Smith");
        duplicateRequestDTO.setEmail("john.doe@itau.com.br"); // Email duplicado
        duplicateRequestDTO.setDepartmentId(testDepartment.getId());
        duplicateRequestDTO.setPositionId(testPosition.getId());

        String requestJson = objectMapper.writeValueAsString(duplicateRequestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verificar que apenas o employee original existe
        List<EmployeeJpaEntity> savedEntities = employeeJpaRepository.findAll();
        assertThat(savedEntities, hasSize(1));
        assertThat(savedEntities.get(0).getName(), is("Existing User"));
    }

    @Test
    @DisplayName("Should get all employees successfully via GET /api/v1/employees")
    void shouldGetAllEmployeesSuccessfully() throws Exception {
        // Arrange - Criar employees no database
        Employee emp1 = Employee.create(UUID.randomUUID(), "John Doe", Email.create("john@itau.com.br"), EmployeeStatus.ACTIVE, testDepartment, testPosition);
        Employee emp2 = Employee.create(UUID.randomUUID(), "Jane Smith", Email.create("jane@itau.com.br"), EmployeeStatus.ACTIVE, testDepartment, testPosition);
        Employee emp3 = Employee.create(UUID.randomUUID(), "Bob Johnson", Email.create("bob@itau.com.br"), EmployeeStatus.ACTIVE, testDepartment, testPosition);

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$[*].name").exists())
                .andExpect(jsonPath("$[*].email").exists())
                .andExpect(jsonPath("$[*].employeeStatus").exists())
                .andReturn();

        // Verificar resposta detalhada
        String responseJson = result.getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, EmployeeResponseDTO.class);
        List<EmployeeResponseDTO> responseDTOs = objectMapper.readValue(responseJson, listType);

        assertThat(responseDTOs, hasSize(3));

        // Verificar que todos os employees estão presentes
        List<String> names = responseDTOs.stream()
            .map(EmployeeResponseDTO::getName)
            .toList();
        assertThat(names, containsInAnyOrder("John Doe", "Jane Smith", "Bob Johnson"));

        List<String> emails = responseDTOs.stream()
            .map(EmployeeResponseDTO::getEmail)
            .toList();
        assertThat(emails, containsInAnyOrder(
            "john@itau.com.br", 
            "jane@itau.com.br", 
            "bob@itau.com.br"
        ));

        // Verificar que todos têm dados válidos
        responseDTOs.forEach(dto -> {
            assertThat(dto.getId(), is(notNullValue()));
            assertThat(dto.getName(), is(not(emptyString())));
            assertThat(dto.getEmail(), is(not(emptyString())));
            assertThat(dto.getEmployeeStatus(), is(EmployeeStatus.ACTIVE));
            assertThat(dto.getDepartment(), is(notNullValue()));
            assertThat(dto.getPosition(), is(notNullValue()));
        });
    }

    @Test
    @DisplayName("Should get employee by ID successfully via GET /api/v1/employees/{id}")
    void shouldGetEmployeeByIdSuccessfully() throws Exception {
        // Arrange - Criar employee no database
        Employee employee = Employee.create(
            UUID.randomUUID(),
            "John Doe",
            Email.create("john.doe@itau.com.br"),
            EmployeeStatus.ACTIVE,
            testDepartment,
            testPosition
        );
        employeeRepository.save(employee);

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/v1/employees/{id}", employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employee.getId().toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@itau.com.br"))
                .andExpect(jsonPath("$.employeeStatus").value("ACTIVE"))
                .andReturn();

        // Verificar resposta
        String responseJson = result.getResponse().getContentAsString();
        EmployeeResponseDTO responseDTO = objectMapper.readValue(responseJson, EmployeeResponseDTO.class);
        
        assertThat(responseDTO.getId(), is(employee.getId()));
        assertThat(responseDTO.getName(), is("John Doe"));
        assertThat(responseDTO.getEmail(), is("john.doe@itau.com.br"));
        assertThat(responseDTO.getEmployeeStatus(), is(EmployeeStatus.ACTIVE));
        assertThat(responseDTO.getDepartment().getId(), is(testDepartment.getId()));
        assertThat(responseDTO.getPosition().getId(), is(testPosition.getId()));
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent employee by ID")
    void shouldReturn404WhenGettingNonExistentEmployeeById() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should delete employee successfully via DELETE /api/v1/employees/{id}")
    void shouldDeleteEmployeeSuccessfully() throws Exception {
        // Arrange - Criar employee no database
        Employee employee = Employee.create(
            UUID.randomUUID(),
            "John Doe",
            Email.create("john.doe@itau.com.br"),
            EmployeeStatus.ACTIVE,
            testDepartment,
            testPosition
        );
        employeeRepository.save(employee);

        // Verificar que employee existe
        List<EmployeeJpaEntity> beforeDelete = employeeJpaRepository.findAll();
        assertThat(beforeDelete, hasSize(1));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que employee foi deletado
        List<EmployeeJpaEntity> afterDelete = employeeJpaRepository.findAll();
        assertThat(afterDelete, empty());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent employee")
    void shouldReturn404WhenDeletingNonExistentEmployee() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should search employees by criteria successfully via GET /api/v1/employees/search")
    void shouldSearchEmployeesByCriteriaSuccessfully() throws Exception {
        // Arrange - Criar employees com dados diferentes
        Employee emp1 = Employee.create(UUID.randomUUID(), "John Developer", Email.create("john@itau.com.br"), EmployeeStatus.ACTIVE, testDepartment, testPosition);
        Employee emp2 = Employee.create(UUID.randomUUID(), "Jane Manager", Email.create("jane@itau.com.br"), EmployeeStatus.ACTIVE, testDepartment, testPosition);
        Employee emp3 = Employee.create(UUID.randomUUID(), "Bob Analyst", Email.create("bob@itau.com.br"), EmployeeStatus.ACTIVE, testDepartment, testPosition);

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);

        // Act & Assert - Buscar por nome
        mockMvc.perform(get("/api/v1/employees/search")
                .param("name", "John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("John Developer"));

        // Act & Assert - Buscar por department
        mockMvc.perform(get("/api/v1/employees/search")
                .param("department", "Information Technology")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // Todos pertencem ao mesmo department

        // Act & Assert - Buscar por status
        mockMvc.perform(get("/api/v1/employees/search")
                .param("status", "ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // Todos são ACTIVE
    }

    @Test
    @DisplayName("Should change employee status successfully via PATCH /api/v1/employees/{id}/status")
    void shouldChangeEmployeeStatusSuccessfully() throws Exception {
        // Arrange - Criar employee ACTIVE
        Employee employee = Employee.create(
            UUID.randomUUID(),
            "John Doe", 
            Email.create("john.doe@itau.com.br"), 
            EmployeeStatus.ACTIVE,
            testDepartment, 
            testPosition
        );
        employeeRepository.save(employee);

        // Verificar status inicial
        assertThat(employee.getStatus(), is(EmployeeStatus.ACTIVE));

        ChangeEmployeeStatusRequest statusRequest = new ChangeEmployeeStatusRequest(EmployeeStatus.TERMINATED);
        String requestJson = objectMapper.writeValueAsString(statusRequest);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/employees/{id}/status", employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNoContent());

        // Verificar que status foi alterado no database
        EmployeeJpaEntity updatedEntity = employeeJpaRepository.findById(employee.getId()).orElseThrow();
        assertThat(updatedEntity.getStatus(), is(EmployeeStatus.TERMINATED));
    }

    @Test
    @DisplayName("Should return 404 when changing status of non-existent employee")
    void shouldReturn404WhenChangingStatusOfNonExistentEmployee() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        ChangeEmployeeStatusRequest statusRequest = new ChangeEmployeeStatusRequest(EmployeeStatus.TERMINATED);
        String requestJson = objectMapper.writeValueAsString(statusRequest);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/employees/{id}/status", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should reactivate employee successfully via POST /api/v1/employees/{id}/reactivate")
    void shouldReactivateEmployeeSuccessfully() throws Exception {
        // Arrange - Criar employee TERMINATED
        Employee employee = Employee.create(
            UUID.randomUUID(),
            "John Doe", 
            Email.create("john.doe@itau.com.br"), 
            EmployeeStatus.TERMINATED,
            testDepartment, 
            testPosition
        );
        employeeRepository.save(employee);

        // Verificar status inicial
        EmployeeJpaEntity beforeReactivation = employeeJpaRepository.findById(employee.getId()).orElseThrow();
        assertThat(beforeReactivation.getStatus(), is(EmployeeStatus.TERMINATED));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees/{id}/reactivate", employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que employee foi reativado no database
        EmployeeJpaEntity afterReactivation = employeeJpaRepository.findById(employee.getId()).orElseThrow();
        assertThat(afterReactivation.getStatus(), is(EmployeeStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should return 404 when reactivating non-existent employee")
    void shouldReturn404WhenReactivatingNonExistentEmployee() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees/{id}/reactivate", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should return empty list when no employees exist")
    void shouldReturnEmptyListWhenNoEmployeesExist() throws Exception {
        // Arrange - Database vazio (já limpo no @BeforeEach)

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        // Verificar resposta
        String responseJson = result.getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, EmployeeResponseDTO.class);
        List<EmployeeResponseDTO> responseDTOs = objectMapper.readValue(responseJson, listType);

        assertThat(responseDTOs, empty());
    }

    @Test
    @DisplayName("Should verify complete integration chain: HTTP → Controller → Use Case → Repository → Database")
    void shouldVerifyCompleteIntegrationChainHttpToControllerToUseCaseToRepositoryToDatabase() throws Exception {
        // Arrange
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO();
        requestDTO.setName("Integration Test Employee");
        requestDTO.setEmail("integration@itau.com.br");
        requestDTO.setDepartmentId(testDepartment.getId());
        requestDTO.setPositionId(testPosition.getId());

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act - POST para criar
        MvcResult createResult = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Verificar resposta da criação
        String createResponseJson = createResult.getResponse().getContentAsString();
        EmployeeResponseDTO createdDTO = objectMapper.readValue(createResponseJson, EmployeeResponseDTO.class);

        // Act - GET para buscar por ID
        MvcResult getResult = mockMvc.perform(get("/api/v1/employees/{id}", createdDTO.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Act
        MvcResult listResult = mockMvc.perform(get("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Assert - Verificar cadeia completa de integração
        String getResponseJson = getResult.getResponse().getContentAsString();
        EmployeeResponseDTO getDTO = objectMapper.readValue(getResponseJson, EmployeeResponseDTO.class);

        String listResponseJson = listResult.getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, EmployeeResponseDTO.class);
        List<EmployeeResponseDTO> listDTOs = objectMapper.readValue(listResponseJson, listType);

        // 1. HTTP Request → JSON Deserialization → DTO
        assertThat(createdDTO.getName(), is("Integration Test Employee"));
        assertThat(createdDTO.getEmail(), is("integration@itau.com.br"));

        // 2. Controller → Mapper → Use Case Request
        assertThat(createdDTO.getId(), is(notNullValue()));
        assertThat(createdDTO.getEmployeeStatus(), is(EmployeeStatus.ACTIVE));

        // 3. Use Case → Domain → Repository → Database Persistence
        List<EmployeeJpaEntity> dbEntities = employeeJpaRepository.findAll();
        assertThat(dbEntities, hasSize(1));
        EmployeeJpaEntity dbEntity = dbEntities.get(0);
        assertThat(dbEntity.getId(), is(createdDTO.getId()));
        assertThat(dbEntity.getName(), is("Integration Test Employee"));
        assertThat(dbEntity.getEmail(), is("integration@itau.com.br"));

        // 4. Database → Repository → Use Case → Controller → JSON Response
        assertThat(getDTO.getId(), is(createdDTO.getId()));
        assertThat(getDTO.getName(), is("Integration Test Employee"));
        assertThat(getDTO.getEmail(), is("integration@itau.com.br"));

        assertThat(listDTOs, hasSize(1));
        EmployeeResponseDTO listDTO = listDTOs.get(0);
        assertThat(listDTO.getId(), is(createdDTO.getId()));

        // 5. End-to-end consistency verification
        assertThat(getDTO.getId(), is(dbEntity.getId()));
        assertThat(getDTO.getName(), is(dbEntity.getName()));
        assertThat(getDTO.getEmail(), is(dbEntity.getEmail()));
        assertThat(getDTO.getEmployeeStatus(), is(dbEntity.getStatus()));
    }

    @Test
    @DisplayName("Should handle concurrent employee operations")
    @Transactional
    void shouldHandleConcurrentEmployeeOperations() throws Exception {
        // Arrange - Múltiplos employees únicos
        EmployeeRequestDTO emp1 = createEmployeeRequestDTO("John Doe", "john@itau.com.br");
        EmployeeRequestDTO emp2 = createEmployeeRequestDTO("Jane Smith", "jane@itau.com.br");
        EmployeeRequestDTO emp3 = createEmployeeRequestDTO("Bob Johnson", "bob@itau.com.br");

        // Act - Criar employees sequencialmente (simulando concorrência)
        MvcResult result1 = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp1)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result2 = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp2)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result3 = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp3)))
                .andExpect(status().isCreated())
                .andReturn();

        // Assert - Verificar que todos foram criados com IDs únicos
        EmployeeResponseDTO response1 = objectMapper.readValue(
            result1.getResponse().getContentAsString(), EmployeeResponseDTO.class);
        EmployeeResponseDTO response2 = objectMapper.readValue(
            result2.getResponse().getContentAsString(), EmployeeResponseDTO.class);
        EmployeeResponseDTO response3 = objectMapper.readValue(
            result3.getResponse().getContentAsString(), EmployeeResponseDTO.class);

        // IDs únicos
        assertThat(response1.getId(), is(not(equalTo(response2.getId()))));
        assertThat(response1.getId(), is(not(equalTo(response3.getId()))));
        assertThat(response2.getId(), is(not(equalTo(response3.getId()))));

        // Verificar persistência no database
        List<EmployeeJpaEntity> savedEntities = employeeJpaRepository.findAll();
        assertThat(savedEntities, hasSize(3));

        List<String> savedNames = savedEntities.stream()
            .map(EmployeeJpaEntity::getName)
            .toList();
        assertThat(savedNames, containsInAnyOrder("John Doe", "Jane Smith", "Bob Johnson"));

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Should validate content-type and accept headers")
    void shouldValidateContentTypeAndAcceptHeaders() throws Exception {
        // Arrange
        EmployeeRequestDTO requestDTO = createEmployeeRequestDTO("Test Employee", "test@itau.com.br");
        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act & Assert - Content-Type incorreto
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.TEXT_PLAIN) // ❌ Content-Type incorreto
                .content(requestJson))
                .andExpect(status().isUnsupportedMediaType());

        // Act & Assert - Content-Type correto
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON) // ✅ Content-Type correto
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Act & Assert - GET sempre funciona independente do Accept
        mockMvc.perform(get("/api/v1/employees")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // Helper method para criar EmployeeRequestDTO
    private EmployeeRequestDTO createEmployeeRequestDTO(String name, String email) {
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO();
        requestDTO.setName(name);
        requestDTO.setEmail(email);
        requestDTO.setDepartmentId(testDepartment.getId());
        requestDTO.setPositionId(testPosition.getId());
        return requestDTO;
    }
}