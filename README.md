# 📊 People Management API

> Sistema de gestão de colaboradores desenvolvido com **arquitetura DDD/Hexagonal**, **Spring Boot 3.x**, **PostgreSQL**, **Kafka** e **CI/CD automatizado**.

[![CI](https://github.com/jproberto/people-management/actions/workflows/ci.yml/badge.svg)](https://github.com/your-username/people-management/actions)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.8-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.6.0-red)

---

## 🎯 Objetivo

Sistema backend para o time de **Gente & Gestão** responsável por:

- ✅ **Gerenciar colaboradores** (CRUD completo)
- ✅ **Controlar estrutura organizacional** (departamentos e cargos)
- ✅ **Disparar eventos assíncronos** para integrações
- ✅ **Permitir extensibilidade futura** com baixo acoplamento

---

## 🏗️ Arquitetura

### Domain-Driven Design (DDD) + Hexagonal Architecture

```
📁 src/main/java/com/itau/hr/people_management/ 
├── 🧠 domain/ # Camada de Domínio (zero dependências) 
│ ├── department/ # Agregado Department 
│ │ ├── entity/ # Department.java 
│ │ └── repository/ # DepartmentRepository - Interface 
│ ├── employee/ # Agregado Employee 
│ │ ├── entity/ # Employee.java - Entidade rica 
│ │ ├── enumeration/ # EmployeeStatus.java - Enums de domínio 
│ │ ├── event/ # EmployeeCreatedEvent, EmployeeStatusChangedEvent 
│ │ ├── history/ # EmployeeHistory - Value Object 
│ │ ├── criteria/ # EmployeeSearchCriteria - Critérios de busca 
│ │ └── repository/ # EmployeeRepository - Interface 
│ ├── position/ # Agregado Position 
│ │ ├── entity/ # Position.java 
│ │ ├── enumeration/ # PositionLevel.java 
│ │ └── repository/ # PositionRepository - Interface 
│ ├── shared/ # Elementos compartilhados 
│ │ ├── event/ # DomainEvent
│ │ ├── exception / # BusinessException, ConflictException, NotFoundException
│ │ ├── message/ # DomainMessageSource
│ │ └── vo/ # Email
├── 🔧 application/ # Casos de Uso (Application Services) 
│ ├── department/ 
│ │ ├── dto/ # CreateDepartmentRequest, DepartmentResponse 
│ │ └── usecase/ # CreateDepartmentUseCase, GetDepartmentUseCase
│ ├── employee/ 
│ │ ├── dto/ # ChangeEmployeeStatusRequest, CreateEmployeeRequest, EmployeeResponse, SearchEmployeeRequest
│ │ └── usecase/ # ChangeEmployeeStatusUseCase, CreateEmployeeUseCase, DeleteEmployeeUseCase, GetEmployeeUseCase, ReactivateEmployeeUseCase, SearchEmployeesUseCase  
│ └── position/ 
│ | ├── dto/ # CreatePositionRequest, PositionResponse 
│ | └── usecase/ # CreatePositionUseCase, GetPositionUseCase 
├── 🔌 infrastructure/ # Adaptadores (Frameworks & Drivers) 
│ ├── kafka/ # Kafka Producers & Consumers 
│ ├── outbox/ # Outbox Pattern Implementation 
│ ├── persistence/ # JPA Entities & Repository Implementations 
│ └── shared/ 
| │ ├── mapper/ # EmployeeMapper, DepartmentMapper, PositionMapper 
| | └── message/ # DomainMessageSourceInitializer, SpringDomainMessageSource
├── 🌐 interfaces/ # Interface Adapters (Controllers & DTOs) 
│ ├── department/ 
│ │ ├── controller/ # DepartmentController 
│ │ ├── dto/ # DepartmentRequestDTO, DepartmentResponseDTO 
│ │ └── mapper/ # DepartmentControllerMapper 
│ ├── employee/ 
│ │ ├── controller/ # EmployeeController 
│ │ ├── dto/ # EmployeeRequestDTO, EmployeeResponseDTO, EmployeeSearchRequestDTO 
│ │ └── mapper/ # EmployeeControllerMapper 
│ ├── position/ 
│ │ ├── controller/ # PositionController 
│ │ ├── dto/ # PositionRequestDTO, PositionResponseDTO 
│ │ └── mapper/ # PositionControllerMapper 
│ └── shared/ 
│ │ ├── dto/ # ApiErrorResponse 
│ │ └── exception_handler/ # GlobalExceptionHandler 
├── 🔧 config/ # Configurações do Spring 
│ │ ├── KafkaTopicConfig.java # Configuração dos tópicos Kafka 
│ │ └── OpenApiConfig.java # Configuração do Swagger/OpenAPI
```

### Principais Patterns Implementados:

- **🎯 DDD**: Entidades ricas, agregados, value objects
- **🔷 Hexagonal Architecture**: Domínio isolado de frameworks
- **📤 Outbox Pattern**: Garantia de consistência eventual
- **🎪 Event-Driven**: Comunicação assíncrona via Kafka

---

## 🚀 Execução Rápida

### Opção 1: Script Automatizado (Recomendado)

```cmd
# Clone o repositório
git clone <repo-url>
cd people-management

# Execute o script automatizado
run.bat

# Escolha a opção:
# [1] RUN LOCALLY (Infrastructure Docker + Spring Boot local)
# [2] RUN WITH DOCKER (Everything in containers)
```

### Opção 2: Docker Compose

```bash
# Subir tudo com Docker
docker-compose up --build -d

# Verificar status
docker-compose ps

# Logs da aplicação
docker-compose logs -f people-management-app
```

### Opção 3: Desenvolvimento Local

```bash
# 1. Subir infraestrutura
docker-compose up -d postgres kafka zookeeper

# 2. Executar aplicação local
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 📋 Endpoints da API

### 🔗 URLs Principais:
- **Aplicação**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health
- **Kafka UI**: http://localhost:9000 (Kafdrop)

### 📊 Endpoints Disponíveis:

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/v1/employees` | Listar todos os colaboradores |
| `POST` | `/api/v1/employees` | Criar novo colaborador |
| `GET` | `/api/v1/employees/{id}` | Buscar colaborador por ID |
| `DELETE` | `/api/v1/employees/{id}` | Remover colaborador |
| `GET` | `/api/v1/employees/search` | Buscar por critérios |
| `PATCH` | `/api/v1/employees/{id}/ status` |  Alterar status do colaborador |
| `POST` | `/api/v1/employees/{id}/reactivate` | Reativar colaborador |
| `POST` | `/api/v1/departments` | Criar departamento |
| `GET` | `/api/v1/departments` | Listar departamentos |
| `POST` | `/api/v1/positions` | Criar cargo |
| `GET` | `/api/v1/positions` | Listar cargos |

### 🔍 Exemplos de Uso:

```bash
# Criar colaborador
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao.silva@itau.com.br",
    "departmentId": "uuid-do-departamento",
    "positionId": "uuid-do-cargo"
  }'

# Buscar por departamento
curl "http://localhost:8080/api/v1/employees/search?department=RH"

# Listar cargos
curl "http://localhost:8080/api/v1/positions"
```

### 📖 Documentação da API (Swagger):

**Acesse a documentação interativa completa:**

🔗 **[Swagger UI - People Management API](http://localhost:8080/swagger-ui/index.html)**

**Características da documentação:**
- ✅ **Testável**: Execute requests diretamente no browser
- ✅ **Completa**: Todos os endpoints documentados
- ✅ **Exemplos**: Payloads de request/response
- ✅ **Schemas**: Modelos de dados detalhados
- ✅ **Códigos de resposta**: Status HTTP com descrições 

---

## 🔄 Eventos Assíncronos

### Apache Kafka Topics:

| Topic | Evento | Descrição |
|-------|--------|-----------|
| `employee.created` | `EmployeeCreatedEvent` | Disparado ao criar colaborador |
| `employee.status.changed` | `EmployeeStatusChangedEvent` | Disparado ao mudar status |

### Consumers Implementados:

1. **📝 EmployeeEventLogger**: Gera logs estruturados
2. **📚 EmployeeHistoryUpdater**: Atualiza tabela `employee_events_history`

---

## 💾 Banco de Dados

### PostgreSQL (Padrão)

**Credenciais:**
- Host: `localhost:5432`
- Database: `people_management`
- Username: `people_admin`
- Password: `people_password_2024`

### Migrations (Flyway):

```sql
-- V1__create_initial_tables.sql
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    cost_center_code VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS positions (
    id UUID PRIMARY KEY,
    title VARCHAR(100) NOT NULL, 
    position_level VARCHAR(50) NOT NULL,
    CONSTRAINT uk_positions_title_level UNIQUE (title, position_level) 
);

CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department_id UUID,
    position_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (position_id) REFERENCES positions(id)
);
```

---

## 🧪 Testes

### Executar Testes:

```bash
# Todos os testes
./mvnw test

# Apenas testes unitários
./mvnw test -Dtest="**/*Test"

# Apenas testes de integração
./mvnw test -Dtest="**/*IntegrationTest"

# Com cobertura
./mvnw clean test jacoco:report
```

### Tecnologias de Teste:

- ✅ **JUnit 5** + **Mockito** (unitários)
- ✅ **Testcontainers** (PostgreSQL real)
- ✅ **@EmbeddedKafka** (mensageria)
- ✅ **Awaitility** (testes assíncronos)

---

## 🔧 Build & Deploy

### Tecnologias:

- **Java 21**
- **Spring Boot 3.4.8**
- **Maven 3.9+**
- **Docker & Docker Compose**

### Build Local:

```bash
# Compilar
./mvnw clean compile

# Gerar JAR
./mvnw clean package

# Executar JAR
java -jar target/people-management-*.jar
```

### Docker:

```bash
# Build da imagem
docker build -t people-management:latest .

# Executar container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  people-management:latest
```

---

## 🚀 CI/CD Pipeline

### GitHub Actions:

```yaml
# .github/workflows/ci.yml
name: CI - Build and Test

on:
  push:
    branches: [ main, develop ]

jobs:
  test-and-build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
    - run: chmod +x ./mvnw
    - name: 🧪 Run tests
      run: ./mvnw clean test
    - name: 🏗️ Build JAR
      run: ./mvnw package -DskipTests
```

### Pipeline Features:

- ✅ **Testes automatizados** (unitários + integração)
- ✅ **Build do JAR** para deployment
- ✅ **Feedback** automático via GitHub

---

## 📊 Observabilidade

### Spring Actuator:

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`
- **Env**: `/actuator/env`

### Logs Estruturados:

```json
{
  "timestamp": "2024-08-03T18:30:15.123Z",
  "level": "INFO",
  "logger": "EmployeeEventLogger",
  "message": "EVENT_RECEIVED: EMPLOYEE_CREATED_EVENT",
  "employee_id": "550e8400-e29b-41d4-a716-446655440000",
  "event_id": "123e4567-e89b-12d3-a456-426614174000"
}
```

---

## 🤖 Uso de IA Generativa

### Ferramentas Utilizadas:

- **🧠 Google Gemini**: Arquitetura e design
- **🎯 GitHub Copilot**: Implementação, autocomplete e documentação

### Decisões Influenciadas por IA:

1. **Testcontainers**: Sugestão para testes reais
2. **Event-Driven Architecture**: Análise de trade-offs
3. **Estrutura de packages**: Organização hexagonal

### Processo de Desenvolvimento:

- ✅ **Iteração contínua** com feedback da IA
- ✅ **Validação** de decisões arquiteturais
- ✅ **Otimização** de código e testes
- ✅ **Documentação** assistida

**📄 Detalhes completos:** [AI_USAGE.md](AI_USAGE.md)

---

## ✨ Highlights Técnicos

### 🏆 Diferenciais Implementados:

- ✅ **DDD Real**: Entidades ricas, agregados, value objects
- ✅ **Hexagonal Architecture**: Zero dependência de frameworks no domínio
- ✅ **Outbox Pattern**: Consistência eventual garantida
- ✅ **Event-Driven**: Comunicação assíncrona desacoplada
- ✅ **Testcontainers**: Testes com infraestrutura real
- ✅ **CI/CD**: Pipeline automatizado completo
- ✅ **Observabilidade**: Logs estruturados + métricas
- ✅ **Documentation**: Swagger + README completo

---

## 🤝 Contribuição

```bash
# Fork o projeto
git clone <your-fork>

# Criar branch feature
git checkout -b feature/nova-funcionalidade

# Fazer alterações e commit
git commit -m "feat: adicionar nova funcionalidade"

# Push e criar PR
git push origin feature/nova-funcionalidade
```

---

## 📄 Licença

Este projeto foi desenvolvido como **desafio técnico** para processo seletivo.

---

**🚀 Desenvolvido com ❤️ usando DDD, Spring Boot e IA Generativa**
