# Monitorização de erros RPG — Cursor Automation

> Guia para polling periódico de `/rpg errors` + análise automática no Cursor.  
> Relacionado: [ERROR-REPORTING.md](../.cursor/skills/rpg-server-plugin/ERROR-REPORTING.md) · [DEPLOY.md](../.cursor/skills/rpg-server-plugin/DEPLOY.md)

## Visão geral

```
┌─────────────────┐     SSH/tmux      ┌──────────────────┐
│  Windows + WSL  │ ────────────────► │  daniel@bot-server│
│  script local   │   /rpg errors     │  mineserver/tmux  │
└────────┬────────┘   scp reports    └────────┬─────────┘
         │                                       │
         ▼                                       ▼
  .error-snapshots/                    plugins/RPGServer/
  (summary.md, latest.md)              error-reports/latest.md
         │
         ▼
┌─────────────────┐
│ Cursor Automation│  cron (ex. 6h) → agente lê snapshot → análise + sugestão
└─────────────────┘
```

O **Cursor Automation não tem SSH nativo**. O fluxo prático é:

1. **Script WSL** (`scripts/wsl-fetch-rpg-errors.sh`) liga ao servidor e copia reports para o workspace.
2. **Cursor Automation** (agendada) instrui o agente a correr o script (ou ler snapshot já atualizado) e analisar.

Alternativa sem SSH: o plugin já pode criar **GitHub Issues** (`label: server-error`) — a automação pode usar `gh` em vez do script.

---

## Pré-requisitos

| Item | Verificação |
|------|-------------|
| SSH WSL → bot-server | `wsl ssh -o BatchMode=yes daniel@bot-server echo ok` |
| tmux Minecraft | `wsl ssh daniel@bot-server "tmux -L mc list-sessions"` → `minecraft` |
| Error reporting ativo | `plugins/RPGServer/config.yml` → `error-reporting.enabled: true` |
| PAT GitHub (servidor) | `scripts/wsl-configure-error-reporting.sh` |
| Repo local | `C:\Users\Danie\Downloads\Civs-1.11.6\rpg-server-plugin` |

---

## Passo 1 — Scripts (já no repo)

### Busca manual (teste)

```powershell
wsl bash /mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/scripts/wsl-fetch-rpg-errors.sh
```

Saída em `.error-snapshots/` (gitignored):

| Ficheiro | Conteúdo |
|----------|----------|
| `summary.md` | Resumo + corpo de `latest.md` + tail de `latest.log` |
| `summary.json` | Metadados + preview para tooling |
| `latest.md` | Cópia do report mais recente |
| `reports/*.md` | Até 5 reports timestamped |

### Só no servidor (debug SSH)

```bash
ssh daniel@bot-server 'bash -s -- --markdown' < scripts/fetch-error-reports.sh
```

Opções: `--json`, `--markdown`, `--no-command` (não envia `/rpg errors`).

---

## Passo 2 — Atualizar snapshot antes da automação (opcional)

Para máquinas onde o **Cloud Agent não tem WSL/SSH**, agende o script no Windows:

**Task Scheduler (a cada 6 horas):**

1. Ação: `wsl.exe`
2. Argumentos: `bash /mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/scripts/wsl-fetch-rpg-errors.sh`
3. Executar mesmo que utilizador não tenha sessão iniciada (se aplicável)

Assim, quando o Cursor Automation correr, `.error-snapshots/` já está fresco.

---

## Passo 3 — Criar Cursor Automation na UI

### 3.1 Abrir editor

1. Cursor → **Automations** (ou pedir ao agente: *"abre a automação de monitor de erros"*).
2. **New automation**.

### 3.2 Trigger — agenda

| Preset | Cron | Uso |
|--------|------|-----|
| A cada 6 horas | `0 */6 * * *` | Recomendado |
| Diário 09:00 | `0 9 * * *` | Menos ruído |
| Semanal (segunda 09:00) | `0 9 * * 1` | Só revisão |

O fuso é o do relógio do Cursor/cron — ajuste a hora se necessário.

### 3.3 Repositório

- **Repo:** `Daniel730/civs-quests` (contém rpg-server-plugin)
- **Branch:** `master` (ou branch de dev)

### 3.4 Ferramentas (tools)

