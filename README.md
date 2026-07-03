# RPG Server Plugin

Plugin Paper para adicionar uma camada RPG ao servidor Civs, com quests iniciais por arquétipo, progresso persistente por jogador e integrações com Vault, Civs, AuraSkills, PlaceholderAPI e LuckPerms.

**Versão atual:** `0.1.0-SNAPSHOT` (MVP v0.1)

---

## MVP v0.1 — o que funciona vs. o que ainda não existe

### O que funciona (uso real no servidor)

| Área | Descrição |
| --- | --- |
| **Bootstrap** | Carrega config, hooks, quests, listeners, autosave e `/rpg reload`. |
| **Quests YAML** | Lê `plugins/RPGServer/quests/*.yml`; na 1ª execução copia as 3 quests empacotadas. |
| **Objetivo `build_region`** | Escuta `RegionCreatedEvent` do Civs e compara `regionType.getKey()` com o YAML. |
| **Objetivo `skill_level`** | Escuta `SkillLevelUpEvent` do AuraSkills e compara skill + nível. |
| **Progresso passivo** | Não há comando para aceitar quest — o avanço é automático ao construir regiões ou subir skills. |
| **Persistência** | YAML em `plugins/RPGServer/players/<uuid>.yml`; save no logout, autosave (5 min) e disable. |
| **Comandos** | `/rpg quest list`, `/rpg quest status`, `/rpg profile`, `/rpg reload`. |
| **Placeholders** | `%rpg_archetype%` e `%rpg_active_quest%` via expansão PlaceholderAPI `rpg`. |
| **LuckPerms (filtro)** | Com LP ativo, só progride quem tiver `rpg.quest.<id>` (ver secção abaixo). |
| **Mensagens** | MiniMessage com prefixo configurável; feedback ao completar objetivo ou quest. |

**Fluxo típico:** o jogador entra → perfil carregado (ou criado vazio) → constrói regiões Civs ou sobe skills AuraSkills → objetivos marcados com mensagem amarela → quest concluída com mensagem verde → `/rpg quest status` mostra ✓/○ por objetivo.

### O que ainda não está implementado

| Item | Estado |
| --- | --- |
| **Recompensas** | Nenhuma — sem dinheiro Vault, itens ou comandos ao completar quest. |
| **Vault / economia** | Hook conecta `Economy`, mas **não é usado** em quests ou comandos. |
| **Aceitar / abandonar quest** | Sem comando ou GUI para escolher, iniciar ou cancelar caminho. |
| **Escolha de arquétipo** | Definido automaticamente na primeira quest com progresso; sem troca. |
| **Progresso retroativo** | Regiões ou skills obtidas **antes** de instalar o plugin **não contam**. |
| **`quests.starter-quest-id: welcome`** | Quest `welcome` não existe; nunca é atribuída automaticamente. |
| **`quests.max-active`** | Lido no config, mas **não aplicado** — as 3 quests podem progredir em paralelo. |
| **`integrations.civs.require-town-for-quests`** | Só no YAML; não lido pelo código. |
| **`integrations.auraskills.sync-on-join`** | Só no YAML; sem sync de skills ao entrar. |
| **`settings.debug`**, **`progression.default-track`** | Presentes no config sem efeito. |
| **Tutorial Civs nativo** | Objetivos espelham os tutoriais, mas **não ligam** ao fluxo de menus do Civs. |
| **Testes automatizados** | Zero testes no projeto. |

> **Nota:** construir um `shelter` conta para guerreiro **e** construtor (objetivo partilhado). O arquétipo fica fixado na primeira quest em que houver progresso.

---

## Dependências no servidor

| Plugin | `plugin.yml` | Na prática |
| --- | --- | --- |
| **Paper 26.1.2** + **Java 25** | — | Obrigatório |
| **Vault** | `depend` | Obrigatório (JAR presente) |
| **Plugin de economia** (EssentialsX, etc.) | — | Recomendado — Vault precisa de provider; senão só aviso no log |
| **Civs 1.11.6** | `softdepend` | **Obrigatório** — objetivos `build_region` |
| **AuraSkills** | `softdepend` | **Obrigatório** — objetivos `skill_level` |
| **PlaceholderAPI** | `softdepend` | **Obrigatório** — classes carregadas no startup |
| **LuckPerms** | `softdepend` | **Obrigatório** — classes carregadas no startup |
| **ChestShop** | `softdepend` | Opcional — quests merchant (`TransactionEvent`); Sprint 1+ |
| **Essentials** | `softdepend` | Opcional — provider Vault, kits/warps como recompensa |
| **InteractiveBooks** | `softdepend` | Opcional — lore de quest em livros |
| **VeinMiner** | `softdepend` | Opcional — objetivo `vein_mine` (desligado no config por padrão) |

