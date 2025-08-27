# Documentação de Arquitetura - Solicitação de Apólice MVP

## Visão Geral

Este documento detalha a arquitetura do microsserviço de Solicitação de Apólice desenvolvido para o desafio do Itaú. A arquitetura foi projetada para ser clara, didática e seguir boas práticas de desenvolvimento de software, com foco em:

- Separação de responsabilidades
- Isolamento do domínio de negócio
- Testabilidade
- Observabilidade
- Arquitetura orientada a eventos (Event-Driven)

## Arquitetura em Camadas

O projeto segue uma arquitetura em camadas com influências de Clean Architecture e Domain-Driven Design (DDD), organizando o código em quatro camadas principais:

### 1. Camada de Apresentação (API)

Responsável pela interface com o mundo externo, principalmente através de APIs REST.

**Componentes:**
- **Controllers**: Recebem requisições HTTP, delegam para serviços e retornam respostas.
- **DTOs (Data Transfer Objects)**: Objetos para transferência de dados entre a API e os clientes.
- **Validações**: Validações de entrada para garantir a integridade dos dados.

**Exemplos:**
- `SolicitacaoController`: Expõe endpoints para criar, consultar e cancelar solicitações.
- `SolicitacaoRequestDTO`: Representa os dados de entrada para criação de uma solicitação.
- `SolicitacaoResponseDTO`: Representa os dados de saída de uma solicitação.

### 2. Camada de Aplicação (Serviços)

Orquestra os fluxos de negócio, coordenando a interação entre o domínio e a infraestrutura.

**Componentes:**
- **Serviços**: Implementam casos de uso da aplicação.
- **Transações**: Gerenciamento de transações.
- **Mapeamento**: Conversão entre DTOs e entidades de domínio.

**Exemplos:**
- `SolicitacaoService`: Orquestra operações como criar, validar e cancelar solicitações.

### 3. Camada de Domínio

O coração da aplicação, contendo as entidades de negócio e regras de negócio.

**Componentes:**
- **Entidades**: Objetos de negócio com identidade e ciclo de vida.
- **Value Objects**: Objetos imutáveis que representam conceitos do domínio.
- **Eventos de Domínio**: Representam fatos ocorridos no domínio.
- **Regras de Negócio**: Lógica específica do domínio.
- **Interfaces de Repositórios**: Contratos para persistência de entidades.

**Exemplos:**
- `SolicitacaoApolice`: Entidade principal que representa uma solicitação de apólice.
- `StatusSolicitacao`: Enumeração que representa os estados possíveis de uma solicitação.
- `SolicitacaoRecebidaEvent`: Evento que representa o recebimento de uma nova solicitação.
- `RegraValidacaoCliente`: Regras para validação de capital segurado por tipo de cliente.
- `SolicitacaoRepository`: Interface para operações de persistência de solicitações.

### 4. Camada de Infraestrutura

Implementa detalhes técnicos e integração com sistemas externos.

**Componentes:**
- **Repositórios**: Implementação de persistência de dados.
- **Mensageria**: Publicação e consumo de eventos.
- **Clientes Externos**: Integração com APIs externas.
- **Configurações**: Configurações técnicas da aplicação.

**Exemplos:**
- `SolicitacaoEventProducer`: Publica eventos no RabbitMQ.
- `SolicitacaoEventConsumer`: Consome eventos do RabbitMQ.
- `ApiFraudesClientMock`: Mock da API de Fraudes.
- `RabbitMQConfig`: Configuração do RabbitMQ.

## Fluxo de Dados

### Criação de Solicitação

1. `SolicitacaoController` recebe uma requisição POST com `SolicitacaoRequestDTO`
2. `SolicitacaoService` valida os dados e cria uma nova `SolicitacaoApolice`
3. `SolicitacaoRepository` persiste a entidade no banco de dados
4. `SolicitacaoEventProducer` publica um `SolicitacaoRecebidaEvent`
5. `SolicitacaoController` retorna um `SolicitacaoResponseDTO` com os dados da solicitação criada

### Processamento de Validação

1. `SolicitacaoEventConsumer` consome um `SolicitacaoRecebidaEvent`
2. `SolicitacaoService` busca a solicitação pelo ID
3. `ApiFraudesClient` consulta a classificação de risco do cliente
4. `RegraValidacaoCliente` aplica regras de validação com base na classificação
5. `SolicitacaoService` atualiza o status da solicitação
6. `SolicitacaoRepository` persiste a atualização
7. `SolicitacaoEventProducer` publica um evento de resultado (validada ou rejeitada)

### Processamento de Eventos Externos

