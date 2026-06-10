# RUNBOOK — FACOFFEE Finance Service

Como subir o serviço do zero (inclusive após reiniciar o computador) e como
resetar o banco. A ordem importa: **infraestrutura primeiro, app depois**.

## Visão geral

```
Docker (Keycloak :8080, RabbitMQ :5672)  ->  precisam estar UP antes do app
        |                    |
   emite JWT           entrega eventos
        v                    v
   App Spring Boot na porta 3003  (context-path /api/finance)
        |
   SQLite: src/main/resources/persistence/financePersistence.db
   (gitignored; schema recriado no startup via ddl-auto=update)
```

Pré-requisitos: Docker + Docker Compose, um JDK 25+ e Python 3 (para o script de
eventos). Defina `JAVA_HOME` para o seu JDK — neste ambiente é
`/home/gabriel/.jdks/openjdk-26`.

---

## Passo a passo (após reiniciar o PC)

### 1. Subir a infraestrutura (Docker)
Os containers não voltam sozinhos após o reboot:
```bash
cd facoffee-docs
docker compose up -d rabbitmq keycloak
```
Espere ~15–30s e confira:
```bash
docker ps --format '{{.Names}}\t{{.Status}}'        # rabbitmq e keycloak "Up"
curl -s -o /dev/null -w "keycloak %{http_code}\n" http://localhost:8080/realms/facoffee
```

### 2. (Opcional) Reset do banco de dados
O `.db` é local e o schema é recriado no startup. Para zerar pendências/comprovantes:
```bash
# com o app PARADO:
rm -f src/main/resources/persistence/financePersistence.db
```
> Sempre resete com o app parado. Faça isso também ao mudar mapeamento de entidade.
> Reset de DB não afeta usuários/roles do Keycloak.

### 3. Subir o serviço
```bash
chmod +x mvnw     # uma vez — o bit de execução às vezes se perde
JAVA_HOME=/home/gabriel/.jdks/openjdk-26 ./mvnw spring-boot:run
```
Espere `Started FinanceServiceApplication` e verifique:
```bash
curl -s http://localhost:3003/api/finance/health      # {"status":"UP",...}
```
> Se der `Port 3003 was already in use`, mate a instância antiga:
> `kill $(ss -ltnp | grep ':3003' | grep -oP 'pid=\K[0-9]+')`

### 4. Criar dados de teste (pendência via RabbitMQ)
Pendências nascem de evento, não de REST:
```bash
pip install pika   # só na primeira vez
python3 facoffee-docs/scripts/publish_test_messages.py
```
> **Idempotente por `userId` + `cycle`:** rodar o script várias vezes **não** cria
> pendências novas (`id` continua `1`). A app pula duplicatas de mesmo
> `userId`/`cycle` (no script, `usr_123` / `2026-05`). Para gerar `id: "2"`, `3`…
> edite o payload do script com outro `userId` ou `cycle`.

### 5. Pegar token e testar
```bash
# MANAGER:
curl -s -X POST http://localhost:8080/realms/facoffee/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=facoffee-public&username=facoffee@facom.ufms.br&password=facoffee" \
  | jq -r .access_token
```
Swagger: `http://localhost:3003/api/finance/swagger-ui.html` -> **Authorize**
(cole só o token, sem `Bearer`) -> testar os endpoints.

---

## Bloco único — reset completo (copia e cola)

Faz tudo: infra (1) → reset do DB (2) → sobe o app (3) → **cria a pendência de
teste (4)** → **imprime o token MANAGER (5)**. O app sobe em segundo plano e o
script espera o `health` responder antes de publicar o evento — assim os passos 4
e 5 não rodam antes da hora (era o que faltava: sem o passo 4 o banco fica vazio
e os testes dão `404`).