Civs, AuraSkills, PlaceholderAPI e LuckPerms são **hard runtime deps** do MVP v0.1 (imports directos). ChestShop, Essentials e InteractiveBooks degradam graciosamente quando ausentes ou `integrations.*.enabled: false`.

Stack completo e backlog: `.cursor/skills/rpg-server-plugin/FEATURE-EXTRACTION.md`.

---

## LuckPerms — permissões obrigatórias

Com `integrations.luckperms.enabled: true` (padrão) e LuckPerms instalado, **nenhuma quest progride** sem a permissão correspondente.

Formato (prefixo configurável em `config.yml`):

```text
rpg.quest.<id_da_quest>
```

Exemplos para as quests incluídas:

```text
rpg.quest.warrior_path
rpg.quest.builder_path
rpg.quest.merchant_path
```

Conceder a um jogador:

```text
/lp user <jogador> permission set rpg.quest.warrior_path true
/lp user <jogador> permission set rpg.quest.builder_path true
/lp user <jogador> permission set rpg.quest.merchant_path true
```

**Alternativa:** desativar a integração em `plugins/RPGServer/config.yml`:

```yaml
integrations:
  luckperms:
    enabled: false
```

Com LP desativado no config, todas as quests ficam abertas (sem filtro por permissão).

---

## Instalação (Paper)

### 1. Compilar

O `pom.xml` usa o JAR do Civs com `system` scope:

```text
../Civs-1.11.6/target/civs-1.11.6.jar
```

```powershell
cd C:\Users\Danie\Downloads\Civs-1.11.6\rpg-server-plugin
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" package
```

Artefato gerado:

```text
target/rpg-server-0.1.0-SNAPSHOT.jar
```

### 2. Copiar para o servidor

1. Pare o servidor ou use uma janela de manutenção.
2. Copie `target/rpg-server-0.1.0-SNAPSHOT.jar` para a pasta `plugins/` do Paper.
3. Confirme que **Vault**, **Civs**, **AuraSkills**, **PlaceholderAPI** e **LuckPerms** estão na mesma pasta `plugins/` (ver secção de dependências acima).
4. Inicie o servidor.

### 3. Primeira execução

O plugin cria automaticamente:

```text
plugins/RPGServer/config.yml
plugins/RPGServer/quests/          # 3 quests default (warrior, builder, merchant)
plugins/RPGServer/players/         # um YAML por jogador
```

### 4. Configurar LuckPerms

Conceda `rpg.quest.<id>` aos jogadores **ou** defina `integrations.luckperms.enabled: false` no config (ver secção LuckPerms).

### 5. Testar in-game

```text
/rpg quest list
/rpg quest status
/rpg profile
/papi parse me %rpg_archetype%
/papi parse me %rpg_active_quest%
```

Construa regiões Civs e suba skills AuraSkills normalmente; verifique mensagens no chat e o progresso em `/rpg quest status`.

---

## Comandos

| Comando | Permissão | Default | Descrição |
| --- | --- | --- | --- |
| `/rpg help` | — | todos | Lista comandos disponíveis. |
| `/rpg quest list` | `rpg.quest` | `true` | Lista quests e status resumido. |
| `/rpg quest status` | `rpg.quest` | `true` | Mostra objetivos e progresso (✓/○). |
| `/rpg profile` | `rpg.profile` | `true` | Arquétipo e quest ativa do jogador. |
| `/rpg reload` | `rpg.admin` | OP | Recarrega config e quests. |

Alias: `/rpgserver`

---

## Placeholders (PlaceholderAPI)

Identificador da expansão: `rpg`

| Placeholder | Valor |
| --- | --- |
| `%rpg_archetype%` | `Guerreiro`, `Construtor`, `Mercador` ou `Nenhum`. |
| `%rpg_active_quest%` | Nome da quest ativa ou `Nenhuma`. |

---

## Quests iniciais

As três quests em `quests/*.yml` espelham os caminhos iniciais do tutorial Civs:

| Quest | Regiões Civs | Skill AuraSkills |
| --- | --- | --- |
| `warrior_path` | shelter, altar | Fighting nível 5 |
| `builder_path` | shelter, council_room | Foraging nível 5 |
| `merchant_path` | plot7x7, shack | Farming nível 5 |

Permissão LuckPerms correspondente: `rpg.quest.<id>` (ex.: `rpg.quest.warrior_path`).

---

## Build local (referência rápida)

```powershell
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" compile
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" package
```

Documentação técnica adicional: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).
