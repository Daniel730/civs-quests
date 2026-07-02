# Arquitetura do RPG Server Plugin

## Visão geral

`RpgServerPlugin` é o ponto de entrada. No `onEnable`, ele carrega configuração, inicializa hooks, carrega perfis online, carrega quests e registra listeners/comandos.

Fluxo principal:

```text
Paper events
  ├─ Civs RegionCreatedEvent
  └─ AuraSkills SkillLevelUpEvent
        ↓
QuestManager
        ↓
PlayerProfile
        ↓
ProfileManager async YAML save
```

## Pacotes

| Pacote | Responsabilidade |
| --- | --- |
| `dev.daniel730.rpgserver` | Bootstrap do plugin e ciclo de vida. |
| `command` | Executor e tab complete de `/rpg`. |
| `config` | Leitura tipada de `config.yml`. |
| `hook` | Inicialização das integrações externas. |
| `listener` | Eventos de jogador, Civs e AuraSkills. |
| `placeholder` | Expansão PlaceholderAPI `rpg`. |
| `profile` | Modelo e persistência de perfil por jogador. |
| `quest` | Modelo, carregamento e avanço de quests. |
| `util` | Utilitários de mensagens MiniMessage. |

## Integrações

- Vault: hook obrigatório em `plugin.yml`; usado como base para economia em fases futuras.
- Civs: softdepend; escuta `RegionCreatedEvent` diretamente pelo JAR `system` scope.
- AuraSkills: softdepend; escuta `SkillLevelUpEvent`.
- PlaceholderAPI: registra a expansão `rpg`.
- LuckPerms: softdepend; usa permissões Bukkit compatíveis com LuckPerms para liberar quests.

## QuestManager

`QuestManager` carrega arquivos `quests/*.yml` do diretório de dados do plugin. Na primeira execução, copia as quests empacotadas em `src/main/resources/quests`.

Objetivos suportados nesta fase:

- `build_region`: concluído quando Civs dispara `RegionCreatedEvent` com `RegionType#getKey()` igual ao campo `region`.
- `skill_level`: concluído quando AuraSkills dispara `SkillLevelUpEvent` com skill e nível compatíveis.

Quando um objetivo avança, o perfil é marcado como dirty e salvo de forma assíncrona pelo autosave.

## Perfis

Cada jogador tem um arquivo:

```text
plugins/RPGServer/players/<uuid>.yml
```

Campos persistidos:

- `archetype`
- `active-quests`
- `completed-quests`
- `completed-objectives`

O salvamento ocorre no logout, no autosave e no `onDisable`.

## Placeholders

`RpgPlaceholderExpansion` implementa:

- `%rpg_archetype%`
- `%rpg_active_quest%`

Os valores são calculados a partir do `PlayerProfile` carregado para o UUID consultado.

## Próximas fases sugeridas

- Recompensas via Vault, itens e comandos.
- Escolha explícita de arquétipo por GUI/comando.
- Integração mais profunda com o tutorial nativo do Civs.
- Testes automatizados para parser de quests e persistência de perfis.
- Objetivos adicionais como kill, buy, upkeep e menu_action.
