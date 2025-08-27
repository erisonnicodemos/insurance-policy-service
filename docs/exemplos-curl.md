# Exemplos de Uso da API - Solicitação de Apólice MVP

Este documento contém exemplos práticos de como interagir com a API REST do microsserviço de Solicitação de Apólice e com o RabbitMQ para simulação de eventos externos.

## API REST

### Criar Solicitação

#### Requisição

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

#### Resposta

```json
{
  "id": "89846cee-c6d5-4320-92e9-16e122d5c672",
  "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "productId": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
  "category": "AUTO",
  "salesChannel": "MOBILE",
  "paymentMethod": "CREDIT_CARD",
  "status": "RECEBIDO",
  "createdAt": "2023-10-01T14:00:00Z",
  "finishedAt": null,
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
  ],
  "history": [
    {
      "status": "RECEBIDO",
      "timestamp": "2023-10-01T14:00:00Z",
      "observacao": "Solicitação recebida"
    }
  ]
}
```

### Consultar Solicitação por ID

#### Requisição

```bash
curl -X GET http://localhost:8080/api/solicitacoes/89846cee-c6d5-4320-92e9-16e122d5c672
```

#### Resposta

```json
{
  "id": "89846cee-c6d5-4320-92e9-16e122d5c672",
  "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "productId": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
  "category": "AUTO",
  "salesChannel": "MOBILE",
  "paymentMethod": "CREDIT_CARD",
  "status": "VALIDADO",
  "createdAt": "2023-10-01T14:00:00Z",
  "finishedAt": null,
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
  ],
  "history": [
    {
      "status": "RECEBIDO",
      "timestamp": "2023-10-01T14:00:00Z",
      "observacao": "Solicitação recebida"
    },
    {
      "status": "VALIDADO",
      "timestamp": "2023-10-01T14:00:30Z",
      "observacao": "Validação aprovada pela API de Fraudes"
    }
  ]
}
```

### Consultar Solicitações por Cliente

#### Requisição

```bash
curl -X GET http://localhost:8080/api/solicitacoes?customerId=adc56d77-348c-4bf0-908f-22d402ee715c
```

#### Resposta

```json
[
  {
    "id": "89846cee-c6d5-4320-92e9-16e122d5c672",
    "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
    "productId": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
    "category": "AUTO",
    "salesChannel": "MOBILE",
    "paymentMethod": "CREDIT_CARD",
    "status": "VALIDADO",
    "createdAt": "2023-10-01T14:00:00Z",
    "finishedAt": null,
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
    ],
    "history": [
      {
        "status": "RECEBIDO",
        "timestamp": "2023-10-01T14:00:00Z",
        "observacao": "Solicitação recebida"
      },
      {
        "status": "VALIDADO",
        "timestamp": "2023-10-01T14:00:30Z",
        "observacao": "Validação aprovada pela API de Fraudes"
      }
    ]
  }
]
```

### Cancelar Solicitação

#### Requisição

```bash
curl -X PUT http://localhost:8080/api/solicitacoes/89846cee-c6d5-4320-92e9-16e122d5c672/cancelar
```

#### Resposta

```json
{
  "id": "89846cee-c6d5-4320-92e9-16e122d5c672",
  "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "productId": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
  "category": "AUTO",
  "salesChannel": "MOBILE",
  "paymentMethod": "CREDIT_CARD",
  "status": "CANCELADA",
  "createdAt": "2023-10-01T14:00:00Z",
  "finishedAt": "2023-10-01T14:05:00Z",
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
  ],
  "history": [
    {
      "status": "RECEBIDO",
      "timestamp": "2023-10-01T14:00:00Z",
      "observacao": "Solicitação recebida"
    },
    {
      "status": "VALIDADO",
      "timestamp": "2023-10-01T14:00:30Z",
      "observacao": "Validação aprovada pela API de Fraudes"
    },
    {
      "status": "CANCELADA",
      "timestamp": "2023-10-01T14:05:00Z",
      "observacao": "Cancelamento solicitado pelo cliente"
    }
  ]
}
```

### Verificar Saúde da Aplicação

#### Requisição

```bash
curl -X GET http://localhost:8080/api/health
```

#### Resposta

```json
{
  "status": "UP",
  "timestamp": "2023-10-01T14:10:00Z",
  "service": "solicitacao-apolice-mvp",
  "version": "1.0.0"
}
```

