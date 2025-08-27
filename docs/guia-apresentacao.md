# Guia de Apresentação - Solicitação de Apólice MVP

Este guia foi elaborado para auxiliar na apresentação do MVP de Solicitação de Apólice, fornecendo um roteiro estruturado e dicas para destacar os pontos mais importantes do projeto.

## Objetivo da Apresentação

Demonstrar conhecimentos em:
- Java 21 + Spring Boot
- Arquitetura de software
- Event-Driven Architecture
- AWS (simulação)
- Observabilidade
- Boas práticas de desenvolvimento

## Roteiro de Apresentação

### 1. Introdução (2-3 minutos)

- **Apresente o desafio**: Explique brevemente o desafio proposto pelo Itaú
- **Contexto de negócio**: Explique o contexto de seguros e o papel do microsserviço
- **Objetivos do MVP**: Destaque o que o MVP se propõe a entregar

**Exemplo de fala:**
> "Este MVP implementa um microsserviço para gerenciamento de solicitações de apólice de seguros, seguindo uma arquitetura orientada a eventos. O sistema permite receber solicitações, validá-las com uma API de Fraudes, processar eventos de pagamento e subscrição, e gerenciar todo o ciclo de vida da solicitação."

### 2. Arquitetura (3-4 minutos)

- **Visão geral**: Apresente a arquitetura em camadas
- **Componentes principais**: Destaque os principais componentes e suas responsabilidades
- **Fluxo de dados**: Explique como os dados fluem pelo sistema

**Pontos a destacar:**
- Separação clara de responsabilidades entre as camadas
- Isolamento do domínio de negócio
- Arquitetura orientada a eventos
- Observabilidade integrada

**Exemplo de fala:**
> "Adotei uma arquitetura em camadas com influências de Clean Architecture e DDD, organizando o código em quatro camadas principais: Apresentação (API), Aplicação (Serviços), Domínio e Infraestrutura. Esta estrutura permite isolar o domínio de negócio das preocupações técnicas, facilitando testes e manutenção."

### 3. Demonstração do Código (5-6 minutos)

- **Estrutura do projeto**: Mostre a organização de pacotes e classes
- **Domínio**: Destaque as entidades e regras de negócio
- **Event-Driven**: Mostre os produtores e consumidores de eventos
- **Observabilidade**: Destaque os aspectos de logs, métricas e tracing

**Arquivos-chave para mostrar:**
- `SolicitacaoApolice.java`: Entidade principal com ciclo de vida
- `StatusSolicitacao.java`: Enumeração com estados e transições
- `RegraValidacaoCliente.java`: Regras de negócio para validação
- `SolicitacaoService.java`: Orquestração dos casos de uso
- `SolicitacaoEventProducer.java`: Publicação de eventos
- `SolicitacaoEventConsumer.java`: Consumo de eventos
- `ApiFraudesClientMock.java`: Mock da API de Fraudes
- `ObservabilityConfig.java`: Configuração de observabilidade

**Exemplo de fala:**
> "Aqui na classe SolicitacaoApolice, implementei o ciclo de vida da solicitação com validações de transições de estado. Cada alteração de estado é registrada no histórico para auditoria. Na classe RegraValidacaoCliente, implementei as regras de validação de capital segurado para diferentes tipos de cliente e categorias de seguro."

### 4. Demonstração Funcional (5-6 minutos)

- **Execução da aplicação**: Mostre como executar o projeto
- **Fluxo completo**: Demonstre o fluxo completo de uma solicitação
- **Observabilidade**: Mostre logs, métricas e tracing em ação

**Passos para demonstração:**
1. Inicie o RabbitMQ com Docker Compose
2. Execute a aplicação Spring Boot
3. Crie uma solicitação via API REST
4. Observe os logs da operação
5. Verifique o evento publicado no RabbitMQ
6. Observe o processamento da validação
7. Consulte a solicitação para ver o novo estado
8. Simule eventos de pagamento e subscrição
9. Verifique o estado final da solicitação
10. Mostre as métricas no Actuator

**Exemplo de fala:**
> "Vou demonstrar o fluxo completo de uma solicitação. Primeiro, envio uma requisição POST para criar uma nova solicitação. Nos logs, podemos ver o ID de transação que permite rastrear toda a operação. No RabbitMQ, vemos o evento SolicitacaoRecebidaEvent publicado. O consumer processa o evento, consulta a API de Fraudes e aplica as regras de validação."

### 5. Decisões Técnicas (3-4 minutos)

- **Arquitetura**: Explique por que escolheu a arquitetura em camadas
- **Tecnologias**: Justifique as escolhas tecnológicas
- **Simplificações**: Explique o que foi simplificado para o MVP
- **Melhorias futuras**: Mencione o que poderia ser melhorado em uma versão completa

**Pontos a destacar:**
- Escolha do H2 para simplificar a configuração
- RabbitMQ como broker de mensagens pela simplicidade e interface amigável
- Mock da API de Fraudes conforme solicitado no desafio
- Simulação da AWS para demonstrar o conceito sem necessidade de credenciais reais

