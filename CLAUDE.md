# FACOFFEE Finance Service

Microsserviço financeiro do sistema **FACOFFEE**. Responsável por pendências de
cobrança, comprovantes de pagamento e despesas.

## Stack

- **Spring Boot 4.0.6** + **Maven** (Java 25, ver `pom.xml`)
- **SQLite** como banco (`src/main/resources/persistence/financePersistence.db`),
  dialeto `org.hibernate.community.dialect.SQLiteDialect`, `ddl-auto: update`
- **Keycloak** para autenticação JWT — realm `facoffee`, issuer
  `http://localhost:8080/realms/facoffee`. Roles: `MANAGER` e `PARTICIPANT`
  (mapeadas a partir de `realm_access.roles` como `ROLE_*` em `SecurityConfig`)
- **RabbitMQ** (`localhost:5672`) para mensageria assíncrona
- **Contrato (fonte da verdade):** `facoffee-docs/api-docs.yaml` (REST) e
  `facoffee-docs/async-docs.yaml` (eventos), feitos pelo professor — referência
  obrigatória segundo o `GUIA_EQUIPE_FINANCE.md`.

## Como rodar

```bash
JAVA_HOME=/home/gabriel/.jdks/openjdk-26 ./mvnw spring-boot:run
```

- Porta **3003**, context-path **`/api/finance`** → as rotas abaixo descritas como
  `/finance/...` ficam de fato em `/api/finance/...`.
- Sobe Keycloak (`:8080`) e RabbitMQ (`:5672`) antes; sem eles há warnings de
  conexão, mas o web server ainda inicia.
- **Armadilha comum:** se a porta 3003 já estiver ocupada (instância antiga da
  IDE não encerrada), a aplicação falha com `Port 3003 was already in use`.
  Encerre o processo antigo antes de rodar de novo.

## Estrutura

```
controllers/   FinanceController, PendenciasController, PaymentProofController,
               ErrorResponse, ApiExceptionHandler (@RestControllerAdvice p/ 403)
services/      PendencyService
entities/      Pendency, PaymentProof, Expense, *StatusChange + enums
repository/    PendencyRepository, PaymentProofRepository, *StatusChangeRepository
converters/    LocalDate/LocalDateTime <-> texto ISO (autoApply) p/ SQLite
messaging/     PendencyEventListener (+ DTOs) — consome de RabbitMQ
config/        SecurityConfig, RabbitMQConfig, OpenApiConfig
```

## Enums

- `PendencyStatus`: `PENDING`, `PAID`, `OVERDUE`
- `ProofStatus`: `WAITING_VALIDATION`, `VALIDATED`, `REJECTED`
- `PaymentMethod`: `PIX`, `CASH`, `BANK_TRANSFER`, `OTHER`

## Divisão de tarefas

Repositório: `getHenrique/FACOFFEE-finace-service` — **getHenrique** é dono do
repo / tech lead.

### Gabriel (branch `Gabriel`) — Comprovantes de pagamento ✅ implementado

Conforme `api-docs.yaml`. Rotas em `/finance/pendencies/{pendencyId}/proofs`
(= `/api/finance/...`). Implementado em `PaymentProofController`.

- **US-4 (#12)** `POST .../proofs` — **PARTICIPANT** envia comprovante
  (`submittedBy`, `amount`, `paymentDate`, `method`, `receiptUrl`, `note?`).
  Permitido enquanto a pendência não estiver `PAID` e não houver comprovante em
  `WAITING_VALIDATION`. Novo comprovante entra como `WAITING_VALIDATION` (201).
- **US-5 (#13)** `GET` / `PATCH` / `DELETE`:
  - `GET .../proofs` — qualquer autenticado; lista paginada (`totalElements`),
    filtro opcional por `status`.
  - `PATCH .../proofs/{proofId}` — **MANAGER**. **Validar** → comprovante
    `VALIDATED` (grava `validatedBy/At`) + pendência `PAID`. **Rejeitar** →
    `REJECTED` (grava `rejectedBy/At/rejectionReason`; `reason` obrigatório) +
    pendência volta a `PENDING`. Só decide se estiver em `WAITING_VALIDATION`
    (senão `409`).
  - `DELETE .../proofs/{proofId}` — **MANAGER ou dono** do comprovante; só se
    `WAITING_VALIDATION` (senão `409`); negado → `403`.
  - Auditoria em `ProofStatusChange` + campos de decisão no próprio `PaymentProof`.
- Erros no formato `ErrorResponse` (`timestamp/status/error/message/path`).
- ⚠️ O evento `finance.payment.confirmed` **não** é publicado: não existe no
  `async-docs.yaml` (escopo priorizado cobre só `UserDeactivated` e
  `FinancialPendencyCreated`).

### Vitor (branch `Vitor-Dias-matr`) — Despesas (cadastro) + listagem de pendências

- **US-6 (#14)** `POST /finance/expenses` — apenas `MANAGER` cadastra despesa
  (descrição, data, valor `> 0`).
- Também implementou o `PendenciasController` (`GET /finance/pendencies`).

### José Roland (branch `jose-roland`) — Despesas (consulta)

- **US-7 (#15)** `GET /finance/expenses`, `GET /finance/expenses/{id}` e
  `DELETE` — listar despesas ordenadas por data.

## Convenções

- **Mensagem de commit:** `Fix (módulo) - Descrição (#issue)`
- **Datas no SQLite:** o driver `sqlite-jdbc` grava `LocalDate`/`LocalDateTime`
  como epoch-millis e quebra na leitura (`ParseException` → 500). Resolvido por
  `AttributeConverter` com `autoApply = true` em `converters/` (vale p/ todas as
  entidades). `columnDefinition = "TEXT"` sozinho **não** resolve.

## Divergências corrigidas (auditoria do módulo de comprovantes vs `api-docs.yaml`)

Itens onde o código/docs divergiam do contrato do professor e foram ajustados:

1. **Roles não funcionavam** — `SecurityConfig` lia a claim `roles` (inexistente);
   o Keycloak usa `realm_access.roles`. Sem isso, todo `@PreAuthorize` dava `403`.
2. **Status do comprovante** — era `SUBMITTED`; contrato usa `WAITING_VALIDATION`.
3. **`PaymentProof` incompleto** — faltavam `userId`, `paymentDate`,
   `method` (`PaymentMethod`), `note` e auditoria (`validatedBy/At`,
   `rejectedBy/At`, `rejectionReason`); `attachmentFilePath` → `receiptUrl`.
4. **Paginação** — usava `totalItems`; contrato pede `totalElements`.
5. **Formato de erro** — retornava `{message}`; contrato exige `ErrorResponse`.
6. **Autorização por rota** — POST é só `PARTICIPANT`; GET é autenticado; DELETE é
   `MANAGER` **ou dono** (antes estava genérico/restritivo demais).
7. **Rejeição** — não revertia a pendência; agora volta para `PENDING`.
8. **Evento `finance.payment.confirmed`** — citado por engano; não está no
   `async-docs.yaml`, então não é publicado.
