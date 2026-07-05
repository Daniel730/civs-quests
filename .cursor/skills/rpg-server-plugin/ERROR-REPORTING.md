# Error Reporting — servidor → GitHub → Cursor

> Relacionado: [SKILL.md](SKILL.md) · [DEPLOY.md](DEPLOY.md) · [GITHUB-SYNC.md](GITHUB-SYNC.md)

## O que é possível hoje vs o que não é

### ✅ Possível (MVP implementado)

| Capacidade | Status |
|------------|--------|
| Capturar exceções Paper (`ServerExceptionEvent`) | ✅ |
| Capturar threads não tratadas (`UncaughtExceptionHandler`) | ✅ |
| Capturar ERROR+ com stacktrace via Log4j2 (Civs, RPG, outros plugins) | ✅ |
| Dedupe por fingerprint (default 15 min) | ✅ |
| Redação de tokens/senhas no stacktrace | ✅ |
| Gravar report Markdown local para Cursor ler | ✅ |
| `/rpg errors` para listar reports recentes | ✅ |
| Criar **GitHub Issue** com label `server-error` | ✅ |
| POST genérico para **webhook** (Zapier/Make → email) | ✅ |
| Cursor lê issue e corrige com `"fix issue #N"` | ✅ manual |
| Notificação por email via GitHub (watch repo/issues) | ✅ nativo GitHub |

### ❌ Não possível sem glue extra

| Capacidade | Por quê |
|------------|---------|
| Cursor receber webhook inbound nativo | Não existe API “envie stacktrace → acorda agente” no Cursor Desktop |
| Responder email → agente inicia sozinho | Precisa Cursor Cloud Agents API, GitHub Action, Zapier, ou Cursor Automation |
| Email com análise AI automática | Step 2 — Cloud Agent ou sessão  manual no Cursor |
| Unit tests gerados e executados automaticamente | Step 2 — após fix no repo |

## Arquitetura escolhida (MVP)

Módulo **`dev.daniel730.rpgserver.error`** dentro do plugin RPG (não JAR separado).

```
Servidor Paper
  ├─ ServerExceptionEvent ──────┐
  ├─ UncaughtExceptionHandler ──┤
  └─ Log4j2 ERROR+ appender ────┼→ ErrorReportService (async)
                                 │     ├─ dedupe (SHA-256 fingerprint)
                                 │     ├─ redact secrets
                                 │     ├─ LocalFileErrorReporter → plugins/RPGServer/error-reports/latest.md
                                 │     ├─ GitHubIssuesReporter → issue #N
                                 │     └─ WebhookReporter (opcional)
                                 │
GitHub (Daniel730/civs-quests)   │
  issue label: server-error      │
                                 ▼
Cursor (manual ou Automation)    "fix issue #42"
  → analisa stacktrace           → commit/PR
  → usuário valida in-game
```

### Ranking de opções (praticidade)

1. **GitHub Issues** ⭐ — melhor para Cursor; agente lê issues via `gh`/API; email nativo do GitHub.
2. **Webhook → Zapier/Make → email** — bom para alertas; loop AI ainda manual.
3. **Cursor Automation** (poll issues `server-error`) — semi-automático; requer plano/config.
4. **Cursor Cloud Agents API** — loop email completo; mais complexo, custo cloud.
5. **SMTP direto no plugin** — omitido no MVP; webhook + GitHub cobrem alertas.

## Configuração

Edite **`plugins/RPGServer/config.yml`** no servidor (nunca commite o PAT):

```yaml
error-reporting:
  enabled: true
  server-name: bot-server
  dedupe-minutes: 15
  include-plugins: []          # ou [Civs, RPGServer]
  sources:
    paper-server-exception: true
    uncaught-threads: true
    log4j-errors: true
  github:
    enabled: true
    token: "ghp_xxxxxxxxxxxx"   # PAT com escopo repo (issues:write)
    owner: Daniel730
    repo: civs-quests
    labels:
      - server-error
  local:
    enabled: true
    directory: "error-reports" # plugins/RPGServer/error-reports/
    keep-last: 50
  webhook:
    enabled: false
    url: "https://hooks.zapier.com/..."
```

