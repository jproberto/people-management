# Uso de IA Generativa no Projeto

## 🤖 Ferramentas Utilizadas

- **Google Gemini**: Arquitetura, design patterns e decisões técnicas
- **GitHub Copilot (com Claude Sonnet 4)**: Implementação de código, autocomplete inteligente e documentação

## 🎯 Processo de Iteração com IA
### 1. Desenvolvimento Incremental
```
1. Prompt inicial (arquitetura geral)
2. Refinamento (detalhes específicos)
3. Implementação (código assistido)
4. Correção (debugging com IA)
5. Otimização (melhorias contínuas)
```

### 2. Validação Contínua
- Cada sugestão da IA foi **questionada** e **validada**
- Testes executados após cada implementação
- Refatoração constante com feedback de IA

## ⌨️ Prompts Mais Efetivos

### Elaboração de Roteiro Inicial
**Prompt:** Estou participando de um desafio técnico para uma vaga de engenheiro de software Sênior num grande banco. Abaixo estão as informações. Me ajude a elaborar um roteiro com os passos necessários para desenvolver esse projeto. (Em anexo, o PDF com as instruções do desafio)

**Resultado:** Um roteiro dividido em 8 fases, cada uma com diversas etapas, que ajudou a guiar o desenvolvimento do projeto. Resumidamente:
```
Fase 1: Setup e Configuração Inicial (criação do projeto, estrutura de pacotes configuração de banco, Flyway, Swagger)
Fase 2: Modelagem do Domínio (Definição das entidades, agregados e VOs)
Fase 3: Camada de Aplicação (Definição dos Use Cases, DTOs de Entrada e Saída)
Fase 4: Camada de Infraestrutura (Implementação dos repositórios, configuração da Mensageria, implementação de Listeners de eventos internos)
Fase 5: Camada de Interfaces (Criação dos REST controllers)
Fase 6: Testes (Testes unitários, testes de integração, análise de cobertura)
Fase 7: Containerização e Documentação
Fase 8: Diferenciais Técnicos
```

### Discussões Técnicas Para Tomadas de Decisão
**Prompt:** Na entidade Positions temos o campo "level", que possui alguns valores pré-definidos. Podemos fazer um Enum, que no banco de dados seria refletido em uma coluna com constraint, ou uma classe PositionLevel, que seria uma tabela separada. Liste as vantagens e desvantagens de cada uma.

**Resultado:** A resposta trouxe pontos como flexibilidade versus complexidade inicial, se os valores são dinâmicos ou com pouca probabilidade de mudança, reutilização por outros sistemas, implicações técnicas e aderência ao escopo e à arquitetura. Ainda fiz algumas perguntas e refinamentos em cima da resposta, o que me ajudou a chegar à decisão de manter esse campo como enum.

### Configuração de CI/CD
**Prompt:** Crie um pipeline GitHub Actions simples mas eficaz para: executar testes, fazer build do JAR e verificar se o Docker build funciona.

**Resultado:** Pipeline CI/CD funcional e profissional, mas sem complexidade excessiva.

## 📈 Qualidade Melhorada
- **Cobertura de testes:** Mais cenários sugeridos pela IA
- **Error handling:** Casos edge identificados
- **Best practices:** Patterns modernos aplicados
- **Código limpo:** Refatorações constantes

## 🏗️ Decisões Arquiteturais Influenciadas por IA
 ### 1. Estrutura de Agregados DDD
- **Discussão**: Como modelar Employee, Department e Position como agregados
- **Decisão**: Employee como agregado raiz, com referências por ID
- **Implementação**: Value Objects (Email), Enums (EmployeeStatus, PositionLevel)

### 2. Event-Driven Architecture
- **Discussão**: Spring Events vs Kafka para comunicação assíncrona
- **Decisão**: Kafka com consumers internos
- **Implementação**: Topics `employee.created` e `employee.status.changed`

### 3. Mapeamento entre Camadas
- **Discussão**: Como fazer conversões Domain ↔ Application ↔ Interface
- **Decisão**: Mappers específicos em cada camada
- **Implementação**: EmployeeMapper, EmployeeControllerMapper

## 🐛 Limitações e Soluções
### 1. Dependências Deprecated
- **Problema:** IA sugeriu spring-cloud-stream versão antiga
- **Solução:** Pesquisa manual e upgrade para versão compatível
- **Aprendizado:** Sempre validar versões de dependências

### 2. Configuração Específica do Spring Boot 3.4.x
- **Problema:** IA não conhecia mudanças recentes no ControllerAdviceBean
- **Solução:** Pesquisa na documentação oficial + trial and error
- **Aprendizado:** IA pode estar desatualizada para versões muito recentes

### 3. Configuração de Testcontainers
- **Problema:** IA gerou configuração genérica que não funcionava
- **Solução:** Adaptação manual para PostgreSQL específico
- **Aprendizado:** Configurações de infraestrutura precisam ser validadas

## 🎓 Aprendizados
### 1. IA como Pair Programming
- **Vantagem:** Feedback instantâneo e múltiplas perspectivas
- **Desafio:** Validar todas as sugestões
- **Resultado:** Código mais robusto e bem estruturado

### 2. Iteração Rápida
- **Vantagem:** Protótipos rápidos e experimentação
- **Desafio:** Manter qualidade em velocidade alta
- **Resultado:** MVP funcional em tempo reduzido

💡 **Conclusão:** A IA Generativa foi fundamental para acelerar o desenvolvimento mantendo alta qualidade técnica. O segredo está na iteração constante e validação crítica de todas as sugestões.