**Exemplo de fala:**
> "Para o MVP, optei por um banco de dados em memória (H2) para simplificar a configuração e execução. Em um ambiente de produção, seria substituído por um banco relacional robusto como PostgreSQL. Escolhi o RabbitMQ como broker de mensagens pela sua simplicidade de configuração e boa integração com Spring Boot via Spring AMQP."

### 6. Testes (2-3 minutos)

- **Abordagem de testes**: Explique a estratégia de testes
- **Testes unitários**: Mostre exemplos de testes de domínio
- **Testes de integração**: Mostre exemplos de testes de API

**Arquivos-chave para mostrar:**
- `StatusSolicitacaoTest.java`: Testes unitários para enumeração
- `RegraValidacaoClienteTest.java`: Testes unitários para regras de negócio
- `SolicitacaoServiceTest.java`: Testes unitários para serviço
- `SolicitacaoControllerTest.java`: Testes de integração para API

**Exemplo de fala:**
> "Implementei testes unitários para as classes de domínio, focando nas regras de negócio e validações. Por exemplo, aqui no RegraValidacaoClienteTest, verifico se as regras de capital segurado são aplicadas corretamente para diferentes tipos de cliente. Também implementei testes de integração para os controllers, validando o comportamento da API."

### 7. Observabilidade (2-3 minutos)

- **Logs**: Mostre a configuração de logs estruturados
- **Métricas**: Demonstre as métricas coletadas
- **Tracing**: Explique como o tracing é implementado com MDC

**Pontos a destacar:**
- Logs estruturados com SLF4J/Logback
- Métricas com Micrometer e Spring Boot Actuator
- Tracing simplificado com MDC
- Aspectos para instrumentação automática

**Exemplo de fala:**
> "Implementei os três pilares de observabilidade: logs, métricas e tracing. Os logs são estruturados com SLF4J/Logback, incluindo contexto de MDC para correlacionar logs de uma mesma transação. As métricas são coletadas com Micrometer e expostas pelo Spring Boot Actuator, permitindo monitorar operações críticas como consultas à API de Fraudes e processamento de eventos."

### 8. Conclusão (1-2 minutos)

- **Recapitulação**: Resuma os principais pontos apresentados
- **Valor entregue**: Destaque o valor do MVP para o desafio
- **Próximos passos**: Mencione possíveis evoluções do projeto

**Exemplo de fala:**
> "Este MVP demonstra uma implementação completa do microsserviço de Solicitação de Apólice, seguindo boas práticas de arquitetura, com foco em separação de responsabilidades, testabilidade e observabilidade. A arquitetura orientada a eventos permite desacoplamento e escalabilidade, enquanto a estrutura em camadas facilita a manutenção e evolução do sistema."

## Dicas para a Apresentação

### Preparação

- **Ambiente pronto**: Tenha o ambiente configurado e testado antes da apresentação
- **Dados de exemplo**: Prepare dados de exemplo para demonstração
- **Terminal limpo**: Use um terminal limpo para evitar distrações
- **Editor configurado**: Configure seu editor para facilitar a navegação entre arquivos

### Durante a Apresentação

- **Foco no essencial**: Concentre-se nos aspectos mais importantes do projeto
- **Código limpo**: Mostre trechos de código relevantes e bem comentados
- **Linguagem clara**: Use termos técnicos, mas explique-os quando necessário
- **Conexão com requisitos**: Relacione suas escolhas com os requisitos do desafio
- **Honestidade**: Seja honesto sobre simplificações e limitações do MVP

### Possíveis Perguntas

Esteja preparado para responder perguntas como:

1. **Por que escolheu arquitetura em camadas em vez de hexagonal?**
   > "Optei pela arquitetura em camadas pela sua clareza e simplicidade, especialmente para um MVP didático. A arquitetura hexagonal seria uma alternativa válida, mas poderia adicionar complexidade desnecessária para o escopo do desafio."

2. **Como garantir idempotência no processamento de eventos?**
   > "No MVP, não implementei mecanismos robustos de idempotência, mas em um ambiente de produção, utilizaria identificadores únicos de eventos e registro de eventos processados para evitar duplicidade."

3. **Como lidar com falhas na API de Fraudes?**
   > "Implementaria um circuit breaker com Resilience4j para evitar falhas em cascata, além de um mecanismo de retry com backoff exponencial para tentativas automáticas em caso de falhas temporárias."

4. **Como escalar o sistema para um volume maior de solicitações?**
   > "O design permite escalabilidade horizontal, com serviços stateless que podem ser replicados. Utilizaria um balanceador de carga e múltiplas instâncias do serviço, além de escalar o RabbitMQ com clusters."

5. **Como implementaria segurança no sistema?**
   > "Utilizaria Spring Security com OAuth2 e JWT para autenticação e autorização, HTTPS para comunicação criptografada, validação rigorosa de entrada para proteção contra injeção e outros ataques, e auditoria de operações sensíveis."

## Conclusão

Este guia fornece uma estrutura para apresentar o MVP de Solicitação de Apólice de forma clara e eficaz. Adapte-o conforme necessário para destacar os aspectos mais relevantes do seu projeto e demonstrar seu conhecimento técnico.