### GitHub PAT

**Produção (recomendado):** ficheiro separado, não inline no config.

```bash
wsl bash /mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/scripts/wsl-configure-error-reporting.sh
```

Cria `/home/daniel/mineserver/secrets/github-pat.txt` (mode 600) a partir do `gh auth token` local e ativa `error-reporting` no `config.yml`.

Config no servidor:

```yaml
error-reporting:
  github:
    enabled: true
    token: ""
    token-file: "/home/daniel/mineserver/secrets/github-pat.txt"
```

**Manual (alternativa):**

1. GitHub → Settings → Developer settings → Fine-grained token (ou classic).
2. Repositório: `Daniel730/civs-quests` (ou outro).
3. Permissões: **Issues: Read and write**.
4. Cole em `error-reporting.github.token` no servidor **ou** no ficheiro `token-file`.
5. `/rpg reload` ou reinicie o servidor.

### Webhook (opcional — email via Zapier)

1. Crie Zap: Webhook Catch → Email (ou Slack).
2. Cole URL em `error-reporting.webhook.url`.
3. Payload JSON: `event`, `server`, `plugin`, `message`, `stackTrace`, `fingerprint`.

### Verificar

1. Provoque um erro de teste (dev only) ou espere erro real.
2. Confira log: `[error-reporting] GitHub issue criada...`
3. Veja issue em https://github.com/Daniel730/civs-quests/issues

## Workflow Cursor

### Manual (recomendado agora)

1. Servidor lança exceção → `plugins/RPGServer/error-reports/latest.md` com stacktrace + contexto.
2. No servidor/console: `/rpg errors` mostra os últimos reports e o caminho do `latest.md`.
3. Abra Cursor no repo e diga: **"Leia plugins/RPGServer/error-reports/latest.md e corrija a causa raiz."**
4. Agent lê report, propõe fix, roda `mvn compile`.
5. Se GitHub estiver configurado, a issue `#N` também pode ser usada: **"Analisa e corrige o issue #N do civs-quests."**

### Semi-automático (step 2)

- **Cursor Automation**: trigger “new GitHub issue with label `server-error`” → prompt fix template.
- **GitHub Action**: on `issues.opened` → chamar Cursor Cloud Agents API com body da issue.

### Loop email completo (step 3)

```
Issue criada
  → Cloud Agent analisa + comenta (causa, impacto, fix proposto)
  → Email GitHub notifica usuário
  → Usuário responde "aplica fix" no Cursor OU aprova PR
```

Resposta de email **não** acorda Cursor Desktop sozinha — precisa webhook para Cloud Agent ou ação manual.

## Arquivos do módulo

```
src/main/java/dev/daniel730/rpgserver/error/
├── ErrorReportingConfig.java
├── ErrorReportingManager.java
├── ErrorReport.java
├── ErrorReportService.java
├── ErrorDeduplicator.java
├── StackTraceUtil.java
├── GitHubIssuesReporter.java
├── LocalFileErrorReporter.java
├── WebhookReporter.java
├── hook/
│   ├── Log4jErrorCapture.java
│   └── UncaughtExceptionBridge.java
└── listener/
    └── ServerExceptionListener.java
```

Config default: `src/main/resources/config.yml` → secção `error-reporting:` (desabilitado; `local.enabled: true` para quando ligar).

## Segurança

- Tokens só no `config.yml` do **servidor**, nunca no git.
- Placeholders `REPLACE_WITH_GITHUB_PAT` ignorados até substituir.
- Stacktraces redigem `ghp_*`, `Bearer`, `password=`, etc.
- Paths de arquivo são mantidos (úteis para debug).

## Próximos passos

1. Configurar PAT no `bot-server` e `enabled: true`.
2. Ativar GitHub watch no repo para email.
3. Testar com erro controlado em staging.
4. (Opcional) Cursor Automation em issues `server-error`.
5. (Opcional) Cloud Agent + webhook para análise automática comentada na issue.
6. (Opcional) SMTP/JavaMail se quiser email sem Zapier.

## Deploy

Seguir [DEPLOY.md](DEPLOY.md). **Não deployar** com token placeholder — só após configurar secrets no servidor.