### Verificar Saúde do RabbitMQ

#### Requisição

```bash
curl -X GET http://localhost:8080/api/health/rabbitmq
```

#### Resposta

```json
{
  "status": "UP",
  "message": "RabbitMQ conectado",
  "timestamp": "2023-10-01T14:10:05Z"
}
```

## RabbitMQ

### Publicar Evento de Pagamento Confirmado

Você pode publicar eventos diretamente no RabbitMQ usando a interface de gerenciamento (http://localhost:15672) ou via ferramentas como `rabbitmqadmin`.

#### Usando rabbitmqadmin

```bash
rabbitmqadmin publish exchange=solicitacao.exchange routing_key=pagamento.confirmado \
  payload='{"solicitacaoId":"89846cee-c6d5-4320-92e9-16e122d5c672","customerId":"adc56d77-348c-4bf0-908f-22d402ee715c","valor":75.25,"formaPagamento":"CREDIT_CARD","dataConfirmacao":"2023-10-01T14:01:30Z"}'
```

#### Formato do Evento

```json
{
  "solicitacaoId": "89846cee-c6d5-4320-92e9-16e122d5c672",
  "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "valor": 75.25,
  "formaPagamento": "CREDIT_CARD",
  "dataConfirmacao": "2023-10-01T14:01:30Z"
}
```

### Publicar Evento de Subscrição Autorizada

#### Usando rabbitmqadmin

```bash
rabbitmqadmin publish exchange=solicitacao.exchange routing_key=subscricao.autorizada \
  payload='{"solicitacaoId":"89846cee-c6d5-4320-92e9-16e122d5c672","customerId":"adc56d77-348c-4bf0-908f-22d402ee715c","autorizado":true,"dataAutorizacao":"2023-10-01T14:02:00Z"}'
```

#### Formato do Evento

```json
{
  "solicitacaoId": "89846cee-c6d5-4320-92e9-16e122d5c672",
  "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "autorizado": true,
  "dataAutorizacao": "2023-10-01T14:02:00Z"
}
```

### Publicar Evento de Subscrição Negada

#### Usando rabbitmqadmin

```bash
rabbitmqadmin publish exchange=solicitacao.exchange routing_key=subscricao.autorizada \
  payload='{"solicitacaoId":"89846cee-c6d5-4320-92e9-16e122d5c672","customerId":"adc56d77-348c-4bf0-908f-22d402ee715c","autorizado":false,"motivoRejeicao":"Risco elevado identificado pelo subscritor","dataAutorizacao":"2023-10-01T14:02:00Z"}'
```

#### Formato do Evento

```json
{
  "solicitacaoId": "89846cee-c6d5-4320-92e9-16e122d5c672",
  "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "autorizado": false,
  "motivoRejeicao": "Risco elevado identificado pelo subscritor",
  "dataAutorizacao": "2023-10-01T14:02:00Z"
}
```

## Métricas (Actuator)

### Listar Métricas Disponíveis

#### Requisição

```bash
curl -X GET http://localhost:8080/actuator/metrics
```

#### Resposta

```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "http.server.requests",
    "solicitacao.recebidas",
    "solicitacao.validadas",
    "solicitacao.rejeitadas",
    "solicitacao.aprovadas",
    "solicitacao.canceladas",
    "api.fraudes.tempo",
    "eventos.processamento.tempo",
    "api.fraudes.classificacao",
    "eventos.processados",
    "eventos.erros"
  ]
}
```

### Consultar Métrica Específica

#### Requisição

```bash
curl -X GET http://localhost:8080/actuator/metrics/solicitacao.recebidas
```

#### Resposta

```json
{
  "name": "solicitacao.recebidas",
  "description": "Total de solicitações de apólice recebidas",
  "baseUnit": null,
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 5.0
    }
  ],
  "availableTags": []
}
```

## Observações

- Os exemplos acima assumem que a aplicação está rodando localmente na porta 8080.
- Os UUIDs e timestamps são exemplos e serão diferentes em sua execução.
- Para usar o `rabbitmqadmin`, você precisa instalá-lo ou usar a interface web do RabbitMQ.
- As respostas podem variar dependendo do estado atual da aplicação e dos dados existentes.

