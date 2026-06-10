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
```bash
# a partir da raiz do repositório
( cd facoffee-docs && docker compose up -d rabbitmq keycloak )
sleep 25
kill $(ss -ltnp 2>/dev/null | grep ':3003' | grep -oP 'pid=\K[0-9]+') 2>/dev/null
rm -f src/main/resources/persistence/financePersistence.db
chmod +x mvnw
JAVA_HOME=/home/gabriel/.jdks/openjdk-26 ./mvnw spring-boot:run
```

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
