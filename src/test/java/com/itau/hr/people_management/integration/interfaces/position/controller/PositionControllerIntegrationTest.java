package com.itau.hr.people_management.integration.interfaces.position.controller;

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
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;
import com.itau.hr.people_management.infrastructure.persistence.entity.PositionJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.repository.JpaPositionRepository;
import com.itau.hr.people_management.interfaces.position.dto.PositionRequestDTO;
import com.itau.hr.people_management.interfaces.position.dto.PositionResponseDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("PositionController Integration Tests")
class PositionControllerIntegrationTest {

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
    private PositionRepository positionRepository;

    @Autowired
    private JpaPositionRepository positionJpaRepository;

    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        positionJpaRepository.deleteAll();
        positionJpaRepository.flush();
    }

    @Test
    @DisplayName("Should create position successfully via POST /api/v1/positions")
    void shouldCreatePositionSuccessfully() throws Exception {
        // Arrange
        PositionRequestDTO requestDTO = new PositionRequestDTO();
        requestDTO.setTitle("Senior Software Engineer");
        requestDTO.setPositionLevel("SENIOR");

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Senior Software Engineer"))
                .andExpect(jsonPath("$.positionLevel").value("SENIOR"))
                .andReturn();

        // Verificar resposta JSON
        String responseJson = result.getResponse().getContentAsString();
        PositionResponseDTO responseDTO = objectMapper.readValue(responseJson, PositionResponseDTO.class);
        
        assertThat(responseDTO.getId(), is(notNullValue()));
        assertThat(responseDTO.getTitle(), is("Senior Software Engineer"));
        assertThat(responseDTO.getPositionLevel(), is(PositionLevel.SENIOR));

        // Verificar persistência no database
        List<PositionJpaEntity> savedEntities = positionJpaRepository.findAll();
        assertThat(savedEntities, hasSize(1));
        
        PositionJpaEntity savedEntity = savedEntities.get(0);
        assertThat(savedEntity.getId(), is(responseDTO.getId()));
        assertThat(savedEntity.getTitle(), is("Senior Software Engineer"));
        assertThat(savedEntity.getPositionLevel(), is(PositionLevel.SENIOR));
    }

    @Test
    @DisplayName("Should return 400 when creating position with invalid data")
    void shouldReturn400WhenCreatingPositionWithInvalidData() throws Exception {
        // Arrange - DTO com dados inválidos
        PositionRequestDTO invalidRequestDTO = new PositionRequestDTO();
        invalidRequestDTO.setTitle(""); // Título vazio - inválido
        invalidRequestDTO.setPositionLevel("INVALID_LEVEL"); // Level inválido

        String requestJson = objectMapper.writeValueAsString(invalidRequestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verificar que nada foi salvo no database
        List<PositionJpaEntity> savedEntities = positionJpaRepository.findAll();
        assertThat(savedEntities, empty());
    }

    @Test
    @DisplayName("Should return 400 when creating position with null fields")
    void shouldReturn400WhenCreatingPositionWithNullFields() throws Exception {
        // Arrange - DTO com campos null
        PositionRequestDTO invalidRequestDTO = new PositionRequestDTO();
        // title e positionLevel ficam null

        String requestJson = objectMapper.writeValueAsString(invalidRequestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        // Verificar que nada foi salvo no database
        List<PositionJpaEntity> savedEntities = positionJpaRepository.findAll();
        assertThat(savedEntities, empty());
    }

    @Test
    @DisplayName("Should return 409 when creating position with duplicate title and level")
    void shouldReturn409WhenCreatingPositionWithDuplicateTitleAndLevel() throws Exception {
        // Arrange - Criar position existente no database
        Position existingPosition = Position.create(
            UUID.randomUUID(),
            "Software Developer",
            PositionLevel.JUNIOR
        );
        positionRepository.save(existingPosition);

        // Tentar criar nova position com mesmo título e nível
        PositionRequestDTO duplicateRequestDTO = new PositionRequestDTO();
        duplicateRequestDTO.setTitle("Software Developer");
        duplicateRequestDTO.setPositionLevel("JUNIOR"); // Mesmo título e nível

        String requestJson = objectMapper.writeValueAsString(duplicateRequestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verificar que apenas a position original existe
        List<PositionJpaEntity> savedEntities = positionJpaRepository.findAll();
        assertThat(savedEntities, hasSize(1));
        assertThat(savedEntities.get(0).getTitle(), is("Software Developer"));
        assertThat(savedEntities.get(0).getPositionLevel(), is(PositionLevel.JUNIOR));
    }

    @Test
    @DisplayName("Should allow same title with different position levels")
    void shouldAllowSameTitleWithDifferentPositionLevels() throws Exception {
        // Arrange - Criar position existente
        Position existingPosition = Position.create(
            UUID.randomUUID(),
            "Software Developer",
            PositionLevel.JUNIOR
        );
        positionRepository.save(existingPosition);

        // Criar nova position com mesmo título mas nível diferente
        PositionRequestDTO requestDTO = new PositionRequestDTO();
        requestDTO.setTitle("Software Developer");
        requestDTO.setPositionLevel("SENIOR"); // Mesmo título, nível diferente

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Software Developer"))
                .andExpect(jsonPath("$.positionLevel").value("SENIOR"));

        // Verificar que ambas as positions existem
        List<PositionJpaEntity> savedEntities = positionJpaRepository.findAll();
        assertThat(savedEntities, hasSize(2));
        
        List<PositionLevel> levels = savedEntities.stream()
            .map(PositionJpaEntity::getPositionLevel)
            .toList();
        assertThat(levels, containsInAnyOrder(PositionLevel.JUNIOR, PositionLevel.SENIOR));
    }

    @Test
    @DisplayName("Should return 400 when sending malformed JSON")
    void shouldReturn400WhenSendingMalformedJson() throws Exception {
        // Arrange - JSON malformado
        String malformedJson = "{ \"title\": \"Developer\", \"positionLevel\": }"; // JSON inválido

        // Act & Assert
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all positions successfully via GET /api/v1/positions")
    void shouldGetAllPositionsSuccessfully() throws Exception {
        // Arrange - Criar positions no database
        Position pos1 = Position.create(UUID.randomUUID(), "Software Engineer", PositionLevel.SENIOR);
        Position pos2 = Position.create(UUID.randomUUID(), "Product Manager", PositionLevel.PLENO);
        Position pos3 = Position.create(UUID.randomUUID(), "Data Scientist", PositionLevel.JUNIOR);

        positionRepository.save(pos1);
        positionRepository.save(pos2);
        positionRepository.save(pos3);

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$[*].title").exists())
                .andExpect(jsonPath("$[*].positionLevel").exists())
                .andReturn();

        // Verificar resposta detalhada
        String responseJson = result.getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, PositionResponseDTO.class);
        List<PositionResponseDTO> responseDTOs = objectMapper.readValue(responseJson, listType);

        assertThat(responseDTOs, hasSize(3));

        // Verificar que todas as positions estão presentes
        List<String> titles = responseDTOs.stream()
            .map(PositionResponseDTO::getTitle)
            .toList();
        assertThat(titles, containsInAnyOrder(
            "Software Engineer", 
            "Product Manager", 
            "Data Scientist"
        ));

        List<PositionLevel> levels = responseDTOs.stream()
            .map(PositionResponseDTO::getPositionLevel)
            .toList();
        assertThat(levels, containsInAnyOrder(
            PositionLevel.SENIOR,
            PositionLevel.PLENO,
            PositionLevel.JUNIOR
        ));

        // Verificar que todos têm IDs válidos
        responseDTOs.forEach(dto -> {
            assertThat(dto.getId(), is(notNullValue()));
            assertThat(dto.getTitle(), is(not(emptyString())));
            assertThat(dto.getPositionLevel(), is(notNullValue()));
        });
    }

    @Test
    @DisplayName("Should return empty list when no positions exist")
    void shouldReturnEmptyListWhenNoPositionsExist() throws Exception {
        // Arrange - Database vazio (já limpo no @BeforeEach)

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        // Verificar resposta
        String responseJson = result.getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, PositionResponseDTO.class);
        List<PositionResponseDTO> responseDTOs = objectMapper.readValue(responseJson, listType);

        assertThat(responseDTOs, empty());
    }

    @Test
    @DisplayName("Should verify complete integration chain: HTTP → Controller → Use Case → Repository → Database")
    void shouldVerifyCompleteIntegrationChainHttpToControllerToUseCaseToRepositoryToDatabase() throws Exception {
        // Arrange
        PositionRequestDTO requestDTO = new PositionRequestDTO();
        requestDTO.setTitle("Full Stack Developer");
        requestDTO.setPositionLevel("PLENO");

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act - POST para criar
        MvcResult createResult = mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Verificar resposta da criação
        String createResponseJson = createResult.getResponse().getContentAsString();
        PositionResponseDTO createdDTO = objectMapper.readValue(createResponseJson, PositionResponseDTO.class);

        // Act - GET para listar
        MvcResult listResult = mockMvc.perform(get("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Assert - Verificar cadeia completa de integração
        String listResponseJson = listResult.getResponse().getContentAsString();
        CollectionType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, PositionResponseDTO.class);
        List<PositionResponseDTO> listDTOs = objectMapper.readValue(listResponseJson, listType);

        // 1. HTTP Request → JSON Deserialization → DTO
        assertThat(createdDTO.getTitle(), is("Full Stack Developer"));
        assertThat(createdDTO.getPositionLevel(), is(PositionLevel.PLENO));

        // 2. Controller → Mapper → Use Case Request
        assertThat(createdDTO.getId(), is(notNullValue()));
        assertThat(createdDTO.getId().toString().matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"), is(true));

        // 3. Use Case → Domain → Repository → Database Persistence
        List<PositionJpaEntity> dbEntities = positionJpaRepository.findAll();
        assertThat(dbEntities, hasSize(1));
        PositionJpaEntity dbEntity = dbEntities.get(0);
        assertThat(dbEntity.getId(), is(createdDTO.getId()));
        assertThat(dbEntity.getTitle(), is("Full Stack Developer"));
        assertThat(dbEntity.getPositionLevel(), is(PositionLevel.PLENO));

        // 4. Database → Repository → Use Case → Controller → JSON Response
        assertThat(listDTOs, hasSize(1));
        PositionResponseDTO retrievedDTO = listDTOs.get(0);
        assertThat(retrievedDTO.getId(), is(createdDTO.getId()));
        assertThat(retrievedDTO.getTitle(), is("Full Stack Developer"));
        assertThat(retrievedDTO.getPositionLevel(), is(PositionLevel.PLENO));

        // 5. End-to-end consistency verification
        assertThat(retrievedDTO.getId(), is(dbEntity.getId()));
        assertThat(retrievedDTO.getTitle(), is(dbEntity.getTitle()));
        assertThat(retrievedDTO.getPositionLevel(), is(dbEntity.getPositionLevel()));
    }

    @Test
    @DisplayName("Should handle concurrent position creation requests")
    @Transactional
    void shouldHandleConcurrentPositionCreationRequests() throws Exception {
        // Arrange - Múltiplas positions únicas
        PositionRequestDTO pos1 = new PositionRequestDTO();
        pos1.setTitle("Backend Developer");
        pos1.setPositionLevel("SENIOR");

        PositionRequestDTO pos2 = new PositionRequestDTO();
        pos2.setTitle("Frontend Developer");
        pos2.setPositionLevel("JUNIOR");

        PositionRequestDTO pos3 = new PositionRequestDTO();
        pos3.setTitle("DevOps Engineer");
        pos3.setPositionLevel("PLENO");

        // Act - Criar positions sequencialmente (simulando concorrência)
        MvcResult result1 = mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pos1)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result2 = mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pos2)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result3 = mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pos3)))
                .andExpect(status().isCreated())
                .andReturn();

        // Assert - Verificar que todas foram criadas com IDs únicos
        PositionResponseDTO response1 = objectMapper.readValue(
            result1.getResponse().getContentAsString(), PositionResponseDTO.class);
        PositionResponseDTO response2 = objectMapper.readValue(
            result2.getResponse().getContentAsString(), PositionResponseDTO.class);
        PositionResponseDTO response3 = objectMapper.readValue(
            result3.getResponse().getContentAsString(), PositionResponseDTO.class);

        // IDs únicos
        assertThat(response1.getId(), is(not(equalTo(response2.getId()))));
        assertThat(response1.getId(), is(not(equalTo(response3.getId()))));
        assertThat(response2.getId(), is(not(equalTo(response3.getId()))));

        // Verificar persistência no database
        List<PositionJpaEntity> savedEntities = positionJpaRepository.findAll();
        assertThat(savedEntities, hasSize(3));

        List<String> savedTitles = savedEntities.stream()
            .map(PositionJpaEntity::getTitle)
            .toList();
        assertThat(savedTitles, containsInAnyOrder(
            "Backend Developer", 
            "Frontend Developer", 
            "DevOps Engineer"
        ));

        // Verificar que GET retorna todas
        mockMvc.perform(get("/api/v1/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Should validate content-type and accept headers")
    void shouldValidateContentTypeAndAcceptHeaders() throws Exception {
        // Arrange
        PositionRequestDTO requestDTO = new PositionRequestDTO();
        requestDTO.setTitle("QA Engineer");
        requestDTO.setPositionLevel("PLENO");

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        // Act & Assert - Content-Type incorreto
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.TEXT_PLAIN) // ❌ Content-Type incorreto
                .content(requestJson))
                .andExpect(status().isUnsupportedMediaType());

        // Act & Assert - Content-Type correto
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON) // ✅ Content-Type correto
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Act & Assert - GET sempre funciona independente do Accept
        mockMvc.perform(get("/api/v1/positions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should validate position level enum values")
    void shouldValidatePositionLevelEnumValues() throws Exception {
        // Test all valid position levels
        String[] validLevels = {"JUNIOR", "PLENO", "SENIOR"};
        
        for (String level : validLevels) {
            // Arrange
            PositionRequestDTO requestDTO = new PositionRequestDTO();
            requestDTO.setTitle("Developer " + level);
            requestDTO.setPositionLevel(level);

            String requestJson = objectMapper.writeValueAsString(requestDTO);

            // Act & Assert
            mockMvc.perform(post("/api/v1/positions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.positionLevel").value(level));
        }

        // Verificar que todas as 3 positions foram criadas
        List<PositionJpaEntity> savedEntities = positionJpaRepository.findAll();
        assertThat(savedEntities, hasSize(3));
    }
}