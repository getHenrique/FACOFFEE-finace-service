# REQUISITOS — Configuração da máquina para rodar o Finance Service

Tudo o que precisa estar instalado/configurado para rodar o serviço corretamente,
em **Linux/macOS** ou **Windows**. Depois de cumprir estes requisitos, siga o
[RUNBOOK.md](RUNBOOK.md) (Linux/macOS) ou o [RUNBOOK_WINDOWS.md](RUNBOOK_WINDOWS.md).

---

## 1. Resumo (o que instalar)

| Ferramenta | Versão | Obrigatório? | Para quê |
|------------|--------|--------------|----------|
| **JDK** | **25+** (LTS) | ✅ sim | compilar e rodar o app |
| **Docker** + **Docker Compose** | recente | ✅ sim | subir Keycloak e RabbitMQ |
| **Python** | **3.8+** | ✅ sim (dados de teste) | publicar eventos no RabbitMQ |
| **Maven** | — | ❌ não | usa-se o wrapper `./mvnw` (baixa sozinho 3.9.16) |
| **Git** | recente | ✅ sim | clonar o repositório |
| **curl** | — | recomendado | pegar token / testar via terminal |
| **jq** | — | opcional (Linux/macOS) | extrair o `access_token` |

> **Importante:** o projeto **não usa Lombok** — compila e roda com `./mvnw`
> direto em **qualquer JDK 25+**, sem `-Dmaven.compiler.proc=full` e sem precisar
> de IDE para processar anotações.

---

## 2. JDK 25 (obrigatório)

O projeto tem `java.version = 25` no `pom.xml`. Use **JDK 25 ou superior**.
Recomendado: **Eclipse Temurin / Adoptium 25 (LTS)**.

- Download: <https://adoptium.net/temurin/releases/?version=25>
- Ambiente de referência testado: **Temurin 25.0.3+9**.

**Configurar `JAVA_HOME`:**

- Linux/macOS:
  ```bash
  export JAVA_HOME=/caminho/para/jdk-25      # ex.: /home/<user>/.jdks/jdk-25.0.3+9
  java -version                              # deve mostrar "25.x"
  ```
- Windows (PowerShell):
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.3+9"
  java -version
  ```

> Não é necessário instalar o Maven: o wrapper `./mvnw` (Linux/macOS) / `.\mvnw.cmd`
> (Windows) baixa o Maven 3.9.16 automaticamente na primeira execução.

---

## 3. Docker + Docker Compose (obrigatório)

Usado para subir **Keycloak** (`:8080`) e **RabbitMQ** (`:5672`), definidos em
`facoffee-docs/docker-compose.yml`.

- **Linux:** Docker Engine + plugin `docker compose`.
- **Windows/macOS:** **Docker Desktop** (precisa estar **rodando** antes de subir o app).

Imagens usadas (baixadas automaticamente):
- `quay.io/keycloak/keycloak:25.0`
- imagem do RabbitMQ (ver compose)

Verificar:
```bash
docker --version
docker compose version
docker ps
```

---

## 4. Python 3 + pika (obrigatório para dados de teste)

As pendências nascem de **eventos RabbitMQ** — o script
`facoffee-docs/scripts/publish_test_messages.py` publica os eventos de teste.

```bash
# Linux/macOS
python3 -m pip install pika
# Windows
py -3 -m pip install pika
```

> Sem isso, a app sobe e os endpoints funcionam, mas **não haverá pendência** para
> testar os comprovantes (dará `404`). Despesas e relatórios não dependem disso.

---

## 5. Portas que precisam estar livres

| Porta | Serviço | Observação |
|-------|---------|------------|
| **3003** | App Finance | context-path `/api/finance` |
| **8080** | Keycloak | emissão de JWT |
| **5672** | RabbitMQ (AMQP) | entrega de eventos |
| **15672** | RabbitMQ Management | opcional (UI web), login `facoffee`/`facoffee` |

Se a **3003** estiver ocupada, o app falha com `Port 3003 was already in use` —
veja como matar o processo no RUNBOOK do seu SO.

---

## 6. Recursos mínimos sugeridos

- **RAM:** ~4 GB livres (JVM + 2 containers Docker).
- **Disco:** ~2 GB (imagens Docker + dependências Maven em `~/.m2`).
- **Rede:** acesso à internet na primeira execução (baixar Maven, dependências e
  imagens Docker).

---

## 7. Checklist rápido de verificação

```bash
java -version            # 25.x
docker ps                # daemon respondendo
docker compose version   # plugin presente
python3 --version        # 3.8+   (Windows: py -3 --version)
git --version
# dentro do repo:
./mvnw -v                # baixa/mostra Maven 3.9.16 (Windows: .\mvnw.cmd -v)
```

Cumprido tudo isso, vá para o RUNBOOK do seu sistema operacional e siga o
passo a passo.

---

## 8. Stack e versões do projeto (referência)

- **Spring Boot:** 4.0.6
- **Java:** 25
- **Maven (wrapper):** 3.9.16
- **Banco:** SQLite (`sqlite-jdbc`), arquivo local, `ddl-auto=update`
- **Auth:** Keycloak (realm `facoffee`, roles `MANAGER` / `PARTICIPANT`)
- **Mensageria:** RabbitMQ
- **Datas no SQLite:** persistidas como texto ISO via `AttributeConverter`
  (`autoApply`) — ver `converters/`.