| Ferramenta | Recomendação |
|------------|--------------|
| Terminal / shell | **Sim** — para `wsl bash scripts/wsl-fetch-rpg-errors.sh` |
| GitHub (`gh`) | **Opcional** — listar/criar issues `server-error` |
| MCP GitHub | Opcional se configurado no dashboard Cursor |
| Deploy / restart | **Não** |

### 3.5 Instruções (prompt)

Copie o bloco de `automation/rpg-error-monitor-prefill.json` → campo **Instructions**, ou use o JSON de prefill ao importar.

Resumo do comportamento esperado:

1. Correr `wsl bash scripts/wsl-fetch-rpg-errors.sh`
2. Ler `.error-snapshots/summary.md`
3. Se vazio → "Nenhum erro novo"
4. Se erro → causa raiz, fix, `mvn compile` sugerido
5. Opcional: `gh issue comment` — **nunca** deploy automático

### 3.6 Importar prefill JSON

Ficheiro: `automation/rpg-error-monitor-prefill.json`

No Agents Window, o agente pode abrir o editor com este draft via integração Glass. Ajuste cron e tools antes de guardar.

---

## Passo 4 — Validar

1. `wsl bash scripts/wsl-fetch-rpg-errors.sh` — deve criar `.error-snapshots/`
2. No Cursor chat: *"Leia .error-snapshots/summary.md e diga se há erros"*
3. Dispare a automação manualmente (Run once) e confira o run log
4. (Opcional) Provocar erro de teste em staging e repetir

---

## O que a automação **PODE** fazer

| Ação | Sim |
|------|-----|
| SSH via script WSL local | Sim (quando o agente tem terminal + WSL) |
| Enviar `/rpg errors` via tmux | Sim (dentro do script remoto) |
| Ler `latest.md` e logs | Sim |
| Analisar stacktrace e sugerir fix | Sim |
| Correr `mvn compile` localmente | Sim (se pedido no prompt) |
| Comentar/criar GitHub issue | Sim, se `gh` autenticado |
| Usar issues `server-error` já criadas pelo plugin | Sim (melhor para Cloud Agent) |

## O que **NÃO PODE / NÃO DEVE** fazer

| Limitação | Motivo |
|-----------|--------|
| SSH direto do Cloud Agent para bot-server | Rede privada / sem WSL |
| Deploy ou restart do servidor sem aprovação | Risco produção |
| Commit/push/PR automático | Regra do utilizador |
| Ler ficheiros só no servidor sem script | Automation não monta SSH sozinha |
| Acordar Cursor via email/webhook inbound | Não existe API Desktop nativa |
| Substituir dedupe GitHub (15 min) | Plugin já deduplica |

---

## Duas estratégias recomendadas

### A — Híbrida (melhor para si)

1. Servidor → GitHub issue em tempo real (`error-reporting.github.enabled`)
2. Task Scheduler → `wsl-fetch-rpg-errors.sh` a cada 6h
3. Cursor Automation → lê snapshot + issues abertas

### B — Só GitHub (Cloud Agent puro)

1. Desative dependência de SSH na automação
2. Trigger cron + prompt: `gh issue list --label server-error --state open`
3. Análise no body da issue

---

## Troubleshooting

| Problema | Solução |
|----------|---------|
| `ssh: Permission denied` | Chave em WSL: `ssh-copy-id daniel@bot-server` |
| tmux session missing | `wsl bash scripts/wsl-restart-mineserver.sh start` |
| `latest.md` vazio | Nenhum erro capturado; verifique `error-reporting.enabled` |
| Automation não corre script | Use Task Scheduler + leitura de `.error-snapshots/` |
| Cloud Agent sem dados | Use estratégia B (GitHub issues) |

---

## Ficheiros criados

```
scripts/fetch-error-reports.sh      # lógica remota (SSH stdin)
scripts/wsl-fetch-rpg-errors.sh     # entrada Windows/WSL
automation/rpg-error-monitor-prefill.json
docs/CURSOR-AUTOMATION-ERROR-MONITORING.md
.error-snapshots/                   # gerado localmente (gitignored)
```

---

## English summary

Cursor Automations cannot SSH to your Minecraft host by themselves. Run `wsl-fetch-rpg-errors.sh` on a schedule (or let the automation invoke it when WSL is available), then point the scheduled agent at `.error-snapshots/summary.md`. For cloud-only runs, rely on GitHub issues labeled `server-error` that the RPG plugin already creates. Never auto-deploy from this automation.