```bash
# a partir da raiz do repositório — copia e cola tudo de uma vez
( cd facoffee-docs && docker compose up -d rabbitmq keycloak )
sleep 25
kill $(ss -ltnp 2>/dev/null | grep ':3003' | grep -oP 'pid=\K[0-9]+') 2>/dev/null
rm -f src/main/resources/persistence/financePersistence.db
chmod +x mvnw

# (3) sobe o app em segundo plano e espera o health virar 200
JAVA_HOME=/home/gabriel/.jdks/openjdk-26 ./mvnw spring-boot:run > /tmp/finance-app.log 2>&1 &
until [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:3003/api/finance/health)" = "200" ]; do sleep 2; done
echo "app UP"

# (4) cria a pendência de teste via RabbitMQ (pendências nascem de evento, não de REST)
pip install pika   # só na primeira vez
python3 facoffee-docs/scripts/publish_test_messages.py

# (5) imprime o token MANAGER — copie e cole no Swagger (Authorize, sem "Bearer")
curl -s -X POST http://localhost:8080/realms/facoffee/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=facoffee-public&username=facoffee@facom.ufms.br&password=facoffee" \
  | jq -r .access_token
```

> O app fica rodando em segundo plano (log em `/tmp/finance-app.log`). Para
> pará-lo depois: `kill $(ss -ltnp 2>/dev/null | grep ':3003' | grep -oP 'pid=\K[0-9]+')`.

---

## Casos de teste pelo Swagger

Cada endpoint traz **Entrada** (corpo a enviar) e **Saída esperada** (status +
corpo), além de uma tabela de **erros possíveis**. Sempre os dois cenários de
auth: **autenticado** (clique *Authorize* e cole o token) e **não autenticado**
(sem token). Caminhos relativos ao context-path `/api/finance`.

> **Antes de começar:** confirme que existe pendência em `GET /pendencies`. Se
> vier vazio (`totalItems: 0`) — banco recém-resetado sem evento publicado — os
> testes de comprovante darão `404`. Rode o **passo 4** (`publish_test_messages.py`)
> para criar a pendência.

**Ordem recomendada** (o estado do comprovante depende dos passos anteriores):

1. `GET /health` — app no ar.
2. `GET /pendencies` — confirma a pendência e pega o **id real** (ex.: `1`).
3. `GET /pendencies/{id}` — detalhe da pendência (status `PENDING`).
4. `POST /pendencies/{id}/proofs` — envia comprovante; nasce `WAITING_VALIDATION`.
5. `GET /pendencies/{id}/proofs` — confirma o comprovante e pega o **proofId**.
6. `DELETE /pendencies/{id}/proofs/{proofId}` — *(opcional)* remove enquanto está
   `WAITING_VALIDATION`. Se remover, repita o passo 4 para ter um comprovante de novo.
7. `PATCH /pendencies/{id}/proofs/{proofId}` — **deixe por último**: validar marca a
   pendência como `PAID` e bloqueia novos `POST`. Para recomeçar, faça o reset
   (bloco único acima).

> **Sobre o `id` da pendência:** a pendência **não** nasce com o id do evento
> (`pend_001`). O listener gera o id automaticamente — com o banco recém-resetado
> e um único evento publicado, ela fica com **`id: "1"`**. Confirme o id real em
> `GET /pendencies` antes dos testes e troque `1` pelo que aparecer. O `id` do
> comprovante também é gerado (idem, começa em `1`).
>
> **Formato de erro:** respostas `4xx` de negócio (`400/403/404/409`) vêm como
> `ErrorResponse` → `{ "timestamp", "status", "error", "message", "path" }`. Já o
> `401` é tratado pelo Spring Security e vem **sem corpo**.

### 1. `GET /health`  · público

- **Entrada:** nenhuma.
- **Saída esperada (autenticado ou não):** `200 OK`
  ```json
  { "status": "UP", "service": "FACOFFEE-Finance", "message": "I'm alive!" }
  ```
- **Erros possíveis:** nenhum (rota livre).

### 2. `GET /pendencies`  · autenticado (qualquer role)

- **Entrada:** nenhuma. Filtros opcionais via query: `userId`, `cycle`,
  `status` (`PENDING`/`PAID`/`OVERDUE`), `page`, `size`.
