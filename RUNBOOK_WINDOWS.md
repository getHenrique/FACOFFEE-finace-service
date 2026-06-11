# RUNBOOK (Windows / PowerShell) — FACOFFEE Finance Service

Versão Windows do [RUNBOOK.md](RUNBOOK.md). Como subir o serviço do zero e resetar
o banco no **Windows 10/11** usando **PowerShell**. A ordem importa:
**infraestrutura primeiro, app depois**.

> Os **casos de teste por endpoint** (Entrada/Saída/erros) são idênticos aos do
> [RUNBOOK.md](RUNBOOK.md#casos-de-teste-pelo-swagger) — só mudam os comandos de
> shell. Veja os requisitos completos em [REQUISITOS.md](REQUISITOS.md).

## Visão geral

```
Docker Desktop (Keycloak :8080, RabbitMQ :5672)  ->  precisam estar UP antes do app
        |                    |
   emite JWT           entrega eventos
        v                    v
   App Spring Boot na porta 3003  (context-path /api/finance)
        |
   SQLite: src\main\resources\persistence\financePersistence.db
```

Pré-requisitos: **Docker Desktop**, **JDK 25+**, **Python 3** e **`curl.exe`**
(já vem no Windows 10/11). Veja [REQUISITOS.md](REQUISITOS.md).

---

## Passo a passo (PowerShell, na raiz do repositório)

### 1. Subir a infraestrutura (Docker Desktop)
O Docker Desktop precisa estar **rodando** (ícone na bandeja).
```powershell
cd facoffee-docs
docker compose up -d rabbitmq keycloak
cd ..
```
Espere ~15–30s e confira:
```powershell
docker ps --format "{{.Names}}`t{{.Status}}"
curl.exe -s -o NUL -w "keycloak %{http_code}`n" http://localhost:8080/realms/facoffee
```

### 2. (Opcional) Reset do banco de dados
```powershell
# com o app PARADO:
Remove-Item .\src\main\resources\persistence\financePersistence.db -ErrorAction SilentlyContinue
```
> Sempre resete com o app parado. Reset de DB **não** afeta usuários/roles do Keycloak.

### 3. Subir o serviço
Aponte o `JAVA_HOME` para a sua **JDK 25** e use o wrapper `.\mvnw.cmd`:
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.3+9"   # ajuste o caminho
.\mvnw.cmd spring-boot:run
```
Espere `Started FinanceServiceApplication` e verifique (em outra janela):
```powershell
curl.exe -s http://localhost:3003/api/finance/health
```
> **Porta 3003 ocupada?** Mate a instância antiga:
> ```powershell
> Get-NetTCPConnection -LocalPort 3003 -State Listen |
>   Select-Object -ExpandProperty OwningProcess |
>   ForEach-Object { Stop-Process -Id $_ -Force }
> ```

### 4. Criar dados de teste (pendência via RabbitMQ)
Pendências nascem de evento, não de REST:
```powershell
py -3 -m pip install pika           # só na primeira vez
py -3 facoffee-docs\scripts\publish_test_messages.py
```
> **Idempotente por `userId` + `cycle`:** rodar o script várias vezes **não** cria
> pendências novas (`id` continua `1`). Para gerar outro id, edite o payload do
> script com outro `userId`/`cycle`.

### 5. Pegar token e testar
Você precisa dos **dois** tokens (papéis diferentes acessam rotas diferentes):
```powershell
# MANAGER (envia: nada; valida proofs, despesas, relatórios)
$mgr = (curl.exe -s -X POST http://localhost:8080/realms/facoffee/protocol/openid-connect/token `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=password&client_id=facoffee-public&username=facoffee@facom.ufms.br&password=facoffee" `
  | ConvertFrom-Json).access_token

# PARTICIPANT (envia/remove comprovante)
$part = (curl.exe -s -X POST http://localhost:8080/realms/facoffee/protocol/openid-connect/token `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=password&client_id=facoffee-public&username=participant@facom.ufms.br&password=participant" `
  | ConvertFrom-Json).access_token

$mgr   # imprime o token MANAGER para colar no Swagger
```
Swagger: `http://localhost:3003/api/finance/swagger-ui.html` → **Authorize**
(cole só o token, sem `Bearer`) → testar os endpoints.

Exemplo de chamada autenticada via PowerShell:
```powershell
curl.exe -s -H "Authorization: Bearer $mgr" http://localhost:3003/api/finance/pendencies
```

---

## Bloco único — reset completo (copia e cola no PowerShell)

```powershell
# a partir da raiz do repositorio
Push-Location facoffee-docs; docker compose up -d rabbitmq keycloak; Pop-Location
Start-Sleep -Seconds 25
Get-NetTCPConnection -LocalPort 3003 -State Listen -ErrorAction SilentlyContinue |
  Select-Object -ExpandProperty OwningProcess | ForEach-Object { Stop-Process -Id $_ -Force }
Remove-Item .\src\main\resources\persistence\financePersistence.db -ErrorAction SilentlyContinue

# (3) sobe o app em segundo plano
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.3+9"   # ajuste
Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -RedirectStandardOutput "finance-app.log"
# espera o health virar 200
do { Start-Sleep -Seconds 2 } until (
  (try { (Invoke-WebRequest http://localhost:3003/api/finance/health -UseBasicParsing).StatusCode } catch { 0 }) -eq 200)
Write-Host "app UP"

# (4) cria a pendencia de teste
py -3 -m pip install pika
py -3 facoffee-docs\scripts\publish_test_messages.py

# (5) imprime o token MANAGER
(curl.exe -s -X POST http://localhost:8080/realms/facoffee/protocol/openid-connect/token `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=password&client_id=facoffee-public&username=facoffee@facom.ufms.br&password=facoffee" `
  | ConvertFrom-Json).access_token
```

---

## Usuários de teste (Keycloak)

| Papel | Usuário | Senha |
|-------|---------|-------|
| MANAGER | `facoffee@facom.ufms.br` | `facoffee` |
| PARTICIPANT | `participant@facom.ufms.br` | `participant` |

- Sobrevivem ao reboot (volume `keycloak_data`). Se rodar `docker compose down -v`,
  só o MANAGER é reimportado do `realm-facoffee.json` — recriar o PARTICIPANT
  conforme a seção correspondente do [RUNBOOK.md](RUNBOOK.md).

---

## Observações específicas do Windows

- Use **`.\mvnw.cmd`** (não `./mvnw`). Se o PowerShell reclamar de script não
  assinado, rode pelo `cmd` ou ajuste a *Execution Policy* da sessão:
  `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`.
- **`curl.exe`** (com `.exe`) é o cURL real do Windows; só `curl` é um alias do
  `Invoke-WebRequest` e tem sintaxe diferente — sempre use `curl.exe` aqui.
- Caminhos usam `\` (barra invertida). O `.db` fica em
  `src\main\resources\persistence\`.
- Python pode ser `py -3` (launcher do Windows) ou `python`, conforme a instalação.
- Tokens expiram (~10h) — ao receber `401`, gere outro (passo 5).
- **Não precisa de Lombok/flag especial:** o projeto compila com `.\mvnw.cmd`
  direto em qualquer **JDK 25+** (sem `-Dmaven.compiler.proc=full`).
