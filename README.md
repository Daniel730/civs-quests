# RPG Server Plugin

Plugin Paper para adicionar uma camada RPG ao servidor Civs, com quests iniciais por arquétipo, progresso persistente por jogador e integrações com Vault, Civs, AuraSkills, PlaceholderAPI e LuckPerms.

## Estado atual

- Scaffold Maven completo para Paper 26.1.2 / Java 25.
- `plugin.yml`, `config.yml` e quests iniciais em `src/main/resources`.
- Hooks de integração para Vault, Civs, AuraSkills, PlaceholderAPI e LuckPerms.
- Placeholders `%rpg_archetype%` e `%rpg_active_quest%`.
- Comandos `/rpg quest list`, `/rpg quest status`, `/rpg profile` e `/rpg reload`.
- Persistência YAML assíncrona em `plugins/RPGServer/players/<uuid>.yml`.

## Requisitos

- Java 25.
- Paper 26.1.2.
- Vault instalado no servidor.
- Civs instalado para objetivos de construção.
- AuraSkills instalado para objetivos de skill.
- PlaceholderAPI instalado para placeholders.
- LuckPerms opcional para controlar acesso por quest.

## Build local

O `pom.xml` usa o JAR do Civs com `system` scope:

```powershell
..\Civs-1.11.6\target\civs-1.11.6.jar
```

Compile:

```powershell
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" compile
```

Gerar JAR:

```powershell
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" package
```

O artefato sai em:

```text
target/rpg-server-0.1.0-SNAPSHOT.jar
```

## Deploy no servidor

1. Pare o servidor ou use uma janela de manutenção.
2. Copie `target/rpg-server-0.1.0-SNAPSHOT.jar` para a pasta `plugins/` do servidor Paper.
3. Confirme que Vault, Civs, AuraSkills, PlaceholderAPI e LuckPerms estão na mesma pasta `plugins/` quando forem necessários.
4. Inicie o servidor.
5. Na primeira execução, o plugin cria `plugins/RPGServer/config.yml`, `plugins/RPGServer/quests/` e `plugins/RPGServer/players/`.
6. Teste no jogo:

```text
/rpg quest list
/rpg quest status
/papi parse me %rpg_archetype%
/papi parse me %rpg_active_quest%
```

## Comandos

| Comando | Permissão | Descrição |
| --- | --- | --- |
| `/rpg quest list` | `rpg.quest` | Lista quests disponíveis e status resumido. |
| `/rpg quest status` | `rpg.quest` | Mostra objetivos e progresso de cada quest. |
| `/rpg profile` | `rpg.profile` | Mostra arquétipo e quest ativa do jogador. |
| `/rpg reload` | `rpg.admin` | Recarrega config e quests. |

## Placeholders

| Placeholder | Valor |
| --- | --- |
| `%rpg_archetype%` | `Guerreiro`, `Construtor`, `Mercador` ou `Nenhum`. |
| `%rpg_active_quest%` | Nome da quest ativa ou `Nenhuma`. |

## Quests iniciais

As três quests em `quests/*.yml` espelham os caminhos iniciais do tutorial Civs:

- `warrior_path`: shelter, altar e Fighting nível 5.
- `builder_path`: shelter, council_room e Foraging nível 5.
- `merchant_path`: plot7x7, shack e Farming nível 5.

## Permissões por quest

Quando LuckPerms está ativo, o plugin checa permissões no formato configurado em `config.yml`:

```yaml
integrations:
  luckperms:
    quest-permission-prefix: "rpg.quest."
```

Exemplo:

```text
rpg.quest.warrior_path
```