- **Saída esperada (autenticado):** `200 OK`
  ```json
  {
    "items": [
      { "id": "1", "source": "MONTHLY_PARTICIPATION", "sourceId": "mpart_001",
        "userId": "usr_123", "cycle": "2026-05", "amount": 40.0, "status": "PENDING" }
    ],
    "page": { "page": 0, "size": 20, "totalItems": 1, "totalPages": 1 }
  }
  ```

| Cenário | Saída |
|---------|-------|
| Sem token | `401` (sem corpo) |
| Filtro sem resultado (ex.: `?status=PAID`) | `200`, `items: []`, `totalItems: 0` |

### 3. `GET /pendencies/1`  · autenticado

- **Entrada:** nenhuma.
- **Saída esperada (autenticado):** `200 OK`
  ```json
  { "id": "1", "source": "MONTHLY_PARTICIPATION", "sourceId": "mpart_001",
    "userId": "usr_123", "cycle": "2026-05", "amount": 40.0, "status": "PENDING" }
  ```

| Cenário | Saída |
|---------|-------|
| Sem token | `401` (sem corpo) |
| `id` inexistente (`GET /pendencies/999`) | `404` (sem corpo) |

### 4. `POST /pendencies/1/proofs`  · **só PARTICIPANT**

- **Entrada:**
  ```json
  {
    "submittedBy": "participant@facom.ufms.br",
    "amount": 40.0,
    "paymentDate": "2026-06-10",
    "method": "PIX",
    "receiptUrl": "https://exemplo.com/comprovantes/1.png",
    "note": "Pagamento referente ao ciclo 2026-05"
  }
  ```
  `method` ∈ `PIX`, `CASH`, `BANK_TRANSFER`, `OTHER`. `note` é opcional.
- **Saída esperada (token PARTICIPANT):** `201 Created`
  ```json
  { "id": "1", "pendencyId": "1", "userId": "participant@facom.ufms.br",
    "amount": 40.0, "paymentDate": "2026-06-10", "method": "PIX",
    "status": "WAITING_VALIDATION", "submittedAt": "...", "validatedAt": null }
  ```

| Cenário | Saída |
|---------|-------|
| Sem token | `401` (sem corpo) |
| Token **MANAGER** (rota é só PARTICIPANT) | `403` `ErrorResponse` |
| Pendência inexistente (`/pendencies/999/proofs`) | `404` `ErrorResponse` |
| Falta campo obrigatório (`submittedBy`/`amount`/`paymentDate`/`method`/`receiptUrl`) | `400` `ErrorResponse` |
| Já existe comprovante em `WAITING_VALIDATION`, ou pendência já `PAID` | `409` `ErrorResponse` |

### 5. `GET /pendencies/1/proofs`  · autenticado

- **Entrada:** nenhuma. Filtro opcional: `?status=WAITING_VALIDATION` (ou
  `VALIDATED`/`REJECTED`), `page`, `size`.
- **Saída esperada (autenticado):** `200 OK` — note `totalElements` (não `totalItems`):
  ```json
  {
    "items": [
      { "id": "1", "pendencyId": "1", "userId": "participant@facom.ufms.br",
        "amount": 40.0, "method": "PIX", "status": "WAITING_VALIDATION" }
    ],
    "page": { "page": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
  }
  ```

| Cenário | Saída |
|---------|-------|
| Sem token | `401` (sem corpo) |
| Pendência inexistente | `404` `ErrorResponse` |
| Filtro sem resultado (ex.: `?status=VALIDATED` antes de validar) | `200`, `items: []`, `totalElements: 0` |

### 6. `PATCH /pendencies/1/proofs/1`  · **só MANAGER**

Use o `id` do comprovante (passo 4). Só decide se ele estiver em
`WAITING_VALIDATION`.

- **Entrada — validar:**
  ```json
  { "status": "VALIDATED", "decidedBy": "facoffee@facom.ufms.br" }
  ```
- **Entrada — rejeitar** (`reason` obrigatório):
  ```json
  { "status": "REJECTED", "decidedBy": "facoffee@facom.ufms.br", "reason": "Comprovante ilegível" }
  ```
