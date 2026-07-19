# RPG Server Plugin

Plugin Paper para adicionar uma camada RPG ao servidor Civs, com quests iniciais por arquétipo, progresso persistente por jogador e integrações com Vault, Civs, AuraSkills, PlaceholderAPI e LuckPerms.

**Versão atual:** `0.1.2` (MVP v0.1)

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
| **Civs 1.11.7** | `softdepend` | Recomendado — objetivos `build_region` / mana HUD |
| **AuraSkills** | `softdepend` | Soft — carregado via `SoftHookFactory` se presente |
| **PlaceholderAPI** | `softdepend` | Soft — placeholders de HUD/quests |
| **LuckPerms** | `softdepend` | Soft — gate de permissões de quest via SoftHook |
| **ChestShop** | `softdepend` | Opcional — quests merchant (`TransactionEvent`) |
| **Essentials** | `softdepend` | Opcional — provider Vault, kits/warps como recompensa |
| **VeinMiner** | `softdepend` | Opcional — objetivo `vein_mine` (desligado no config por padrão) |

Vault é hard-depend. **AuraSkills** e **LuckPerms** são soft-deps (PR #64 SoftHookFactory) — o plugin sobe sem eles. **InteractiveBooks** foi removido do código (Player Hub substituiu lore books); o JAR no servidor é legado e não é usado pelo RPGServer.

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

Para compilar este plugin, é necessário ter o código de ambos os repositórios (`Civs-1.11.6` e `rpg-server-plugin`) no mesmo diretório pai (pastas lado a lado), pois o RPG Server possui uma dependência direta (em escopo `system`) ao JAR gerado do Civs.

#### Pré-requisitos
1. **Java JDK 25**: O projeto utiliza recursos do Java 25. Recomendamos instalar o **Eclipse Temurin JDK 25**.
   - Garanta que a variável de ambiente `JAVA_HOME` aponte para o JDK 25.
   - Adicione o JDK ao `PATH` do sistema.
2. **Apache Maven 3.9.x**: O projeto gerencia as dependências com Maven.
   - Adicione o executável do Maven (`mvn` ou `mvn.cmd`) ao `PATH` do sistema.

#### Passo 1: Resolver Dependências do Civs (Bug do JitPack)
O plugin Civs possui uma dependência externa (`NoCheatPlus`) que falha ao baixar do JitPack. É preciso instalá-la manualmente no repositório Maven local antes de compilar o Civs. Siga o guia abaixo:

- **Windows (PowerShell)**:
  ```powershell
  # Criar pasta temporária
  New-Item -ItemType Directory -Force -Path "$env:TEMP\ncp"
  # Baixar o JAR oficial do NoCheatPlus
  Invoke-WebRequest -Uri "https://github.com/Updated-NoCheatPlus/NoCheatPlus/releases/download/v1.5/NoCheatPlus.jar" -OutFile "$env:TEMP\ncp\NoCheatPlus.jar"
  # Criar o arquivo POM mínimo
  @'
  <project xmlns="http://maven.apache.org/POM/4.0.0">
      <modelVersion>4.0.0</modelVersion>
      <groupId>com.github.Updated-NoCheatPlus.NoCheatPlus</groupId>
      <artifactId>nocheatplus</artifactId>
      <version>1.5</version>
      <packaging>jar</packaging>
  </project>
  '@ | Out-File -FilePath "$env:TEMP\ncp\ncp-clean-pom.xml" -Encoding utf8
  # Instalar no repositório local
  mvn install:install-file -Dfile="$env:TEMP\ncp\NoCheatPlus.jar" -DpomFile="$env:TEMP\ncp\ncp-clean-pom.xml"
  ```
- **Windows (CMD / Prompt de Comando)**:
  ```cmd
  mkdir %TEMP%\ncp
  curl -sL -o %TEMP%\ncp\NoCheatPlus.jar https://github.com/Updated-NoCheatPlus/NoCheatPlus/releases/download/v1.5/NoCheatPlus.jar
  echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"^>^<modelVersion^>4.0.0^</modelVersion^>^<groupId^>com.github.Updated-NoCheatPlus.NoCheatPlus^</groupId^>^<artifactId^>nocheatplus^</artifactId^>^<version^>1.5^</version^>^<packaging^>jar^</packaging^>^</project^> > %TEMP%\ncp\ncp-clean-pom.xml
  mvn install:install-file -Dfile=%TEMP%\ncp\NoCheatPlus.jar -DpomFile=%TEMP%\ncp\ncp-clean-pom.xml
  ```
- **Linux / macOS / WSL**:
  ```bash
  curl -sL -o /tmp/NoCheatPlus.jar https://github.com/Updated-NoCheatPlus/NoCheatPlus/releases/download/v1.5/NoCheatPlus.jar
  printf '<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>com.github.Updated-NoCheatPlus.NoCheatPlus</groupId><artifactId>nocheatplus</artifactId><version>1.5</version><packaging>jar</packaging></project>' > /tmp/ncp-clean-pom.xml
  mvn install:install-file -Dfile=/tmp/NoCheatPlus.jar -DpomFile=/tmp/ncp-clean-pom.xml
  ```

#### Passo 2: Compilar o Civs
Compile primeiro o plugin Civs para gerar o arquivo `.jar` exigido pelo RPG Server:
```bash
cd ../Civs-1.11.6
mvn clean package -DskipTests
```
Isso gerará o arquivo `target/civs-1.11.7.jar`.

#### Passo 3: Compilar o RPG Server
Volte ao diretório do RPG Server e execute o empacotamento:
```bash
cd ../rpg-server-plugin
mvn clean package -DskipTests
```

Artefato gerado:
```text
target/rpg-server-0.1.2.jar
```

### 2. Copiar para o servidor

1. Pare o servidor ou use uma janela de manutenção.
2. Copie `target/rpg-server-0.1.2.jar` para a pasta `plugins/` do Paper.
3. Copie o JAR gerado no passo 2 (`../Civs-1.11.6/target/civs-1.11.7.jar`) também para a pasta `plugins/` do Paper.
4. Confirme que **Vault**, **Civs**, **AuraSkills**, **PlaceholderAPI** e **LuckPerms** estão na mesma pasta `plugins/` do servidor.
5. Inicie o servidor.

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
# 1. Compilar Civs
cd ../Civs-1.11.6
mvn clean package -DskipTests

# 2. Compilar RPG Server
cd ../rpg-server-plugin
mvn clean package -DskipTests
```

Documentação técnica adicional: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).
