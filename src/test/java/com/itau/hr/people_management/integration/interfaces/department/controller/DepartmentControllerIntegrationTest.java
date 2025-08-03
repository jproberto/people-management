package com.itau.hr.people_management.integration.interfaces.department.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;
import com.itau.hr.people_management.infrastructure.persistence.entity.DepartmentJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.repository.JpaDepartmentRepository;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentRequestDTO;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentResponseDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) 
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("DepartmentController Integration Tests")
class DepartmentControllerIntegrationTest {

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
    private DepartmentRepository departmentRepository;

    @Autowired
    private JpaDepartmentRepository departmentJpaRepository;

    @BeforeEach
    void setUp() {
        departmentJpaRepository.deleteAll();
        departmentJpaRepository.flush();
    }

    @Test
    @DisplayName("Should create department successfully via POST /api/v1/departments")
    void shouldCreateDepartmentSuccessfully() throws Exception {
        // Arrange
        DepartmentRequestDTO requestDTO = new DepartmentRequestDTO();
        requestDTO.setName("Information Technology");
        requestDTO.setCostCenterCode("IT001");

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Information Technology"))
                .andExpect(jsonPath("$.costCenterCode").value("IT001"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        DepartmentResponseDTO responseDTO = objectMapper.readValue(responseJson, DepartmentResponseDTO.class);
        
        assertThat(responseDTO.getId(), is(notNullValue()));
        assertThat(responseDTO.getName(), is("Information Technology"));
        assertThat(responseDTO.getCostCenterCode(), is("IT001"));

        List<DepartmentJpaEntity> savedEntities = departmentJpaRepository.findAll();
        assertThat(savedEntities, hasSize(1));
        
        DepartmentJpaEntity savedEntity = savedEntities.get(0);
        assertThat(savedEntity.getId(), is(responseDTO.getId()));
        assertThat(savedEntity.getName(), is("Information Technology"));
        assertThat(savedEntity.getCostCenterCode(), is("IT001"));
    }

    @Test
    @DisplayName("Should return 400 when creating department with invalid data")
    void shouldReturn400WhenCreatingDepartmentWithInvalidData() throws Exception {
        // Arrange - DTO com dados inválidos
        DepartmentRequestDTO invalidRequestDTO = new DepartmentRequestDTO();
        invalidRequestDTO.setName(""); // Nome vazio - inválido
        invalidRequestDTO.setCostCenterCode(""); // Cost center vazio - inválido

        String requestJson = objectMapper.writeValueAsString(invalidRequestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verificar que nada foi salvo no database
        List<DepartmentJpaEntity> savedEntities = departmentJpaRepository.findAll();
        assertThat(savedEntities, empty());
    }

    @Test
    @DisplayName("Should return 400 when creating department with null fields")
    void shouldReturn400WhenCreatingDepartmentWithNullFields() throws Exception {
        // Arrange - DTO com campos null
        DepartmentRequestDTO invalidRequestDTO = new DepartmentRequestDTO();
        // name e costCenterCode ficam null

        String requestJson = objectMapper.writeValueAsString(invalidRequestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        // Verificar que nada foi salvo no database
        List<DepartmentJpaEntity> savedEntities = departmentJpaRepository.findAll();
        assertThat(savedEntities, empty());
    }

    @Test
    @DisplayName("Should return 409 when creating department with duplicate cost center code")
    void shouldReturn409WhenCreatingDepartmentWithDuplicateCostCenterCode() throws Exception {
        // Arrange - Criar department existente no database
        Department existingDepartment = Department.create(
            UUID.randomUUID(),
            "Human Resources",
            "HR001"
        );
        departmentRepository.save(existingDepartment);

        // Tentar criar novo department com mesmo cost center code
        DepartmentRequestDTO duplicateRequestDTO = new DepartmentRequestDTO();
        duplicateRequestDTO.setName("Human Resources - New");
        duplicateRequestDTO.setCostCenterCode("HR001"); // Mesmo código

        String requestJson = objectMapper.writeValueAsString(duplicateRequestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verificar que apenas o department original existe
        List<DepartmentJpaEntity> savedEntities = departmentJpaRepository.findAll();
        assertThat(savedEntities, hasSize(1));
        assertThat(savedEntities.get(0).getName(), is("Human Resources"));
    }

    @Test
    @DisplayName("Should return 400 when sending malformed JSON")
    void shouldReturn400WhenSendingMalformedJson() throws Exception {
        // Arrange - JSON malformado
        String malformedJson = "{ \"name\": \"IT\", \"costCenterCode\": }"; // JSON inválido

        // Act & Assert
        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all departments successfully via GET /api/v1/departments")
    void shouldGetAllDepartmentsSuccessfully() throws Exception {
        // Arrange - Criar departments no database
        Department dept1 = Department.create(
            UUID.randomUUID(),
            "Information Technology",
            "IT001"
        );
        Department dept2 = Department.create(
            UUID.randomUUID(),
            "Human Resources",
            "HR001"
        );
        Department dept3 = Department.create(
            UUID.randomUUID(),
            "Finance",
            "FIN001"
        );

        departmentRepository.save(dept1);
        departmentRepository.save(dept2);
        departmentRepository.save(dept3);

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$[*].name").exists())
                .andExpect(jsonPath("$[*].costCenterCode").exists())
                .andReturn();

        // Verificar resposta detalhada
        String responseJson = result.getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, DepartmentResponseDTO.class);
        List<DepartmentResponseDTO> responseDTOs = objectMapper.readValue(responseJson, listType);

        assertThat(responseDTOs, hasSize(3));

        // Verificar que todos os departments estão presentes
        List<String> names = responseDTOs.stream()
            .map(DepartmentResponseDTO::getName)
            .toList();
        assertThat(names, containsInAnyOrder(
            "Information Technology", 
            "Human Resources", 
            "Finance"
        ));

        List<String> costCenterCodes = responseDTOs.stream()
            .map(DepartmentResponseDTO::getCostCenterCode)
            .toList();
        assertThat(costCenterCodes, containsInAnyOrder(
            "IT001", 
            "HR001", 
            "FIN001"
        ));

        // Verificar que todos têm IDs válidos
        responseDTOs.forEach(dto -> {
            assertThat(dto.getId(), is(notNullValue()));
            assertThat(dto.getName(), is(not(emptyString())));
            assertThat(dto.getCostCenterCode(), is(not(emptyString())));
        });
    }

    @Test
    @DisplayName("Should return empty list when no departments exist")
    void shouldReturnEmptyListWhenNoDepartmentsExist() throws Exception {
        // Arrange - Database vazio (já limpo no @BeforeEach)

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        // Verificar resposta
        String responseJson = result.getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, DepartmentResponseDTO.class);
        List<DepartmentResponseDTO> responseDTOs = objectMapper.readValue(responseJson, listType);

        assertThat(responseDTOs, empty());
    }

    @Test
    @DisplayName("Should verify complete integration chain: HTTP → Controller → Use Case → Repository → Database")
    void shouldVerifyCompleteIntegrationChainHttpToControllerToUseCaseToRepositoryToDatabase() throws Exception {
        // Arrange
        DepartmentRequestDTO requestDTO = new DepartmentRequestDTO();
        requestDTO.setName("Research and Development");
        requestDTO.setCostCenterCode("RD001");

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act - POST para criar
        MvcResult createResult = mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Verificar resposta da criação
        String createResponseJson = createResult.getResponse().getContentAsString();
        DepartmentResponseDTO createdDTO = objectMapper.readValue(createResponseJson, DepartmentResponseDTO.class);

        // Act - GET para listar
        MvcResult listResult = mockMvc.perform(get("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Assert - Verificar cadeia completa de integração
        String listResponseJson = listResult.getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, DepartmentResponseDTO.class);
        List<DepartmentResponseDTO> listDTOs = objectMapper.readValue(listResponseJson, listType);

        // 1. HTTP Request → JSON Deserialization → DTO
        assertThat(createdDTO.getName(), is("Research and Development"));
        assertThat(createdDTO.getCostCenterCode(), is("RD001"));

        // 2. Controller → Mapper → Use Case Request
        assertThat(createdDTO.getId(), is(notNullValue()));
        assertThat(createdDTO.getId().toString().matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"), is(true));

        // 3. Use Case → Domain → Repository → Database Persistence
        List<DepartmentJpaEntity> dbEntities = departmentJpaRepository.findAll();
        assertThat(dbEntities, hasSize(1));
        DepartmentJpaEntity dbEntity = dbEntities.get(0);
        assertThat(dbEntity.getId(), is(createdDTO.getId()));
        assertThat(dbEntity.getName(), is("Research and Development"));
        assertThat(dbEntity.getCostCenterCode(), is("RD001"));

        // 4. Database → Repository → Use Case → Controller → JSON Response
        assertThat(listDTOs, hasSize(1));
        DepartmentResponseDTO retrievedDTO = listDTOs.get(0);
        assertThat(retrievedDTO.getId(), is(createdDTO.getId()));
        assertThat(retrievedDTO.getName(), is("Research and Development"));
        assertThat(retrievedDTO.getCostCenterCode(), is("RD001"));

        // 5. End-to-end consistency verification
        assertThat(retrievedDTO.getId(), is(dbEntity.getId()));
        assertThat(retrievedDTO.getName(), is(dbEntity.getName()));
        assertThat(retrievedDTO.getCostCenterCode(), is(dbEntity.getCostCenterCode()));
    }

    @Test
    @DisplayName("Should handle concurrent department creation requests")
    @Transactional
    void shouldHandleConcurrentDepartmentCreationRequests() throws Exception {
        // Arrange - Múltiplos departments únicos
        DepartmentRequestDTO dept1 = new DepartmentRequestDTO();
        dept1.setName("Marketing");
        dept1.setCostCenterCode("MKT001");

        DepartmentRequestDTO dept2 = new DepartmentRequestDTO();
        dept2.setName("Sales");
        dept2.setCostCenterCode("SAL001");

        DepartmentRequestDTO dept3 = new DepartmentRequestDTO();
        dept3.setName("Operations");
        dept3.setCostCenterCode("OPS001");

        // Act - Criar departments sequencialmente (simulando concorrência)
        MvcResult result1 = mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dept1)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result2 = mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dept2)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result3 = mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dept3)))
                .andExpect(status().isCreated())
                .andReturn();

        // Assert - Verificar que todos foram criados com IDs únicos
        DepartmentResponseDTO response1 = objectMapper.readValue(
            result1.getResponse().getContentAsString(), DepartmentResponseDTO.class);
        DepartmentResponseDTO response2 = objectMapper.readValue(
            result2.getResponse().getContentAsString(), DepartmentResponseDTO.class);
        DepartmentResponseDTO response3 = objectMapper.readValue(
            result3.getResponse().getContentAsString(), DepartmentResponseDTO.class);

        // IDs únicos
        assertThat(response1.getId(), is(not(equalTo(response2.getId()))));
        assertThat(response1.getId(), is(not(equalTo(response3.getId()))));
        assertThat(response2.getId(), is(not(equalTo(response3.getId()))));

        List<DepartmentJpaEntity> savedEntities = departmentJpaRepository.findAll();
        assertThat(savedEntities, hasSize(3));

        List<String> savedNames = savedEntities.stream()
            .map(DepartmentJpaEntity::getName)
            .toList();
        assertThat(savedNames, containsInAnyOrder("Marketing", "Sales", "Operations"));

        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Should validate content-type and accept headers")
    void shouldValidateContentTypeAndAcceptHeaders() throws Exception {
        // Arrange
        DepartmentRequestDTO requestDTO = new DepartmentRequestDTO();
        requestDTO.setName("Legal");
        requestDTO.setCostCenterCode("LEG001");

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act & Assert - Content-Type incorreto
        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.TEXT_PLAIN) // ❌ Content-Type incorreto
                .content(requestJson))
                .andExpect(status().isUnsupportedMediaType());

        // Act & Assert - Content-Type correto
        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON) // ✅ Content-Type correto
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Act & Assert - GET sempre funciona independente do Accept
        mockMvc.perform(get("/api/v1/departments")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}