- **Saída esperada (token MANAGER, validar):** `200 OK` — comprovante vira
  `VALIDATED` e a **pendência vira `PAID`**:
  ```json
  { "id": "1", "status": "VALIDATED", "validatedBy": "facoffee@facom.ufms.br", "validatedAt": "..." }
  ```
  Ao rejeitar, o comprovante vira `REJECTED` e a **pendência volta a `PENDING`**.

| Cenário | Saída |
|---------|-------|
| Sem token | `401` (sem corpo) |
| Token PARTICIPANT | `403` `ErrorResponse` |
| Pendência ou comprovante inexistente | `404` `ErrorResponse` |
| Comprovante não está em `WAITING_VALIDATION` (já decidido) | `409` `ErrorResponse` |
| Falta `decidedBy` | `400` `ErrorResponse` |
| `status` ≠ `VALIDATED`/`REJECTED` | `400` `ErrorResponse` |
| `REJECTED` sem `reason` | `400` `ErrorResponse` |

### 7. `DELETE /pendencies/1/proofs/1`  · **MANAGER ou dono**

Só remove enquanto estiver em `WAITING_VALIDATION`.

- **Entrada:** nenhuma.
- **Saída esperada (MANAGER ou dono do comprovante):** `204 No Content` (sem corpo).

| Cenário | Saída |
|---------|-------|
| Sem token | `401` (sem corpo) |
| PARTICIPANT que **não** é o dono | `403` `ErrorResponse` |
| Pendência ou comprovante inexistente | `404` `ErrorResponse` |
| Comprovante já decidido (`VALIDATED`/`REJECTED`) | `409` `ErrorResponse` |

---

## Usuários de teste (Keycloak)

| Papel | Usuário | Senha |
|-------|---------|-------|
| MANAGER | `facoffee@facom.ufms.br` | `facoffee` |
| PARTICIPANT | `participant@facom.ufms.br` | `participant` |

- Os usuários ficam no volume `keycloak_data` e **sobrevivem ao reboot**.
- Se rodar `docker compose down -v` (apaga volumes), o realm é reimportado do
  `facoffee-docs/keycloak/realm-facoffee.json` e **só o MANAGER volta** — o
  PARTICIPANT de teste precisa ser recriado.
- Admin master do Keycloak: `facoffee` / `facoffee` (realm `master`, client
  `admin-cli`) — usado para criar usuários/atribuir roles via Admin API.

### Recriar o usuário PARTICIPANT (se o volume for apagado)
```bash
KC=http://localhost:8080
ADM=$(curl -s -X POST $KC/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=admin-cli&username=facoffee&password=facoffee" | jq -r .access_token)
curl -s -X POST $KC/admin/realms/facoffee/users -H "Authorization: Bearer $ADM" -H 'Content-Type: application/json' \
  -d '{"username":"participant@facom.ufms.br","email":"participant@facom.ufms.br","firstName":"Part","lastName":"Icipante","enabled":true,"emailVerified":true,"credentials":[{"type":"password","value":"participant","temporary":false}]}'
KUID=$(curl -s "$KC/admin/realms/facoffee/users?username=participant@facom.ufms.br" -H "Authorization: Bearer $ADM" | jq -r '.[0].id')
ROLE=$(curl -s "$KC/admin/realms/facoffee/roles/PARTICIPANT" -H "Authorization: Bearer $ADM" | jq -c '{id,name}')
curl -s -X POST "$KC/admin/realms/facoffee/users/$KUID/role-mappings/realm" -H "Authorization: Bearer $ADM" -H 'Content-Type: application/json' -d "[$ROLE]"
```

---

## Observações

- O app **compila e inicia** sem Docker, mas sem Keycloak/RabbitMQ as rotas
  protegidas e a criação de pendências não funcionam.
- Tokens expiram (~10h) — ao receber `401`, gere outro (passo 5).
- Sem JDK no PATH você verá `JAVA_HOME is not defined` / `java: command not found`;
  por isso sempre exporte o `JAVA_HOME` ao rodar o `./mvnw`.
