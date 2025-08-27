# Solicitação de Apólice MVP - Desafio Itaú

Este projeto é um MVP  para o desafio de Software Engineer do Itaú, implementando um microsserviço para gerenciamento de solicitações de apólice de seguros com arquitetura orientada a eventos (Event-Driven Architecture).

## Índice

- [Requisitos do Case](#requisitos-do-case)
- [Arquitetura](#arquitetura)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Como Executar](#como-executar)
- [Interagindo com a Aplicação](#interagindo-com-a-aplicação)
- [Observabilidade](#observabilidade)
- [Testes](#testes)
- [Decisões Técnicas](#decisões-técnicas)
- [Premissas Assumidas](#premissas-assumidas)
- [Apresentando a Solução](#apresentando-a-solução)

## Requisitos do Case

O desafio consiste em desenvolver um microsserviço para gerenciar o ciclo de vida de solicitações de apólice de seguros da seguradora ACME. O sistema deve:

1. Receber solicitações de apólice via API REST
2. Processar solicitações consultando uma API de Fraudes (mockada)
3. Permitir consultas por ID da solicitação ou ID do cliente
4. Receber e processar eventos de pagamento e subscrição
5. Permitir cancelamento de solicitações
6. Gerenciar o ciclo de vida da solicitação através de estados
7. Publicar eventos para outros serviços

O ciclo de vida da solicitação segue os estados: RECEBIDO → VALIDADO → PENDENTE → APROVADA/REJEITADA, com possibilidade de CANCELADA em determinados momentos.

## Arquitetura

Para este MVP, foi adotada uma **Arquitetura em Camadas** com foco no domínio, inspirada em princípios de Clean Architecture e Domain-Driven Design (DDD). Esta escolha visa equilibrar a clareza didática com boas práticas de engenharia de software.

### Camadas da Aplicação

1. **Camada de Apresentação (API)**
   - Controllers REST
   - DTOs (Data Transfer Objects)
   - Validações de entrada

2. **Camada de Aplicação (Serviços)**
   - Orquestração dos casos de uso
   - Transações
   - Coordenação entre domínio e infraestrutura

3. **Camada de Domínio**
   - Entidades de negócio (SolicitacaoApolice, etc.)
   - Regras de negócio (validação de capital segurado)
   - Eventos de domínio
   - Interfaces de repositórios

4. **Camada de Infraestrutura**
   - Implementação de repositórios (JPA)
   - Mensageria (RabbitMQ)
   - Clientes externos (API de Fraudes)
   - Configurações técnicas

### Fluxo Principal

O fluxo principal da aplicação segue o ciclo de vida da solicitação de apólice:

1. Cliente envia solicitação via API REST
2. Sistema persiste a solicitação com status RECEBIDO
3. Evento SolicitacaoRecebidaEvent é publicado
4. Consumer processa o evento e consulta API de Fraudes
5. Regras de validação são aplicadas com base na classificação do cliente
6. Status é atualizado para VALIDADO ou REJEITADA
7. Evento correspondente é publicado
8. Eventos externos de pagamento e subscrição são processados
9. Status é atualizado para APROVADA ou REJEITADA

## Tecnologias Utilizadas

- **Java 17**: Versão LTS mais recente do Java
- **Spring Boot 3.x**: Framework para desenvolvimento de aplicações Java
- **Spring Data JPA**: Para persistência de dados
- **H2 Database**: Banco de dados em memória para o MVP
- **RabbitMQ**: Broker de mensagens para arquitetura orientada a eventos
- **Spring AMQP**: Integração com RabbitMQ
- **Spring Boot Actuator**: Endpoints para observabilidade
- **SLF4J/Logback**: Logging estruturado
- **Micrometer**: Biblioteca para métricas
- **JUnit 5**: Framework de testes
- **Mockito**: Framework para mocks em testes
- **Docker/Docker Compose**: Containerização da aplicação e dependências

## Estrutura do Projeto

```
src/
  main/
    java/
      com/itau/seguros/solicitacao/
        SolicitacaoApplication.java
        config/                      # Configurações da aplicação
        api/                         # Camada de apresentação
          controller/                # Controllers REST
          dto/                       # DTOs de request/response
        application/                 # Camada de aplicação
          service/                   # Serviços de orquestração
        domain/                      # Camada de domínio
          model/                     # Entidades e objetos de valor
          event/                     # Eventos de domínio
          repository/                # Interfaces de repositórios
          rule/                      # Regras de negócio
        infrastructure/              # Camada de infraestrutura
          persistence/               # Implementação de repositórios
          messaging/                 # Producers e consumers
          external/                  # Clientes de APIs externas
          aws/                       # Simulação de integração AWS
    resources/
      application.yml                # Configurações da aplicação
      logback-spring.xml             # Configuração de logs
  test/
    java/                            # Testes unitários e de integração
```

## Como Executar

### Pré-requisitos

- Java 17
- Docker e Docker Compose
- Maven

### Passos para Execução

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/itau-seguros-mvp.git
   cd itau-seguros-mvp
   ```

2. Execute o Docker Compose para iniciar o RabbitMQ:
   ```bash
   docker-compose up -d rabbitmq
   ```

3. Compile e execute a aplicação:
   ```bash
   ./mvnw clean package
   ./mvnw spring-boot:run
   ```

4. Alternativamente, execute tudo via Docker Compose:
   ```bash
   docker-compose up -d
   ```

### Acessando a Aplicação

- API REST: http://localhost:8080/api/solicitacoes
- Console H2: http://localhost:8080/h2-console
- RabbitMQ Management: http://localhost:15672 (guest/guest)
- Actuator: http://localhost:8080/actuator

## Interagindo com a Aplicação

### API REST

#### Criar Solicitação

```bash
curl -X POST http://localhost:8080/api/solicitacoes \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
    "productId": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
    "category": "AUTO",
    "salesChannel": "MOBILE",
    "paymentMethod": "CREDIT_CARD",
    "totalMonthlyPremiumAmount": 75.25,
    "insuredAmount": 275000.50,
    "coverages": {
      "Roubo": 100000.25,
      "Perda Total": 100000.25,
      "Colisão com Terceiros": 75000.00
    },
    "assistances": [
      "Guincho até 250km",
      "Troca de Óleo",
      "Chaveiro 24h"
    ]
  }'
```

#### Consultar Solicitação por ID

```bash
curl -X GET http://localhost:8080/api/solicitacoes/{id}
```

#### Consultar Solicitações por Cliente

```bash
curl -X GET http://localhost:8080/api/solicitacoes?customerId={customerId}
```

#### Cancelar Solicitação

```bash
curl -X PUT http://localhost:8080/api/solicitacoes/{id}/cancelar
```

### Mensageria (RabbitMQ)

Para simular eventos de pagamento e subscrição, você pode publicar mensagens no RabbitMQ usando a interface de gerenciamento ou via AMQP.

#### Evento de Pagamento Confirmado

```json
{
  "solicitacaoId": "89846cee-c6d5-4320-92e9-16e122d5c672",
  "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "valor": 75.25,
  "formaPagamento": "CREDIT_CARD",
  "dataConfirmacao": "2023-10-01T14:01:30Z"
}
```

#### Evento de Subscrição Autorizada

```json
{
  "solicitacaoId": "89846cee-c6d5-4320-92e9-16e122d5c672",
  "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "autorizado": true,
  "dataAutorizacao": "2023-10-01T14:02:00Z"
}
```

## Observabilidade

O MVP implementa três pilares de observabilidade:

### Logs

- Logs estruturados com SLF4J/Logback
- Formato padronizado com timestamp, thread, nível, ID de transação, ID de cliente
- Contexto de MDC para correlacionar logs de uma mesma transação
- Configuração para saída em console e arquivo

### Métricas

- Spring Boot Actuator expõe métricas em /actuator/metrics
- Micrometer para instrumentação de código
- Contadores para eventos de negócio (solicitações recebidas, validadas, etc.)
- Timers para operações críticas (consulta à API de Fraudes, processamento de eventos)

### Tracing

- Implementação simplificada com MDC (Mapped Diagnostic Context)
- ID de transação gerado para cada operação
- Propagação de contexto entre threads e componentes

## Testes

O projeto inclui testes unitários e de integração para demonstrar a abordagem de testes:

### Testes Unitários

- Testes de domínio (entidades, regras de negócio)
- Testes de serviços com mocks
- Cobertura de casos de sucesso e falha

### Testes de Integração

- Testes de controllers com MockMvc
- Verificação de respostas HTTP e validações

## Decisões Técnicas

### Banco de Dados em Memória (H2)

Para o MVP, optou-se por um banco de dados em memória para simplificar a configuração e execução. Em um ambiente de produção, seria substituído por um banco relacional robusto como PostgreSQL ou MySQL.

### RabbitMQ como Message Broker

O RabbitMQ foi escolhido por sua simplicidade de configuração, interface de gerenciamento amigável e boa integração com Spring Boot via Spring AMQP. É ideal para demonstrar o conceito de Event-Driven Architecture no MVP.

### Mock da API de Fraudes

Conforme solicitado no desafio, a API de Fraudes foi mockada internamente, retornando classificações de risco pré-definidas com base no ID do cliente. Em um cenário real, seria um serviço externo.

### Simulação AWS

A integração com AWS foi simulada através de uma classe mock (AwsSqsClient) que apenas loga as operações. Em um ambiente real, utilizaria o AWS SDK para Java.

### Arquitetura em Camadas

A arquitetura em camadas foi escolhida por sua clareza e facilidade de compreensão, especialmente para um MVP didático. Ela permite isolar o domínio e as regras de negócio das preocupações técnicas.

## Premissas Assumidas

1. **Simplificação de Regras**: Implementamos todas as regras de validação por tipo de cliente e categoria de seguro, mas em um cenário real, essas regras poderiam ser mais complexas e dinâmicas.

2. **Transações**: Assumimos que cada operação de alteração de estado é uma transação atômica.

3. **Idempotência**: Não implementamos mecanismos robustos de idempotência para processamento de eventos, o que seria necessário em um ambiente de produção.

4. **Segurança**: Não implementamos autenticação e autorização, que seriam essenciais em um ambiente real.

5. **Resiliência**: Implementamos tratamento básico de erros, mas em produção seria necessário um sistema mais robusto com retry, circuit breaker, etc.

## Apresentando a Solução

### Pontos-chave para Destacar

1. **Arquitetura Limpa**: Separação clara de responsabilidades entre as camadas, com o domínio no centro.

2. **Event-Driven**: Demonstração de arquitetura orientada a eventos com publicação e consumo de mensagens.

3. **Observabilidade**: Implementação dos três pilares (logs, métricas, tracing) para monitoramento.

4. **Testes**: Abordagem de testes unitários e de integração para garantir qualidade.

5. **Simulação AWS**: Demonstração de como seria a integração com serviços AWS.

### Exemplo de Fluxo para Apresentação

1. **Criar Solicitação**: Mostrar a criação de uma solicitação via API REST.
2. **Verificar Logs**: Observar os logs da operação com ID de transação.
3. **Consultar RabbitMQ**: Verificar o evento publicado no RabbitMQ.
4. **Processar Validação**: Observar o processamento do evento e consulta à API de Fraudes.
5. **Verificar Estado**: Consultar a solicitação para ver o novo estado.
6. **Simular Pagamento**: Publicar um evento de pagamento confirmado.
7. **Simular Subscrição**: Publicar um evento de subscrição autorizada.
8. **Verificar Aprovação**: Consultar a solicitação para ver o estado final.
9. **Mostrar Métricas**: Acessar o Actuator para ver as métricas coletadas.

---

Este MVP foi desenvolvido como parte do processo seletivo para Engenheiro Sênior Java no Itaú, seguindo os requisitos do desafio e buscando demonstrar conhecimentos em Java, Spring Boot, arquitetura de software, AWS, Event-Driven e observabilidade.