1. `PagamentoEventConsumer` ou `SubscricaoEventConsumer` consome um evento externo
2. `SolicitacaoService` busca a solicitação pelo ID
3. `SolicitacaoService` atualiza o status da solicitação
4. `SolicitacaoRepository` persiste a atualização
5. `SolicitacaoEventProducer` publica um evento de resultado (aprovada ou rejeitada)

## Observabilidade

A arquitetura incorpora observabilidade em vários níveis:

### Logs

- **Logs Estruturados**: Formato padronizado com informações contextuais
- **MDC (Mapped Diagnostic Context)**: Correlação de logs de uma mesma transação
- **Níveis de Log**: Diferentes níveis para diferentes tipos de informação

### Métricas

- **Contadores**: Contagem de eventos de negócio
- **Timers**: Medição de tempo de operações críticas
- **Gauges**: Medição de valores instantâneos
- **Tags**: Categorização de métricas para análise

### Tracing

- **ID de Transação**: Identificador único para cada operação
- **Propagação de Contexto**: Passagem de contexto entre componentes
- **Logs Correlacionados**: Logs com o mesmo ID de transação

## Event-Driven Architecture

A arquitetura é orientada a eventos, com os seguintes componentes:

### Eventos

- **Eventos de Domínio**: Representam fatos ocorridos no domínio
- **Eventos Externos**: Representam fatos ocorridos em sistemas externos

### Produtores

- **SolicitacaoEventProducer**: Publica eventos no RabbitMQ

### Consumidores

- **SolicitacaoEventConsumer**: Consome eventos de solicitação
- **PagamentoEventConsumer**: Consome eventos de pagamento
- **SubscricaoEventConsumer**: Consome eventos de subscrição

### Broker de Mensagens

- **RabbitMQ**: Broker de mensagens para comunicação assíncrona

## Ciclo de Vida da Solicitação

O ciclo de vida da solicitação é gerenciado através de estados e transições:

1. **RECEBIDO**: Estado inicial quando uma solicitação é criada
   - Transições permitidas: VALIDADO, CANCELADA

2. **VALIDADO**: Estado após validação pela API de Fraudes
   - Transições permitidas: PENDENTE, REJEITADA, CANCELADA

3. **PENDENTE**: Estado aguardando pagamento e subscrição
   - Transições permitidas: APROVADA, REJEITADA, CANCELADA

4. **APROVADA**: Estado final após confirmação de pagamento e subscrição
   - Estado final, sem transições permitidas

5. **REJEITADA**: Estado final após rejeição por regras de negócio, pagamento ou subscrição
   - Estado final, sem transições permitidas

6. **CANCELADA**: Estado após cancelamento pelo cliente
   - Estado final, sem transições permitidas

## Integração com AWS (Simulada)

A integração com AWS é simulada através de classes mock:

- **AwsSqsClient**: Simula o envio de mensagens para o Amazon SQS

Em um ambiente real, estas classes seriam substituídas por implementações que utilizam o AWS SDK para Java.

## Considerações de Segurança

O MVP não implementa mecanismos de segurança, mas em um ambiente de produção seriam necessários:

- **Autenticação e Autorização**: OAuth2, JWT, Spring Security
- **HTTPS**: Comunicação criptografada
- **Validação de Entrada**: Proteção contra injeção e outros ataques
- **Auditoria**: Registro de operações sensíveis

## Considerações de Escalabilidade

O design permite escalabilidade horizontal:

- **Stateless**: Serviços sem estado podem ser replicados
- **Mensageria**: Comunicação assíncrona permite desacoplamento
- **Banco de Dados**: Pode ser escalado independentemente

## Decisões Arquiteturais

### Arquitetura em Camadas vs. Hexagonal

Optou-se pela arquitetura em camadas por sua simplicidade e clareza, especialmente para um MVP didático. A arquitetura hexagonal (ports and adapters) seria uma alternativa válida, mas poderia adicionar complexidade desnecessária para o escopo do desafio.

### Banco de Dados em Memória

O H2 foi escolhido para simplificar a configuração e execução do MVP. Em produção, seria substituído por um banco de dados relacional robusto como PostgreSQL ou MySQL.

### RabbitMQ como Message Broker

O RabbitMQ foi escolhido por sua simplicidade de configuração e interface de gerenciamento amigável. Alternativas como Kafka seriam mais adequadas para cenários com maior volume de eventos e necessidade de processamento de streams.

### Mock da API de Fraudes

A API de Fraudes foi mockada internamente para simplificar o MVP. Em um cenário real, seria um serviço externo com sua própria infraestrutura.

## Conclusão

A arquitetura do microsserviço de Solicitação de Apólice foi projetada para ser clara, didática e seguir boas práticas de desenvolvimento de software. Ela permite a implementação dos requisitos do desafio de forma estruturada e extensível, com foco em separação de responsabilidades, testabilidade e observabilidade.

