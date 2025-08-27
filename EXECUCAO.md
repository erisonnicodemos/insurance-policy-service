# Execução de Testes da API

Este arquivo contém exemplos de comandos CURL para testar os principais endpoints da aplicação, com explicações sobre cada um.

---

## Criar uma nova solicitação de apólice

**Endpoint:** `POST /api/solicitacoes`

**CURL:**
```bash
curl -X POST http://localhost:8080/api/solicitacoes \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "7361184d-0858-4cb3-a19c-21a2be7e7f2c",
    "category": "VIDA",
    "paymentMethod": "BOLETO",
    "productId": "123",
    "salesChannel": "ONLINE",
    "totalMonthlyPremiumAmount": 100.50,
    "assistances": ["ASSISTENCIA_1", "ASSISTENCIA_2"],
    "coverages": {"COBERTURA_1": 5000.00},
    "insuredAmount": 10000.00
  }'
```

**Explicação:**
Este comando cria uma nova solicitação de apólice de seguro, enviando todos os dados obrigatórios para o backend. Se a requisição for bem-sucedida, retorna um JSON com os dados da solicitação criada, incluindo o campo `id`.

**Exemplo de resposta:**
```json
{
  "id": "efdd9700-1929-4735-a9f2-650dfab7a774",
  "customerId": "7361184d-0858-4cb3-a19c-21a2be7e7f2c",
  "productId": "123",
  "category": "VIDA",
  "salesChannel": "ONLINE",
  "paymentMethod": "BOLETO",
  "status": "RECEBIDO",
  "createdAt": "2025-08-27T14:16:31.963163817",
  "finishedAt": null,
  "totalMonthlyPremiumAmount": 100.5,
  "insuredAmount": 10000,
  "coverages": {
    "COBERTURA_1": 5000
  },
  "assistances": [
    "ASSISTENCIA_1",
    "ASSISTENCIA_2"
  ],
  "history": [
    {
      "status": "RECEBIDO",
      "timestamp": "2025-08-27T14:16:31.963193621",
      "observacao": "Solicitação recebida"
    }
  ]
}
```
