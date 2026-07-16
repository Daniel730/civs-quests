package dev.daniel730.rpgserver.config;

import dev.daniel730.rpgserver.quest.QuestAcceptResult;
import org.bukkit.configuration.file.FileConfiguration;

public final class PluginConfig {

    private final boolean debug;
    private final boolean civsEnabled;
    private final boolean auraSkillsEnabled;
    private final boolean vaultEnabled;
    private final boolean requireEconomy;
    private final boolean placeholderEnabled;
    private final boolean luckPermsEnabled;
    private final boolean chestShopEnabled;
    private final boolean essentialsEnabled;
    private final boolean essentialsKitRewardsEnabled;
    private final boolean essentialsWarpRewardsEnabled;
    private final boolean guideBookOnJoin;
    private final boolean veinMinerEnabled;
    private final String questPermissionPrefix;
    private final int autosaveMinutes;
    private final boolean syncOnJoinFromCivs;
    private final int maxActiveQuests;
    private final String starterQuestId;
    private final boolean allowAbandon;
    private final String questResetTimezone;
    private final String messagePrefix;
    private final String noPermissionMessage;
    private final String reloadSuccessMessage;
    private final boolean questNotificationsEnabled;
    private final boolean questBossBarEnabled;
    private final String questObjectiveActionBar;
    private final String questObjectiveTitle;
    private final String questObjectiveSubtitle;
    private final String questCompleteActionBar;
    private final String questCompleteTitle;
    private final String questCompleteSubtitle;
    private final String questBossBarTitle;
    private final String questAcceptSuccess;
    private final String questAcceptNotFound;
    private final String questAcceptAlreadyComplete;
    private final String questAcceptAlreadyStarted;
    private final String questAcceptLocked;
    private final String questAcceptNoPermission;
    private final String questAcceptMaxActive;
    private final String questAcceptArchetypeLocked;
    private final String questTrackSuccess;
    private final String questTrackFailed;
    private final int questProgressNotifyInterval;
    private final String questProgressActionBar;
    private final String questJournalLockedPrerequisite;
    private final String questJournalLockedPermission;
    private final String questJournalLockedGeneric;
    private final String questJournalTrackedBadge;
    private final String questJournalTrackHint;
    private final String questJournalAlreadyTracked;
    private final String questJournalAcceptHint;
    private final String questJournalAbandonHint;
    private final String questJournalCompletedHint;
    private final String questJournalPageIndicator;
    private final String questSyncSuccessAll;
    private final String questSyncSuccessOne;
    private final String hubTitle;
    private final String hubItemName;
    private final String hubItemLore;
    private final String hubGranted;
    private final String hubRefreshed;
    private final String hubInventoryFull;
    private final String hubAlreadyHave;
    private final String hubTabInicio;
    private final String hubTabCivs;
    private final String hubTabRpg;
    private final String hubTabConfig;
    private final String hubTabQuests;
    private final String hubFooterRefresh;
    private final String hubFooterBack;
    private final String hubFooterTrack;
    private final String hubFooterSync;
    private final String hubFooterClose;
    private final String hubFooterJournal;
    private final String hubInicioProfileTitle;
    private final String hubInicioChoosePath;
    private final String hubInicioChoosePathLore;
    private final String hubInicioNextQuest;
    private final String hubInicioNextQuestLore;
    private final String hubInicioNoQuest;
    private final String hubCivsTownTitle;
    private final String hubCivsTownLore;
    private final String hubCivsLocationsTitle;
    private final String hubCivsLocationsLore;
    private final String hubCivsTownInfoTitle;
    private final String hubCivsTownInfoLore;
    private final String hubCivsRegionsTitle;
    private final String hubCivsRegionsLore;
    private final String hubCivsAuctionTitle;
    private final String hubCivsAuctionLore;
    private final String hubCivsSpellsTitle;
    private final String hubCivsSpellsLore;
    private final String hubCivsFarmsTitle;
    private final String hubCivsFarmsLore;
    private final String hubCivsCombatTitle;
    private final String hubCivsCombatLore;
    private final String hubCivsChestShopTitle;
    private final String hubCivsChestShopLore;
    private final String hubRpgJournalTitle;
    private final String hubRpgJournalLore;
    private final String hubRpgQuestTreeTitle;
    private final String hubRpgQuestTreeLore;
    private final String hubRpgPerksTitle;
    private final String hubRpgPerksLore;
    private final String hubRpgPerkSummaryTitle;
    private final String hubRpgPerkSummaryLore;
    private final String hubRpgDailyTitle;
    private final String hubRpgWeeklyTitle;
    private final String hubRpgScheduleAvailable;
    private final String hubRpgScheduleDone;
    private final String hubRpgScheduleLocked;
    private final String hubRpgScheduleNone;
    private final String hubRpgScheduleHint;
    private final String hubRpgProfileTitle;
    private final String hubRpgProfileLore;
    private final String hubConfigNotificationsTitle;
    private final String hubConfigNotificationsLore;
    private final String hubConfigBossBarTitle;
    private final String hubConfigBossBarLore;
    private final String hubConfigHintTitle;
    private final String hubConfigHintLore;
    private final String hubQuestsOpenJournalTitle;
    private final String hubQuestsOpenJournalLore;
    private final String settingsNotificationsOn;
    private final String settingsNotificationsOff;
    private final String settingsBossBarOn;
    private final String settingsBossBarOff;
    private final double rewardMoneyMultiplier;
    private final double rewardSkillXpMultiplier;
    private final double rewardCivsSkillXpMultiplier;
    private final String rewardSummaryHeader;
    private final String questJournalChainNext;
    private final String questJournalChainRequires;
    private final boolean dailyCtaEnabled;
    private final String dailyCtaTitle;
    private final String dailyCtaSubtitle;
    private final TrackedHudMode trackedHudMode;
    private final TransientHudChannel transientHudChannel;
    private final boolean composedHudEnabled;
    private final int composedHudIntervalTicks;
    private final String composedHudFormat;
    private final boolean hideVanillaHeartsEnabled;
    private final int hideVanillaHeartsHttpPort;
    private final String hideVanillaHeartsHost;
    private final String hideVanillaHeartsUrl;
    private final boolean hideVanillaHeartsForce;
    private final String hideVanillaHeartsPrompt;
    private final boolean questProgressPulse;
    private final String questProgressSound;
    private final float questProgressSoundVolume;
    private final float questProgressSoundPitch;
    private final String questObjectiveCompleteSound;
    private final float questObjectiveCompleteSoundVolume;
    private final float questObjectiveCompleteSoundPitch;
    private final boolean questObjectiveCompleteParticles;
    private final int questObjectiveTitleFadeIn;
    private final int questObjectiveTitleStay;
    private final int questObjectiveTitleFadeOut;
    private final String questCompleteSound;
    private final float questCompleteSoundVolume;
    private final float questCompleteSoundPitch;
    private final boolean questCompleteFirework;
    private final boolean questCompleteParticles;
    private final int questCompleteTitleFadeIn;
    private final int questCompleteTitleStay;
    private final int questCompleteTitleFadeOut;
    private final String journalOpenSound;
    private final float journalOpenSoundPitch;
    private final String journalAcceptSound;
    private final float journalAcceptSoundPitch;
    private final String journalTrackSound;
    private final float journalTrackSoundPitch;
    private final String journalClickSound;
    private final float journalSoundVolume;
    private final boolean welcomeEnabled;
    private final String welcomeTitle;
    private final String welcomeSubtitle;
    private final String welcomeMessage;
    private final String welcomeSound;
    private final float welcomeSoundVolume;
    private final float welcomeSoundPitch;
    private final boolean welcomeGiveHubItem;
    private final int welcomeTitleFadeIn;
    private final int welcomeTitleStay;
    private final int welcomeTitleFadeOut;

    private final boolean rebirthEnabled;
    private final java.util.List<String> rebirthCapstoneIds;
    private final double rebirthEssenceRefundPercent;
    private final int pathEssencePerTier;
    private final java.util.Map<String, PathTraitConfig> pathTraits;
    private final double huntSpawnPartyRadius;
    private final long huntSpawnCooldownSeconds;
    private final java.util.List<String> dailyRotationPool;
    private final java.util.List<String> weeklyRotationPool;
    private final int dailyRotationCount;
    private final int weeklyRotationCount;

    public record PathTraitConfig(String buffStat, double buffValue, String buffOperation,
                                  String debuffStat, double debuffValue, String debuffOperation) {
    }

    public PluginConfig(FileConfiguration config) {
        this.debug = config.getBoolean("settings.debug", false);
        this.civsEnabled = config.getBoolean("integrations.civs.enabled", true);
        this.auraSkillsEnabled = config.getBoolean("integrations.auraskills.enabled", true);
        this.vaultEnabled = config.getBoolean("integrations.vault.enabled", true);
        this.requireEconomy = config.getBoolean("integrations.vault.require-economy", true);
        this.placeholderEnabled = config.getBoolean("integrations.placeholderapi.enabled", true);
        this.luckPermsEnabled = config.getBoolean("integrations.luckperms.enabled", true);
        this.chestShopEnabled = config.getBoolean("integrations.chestshop.enabled", true);
        this.essentialsEnabled = config.getBoolean("integrations.essentials.enabled", true);
        this.essentialsKitRewardsEnabled = config.getBoolean("integrations.essentials.kit-rewards", true);
        this.essentialsWarpRewardsEnabled = config.getBoolean("integrations.essentials.warp-rewards", true);
        this.guideBookOnJoin = config.getBoolean("player-hub.on-join",
                config.getBoolean("guide-book.on-join", true));
        this.veinMinerEnabled = config.getBoolean("integrations.veinminer.enabled", false);
        this.questPermissionPrefix = config.getString("integrations.luckperms.quest-permission-prefix", "rpg.quest.");
        this.autosaveMinutes = config.getInt("progression.autosave-minutes", 5);
        this.syncOnJoinFromCivs = config.getBoolean("progression.sync-on-join-from-civs", false);
        this.maxActiveQuests = config.getInt("quests.max-active", 3);
        this.allowAbandon = config.getBoolean("quests.allow-abandon", true);
        this.questResetTimezone = config.getString("quests.reset-timezone", "UTC");
        this.starterQuestId = config.getString("quests.starter-quest-id", "welcome");
        this.messagePrefix = config.getString("messages.prefix", "<gray>[<gold>RPG</gold>]</gray> ");
        this.noPermissionMessage = config.getString("messages.no-permission", "<red>Sem permissão.</red>");
        this.reloadSuccessMessage = config.getString("messages.reload-success", "<green>Configuração recarregada.</green>");
        this.questNotificationsEnabled = config.getBoolean("messages.quest-notifications", true);
        this.questBossBarEnabled = config.getBoolean("messages.quest-bossbar.enabled",
                config.getBoolean("messages.quest-bossbar", true));
        this.questObjectiveActionBar = config.getString("messages.quest-objective-complete.action-bar",
                "<yellow>✓</yellow> <white>{objective}</white>");
        this.questObjectiveTitle = config.getString("messages.quest-objective-complete.title",
                "<gold>Objetivo concluído</gold>");
        this.questObjectiveSubtitle = config.getString("messages.quest-objective-complete.subtitle",
                "<gray>{objective}</gray>");
        this.questCompleteActionBar = config.getString("messages.quest-complete.action-bar",
                "<green>✓</green> <white>{quest}</white>");
        this.questCompleteTitle = config.getString("messages.quest-complete.title",
                "<green>Quest concluída!</green>");
        this.questCompleteSubtitle = config.getString("messages.quest-complete.subtitle",
                "<white>{quest}</white>");
        this.questBossBarTitle = config.getString("messages.quest-bossbar-title",
                config.getString("messages.quest-bossbar.title",
                        "<gold>{quest}</gold> <gray>({progress})</gray>"));
        this.questAcceptSuccess = config.getString("messages.quest-accept.success",
                "<green>Quest aceita:</green> <white>{quest}</white>");
        this.questAcceptNotFound = config.getString("messages.quest-accept.not-found",
                "<red>Quest não encontrada.</red>");
        this.questAcceptAlreadyComplete = config.getString("messages.quest-accept.already-complete",
                "<red>Esta quest já foi concluída.</red>");
        this.questAcceptAlreadyStarted = config.getString("messages.quest-accept.already-started",
                "<red>Esta quest já está em andamento.</red>");
        this.questAcceptLocked = config.getString("messages.quest-accept.locked",
                "<red>Pré-requisitos não atendidos.</red>");
        this.questAcceptNoPermission = config.getString("messages.quest-accept.no-permission",
                "<red>Você não tem permissão para esta quest.</red>");
        this.questAcceptMaxActive = config.getString("messages.quest-accept.max-active",
                "<red>Limite de quests ativas atingido.</red>");
        this.questAcceptArchetypeLocked = config.getString("messages.quest-accept.archetype-locked",
                "<red>Você já escolheu outro caminho. Cada arquétipo tem quests únicas.</red>");
        this.questTrackSuccess = config.getString("messages.quest-track.success",
                "<yellow>Rastreando:</yellow> <white>{quest}</white>");
        this.questTrackFailed = config.getString("messages.quest-track.failed",
                "<red>Não foi possível rastrear esta quest.</red>");
        this.questProgressNotifyInterval = config.getInt("messages.quest-progress.interval", 5);
        this.questProgressActionBar = config.getString("messages.quest-progress.action-bar",
                "<gray>{objective}</gray> <white>({current}/{total})</white>");
        this.questJournalLockedPrerequisite = config.getString("messages.quest-journal.locked-prerequisite",
                "<red>Falta concluir:</red> <white>{quest}</white>");
        this.questJournalLockedPermission = config.getString("messages.quest-journal.locked-permission",
                "<red>Requer desbloqueio (permissão).</red>");
        this.questJournalLockedGeneric = config.getString("messages.quest-journal.locked-generic",
                "<red>Esta quest está bloqueada.</red> <gray>Cumpra os requisitos acima.</gray>");
        this.questJournalTrackedBadge = config.getString("messages.quest-journal.tracked-badge",
                "<gold>★ Quest rastreada</gold>");
        this.questJournalTrackHint = config.getString("messages.quest-journal.track-hint",
                "<gold>▶ Clique para rastrear</gold> <gray>(boss bar + notificações)</gray>");
        this.questJournalAlreadyTracked = config.getString("messages.quest-journal.already-tracked",
                "<gray>{quest} já está sendo rastreada.</gray>");
        this.questJournalAcceptHint = config.getString("messages.quest-journal.accept-hint",
                "<yellow>▶ Clique para aceitar a quest.</yellow>");
        this.questJournalAbandonHint = config.getString("messages.quest-journal.abandon-hint",
                "<red>▶ Shift-clique para abandonar.</red>");
        this.questJournalCompletedHint = config.getString("messages.quest-journal.completed-hint",
                "<aqua>✔ Quest concluída.</aqua>");
        this.questJournalPageIndicator = config.getString("messages.quest-journal.page-indicator",
                "<dark_gray>Página {page}/{total}</dark_gray>");
        this.questSyncSuccessAll = config.getString("messages.quest-sync.success-all",
                "<green>Sync concluído</green> para <white>{players}</white> jogador(es): "
                        + "<yellow>{objectives}</yellow> objetivo(s) e <yellow>{quests}</yellow> quest(s) backfilled.");
        this.questSyncSuccessOne = config.getString("messages.quest-sync.success-one",
                "<green>Sync concluído</green> para <white>{player}</white>: "
                        + "<yellow>{objectives}</yellow> objetivo(s) e <yellow>{quests}</yellow> quest(s) backfilled.");
        this.hubTitle = config.getString("messages.player-hub.title",
                "<gold><bold>Central do Reino</bold></gold>");
        this.hubItemName = config.getString("messages.player-hub.item-name",
                "<gold><italic>Central do Reino</italic></gold>");
        this.hubItemLore = config.getString("messages.player-hub.item-lore",
                "<gray>Clique direito ou </gray><yellow>/rpg hub</yellow>");
        this.hubGranted = config.getString("messages.player-hub.grant",
                "<gray>Central do Reino recebida.</gray> <yellow>Clique direito</yellow> <gray>ou</gray> <yellow>/rpg hub</yellow>");
        this.hubRefreshed = config.getString("messages.player-hub.refreshed",
                "<gray>Central atualizada com seu progresso.</gray>");
        this.hubInventoryFull = config.getString("messages.player-hub.inventory-full",
                "<yellow>Inventário cheio — item dropado no chão.</yellow>");
        this.hubAlreadyHave = config.getString("messages.player-hub.already-have",
                "<gray>Você já possui a Central do Reino.</gray> <yellow>/rpg hub</yellow> <gray>para abrir.</gray>");
        this.hubTabInicio = config.getString("messages.player-hub.tabs.inicio", "Início");
        this.hubTabCivs = config.getString("messages.player-hub.tabs.civs", "Civs");
        this.hubTabRpg = config.getString("messages.player-hub.tabs.rpg", "RPG");
        this.hubTabConfig = config.getString("messages.player-hub.tabs.config", "Config");
        this.hubTabQuests = config.getString("messages.player-hub.tabs.quests", "Quests");
        this.hubFooterRefresh = config.getString("messages.player-hub.footer.refresh",
                "<aqua>↻ Atualizar</aqua>");
        this.hubFooterBack = config.getString("messages.player-hub.footer.back",
                "<yellow>← Voltar</yellow>");
        this.hubFooterTrack = config.getString("messages.player-hub.footer.track",
                "<gold>★ Rastrear</gold>");
        this.hubFooterSync = config.getString("messages.player-hub.footer.sync",
                "<green>↺ Sync</green>");
        this.hubFooterClose = config.getString("messages.player-hub.footer.close",
                "<red>✕ Fechar</red>");
        this.hubFooterJournal = config.getString("messages.player-hub.footer.journal",
                "<yellow>Diário de Quests</yellow>");
        this.hubInicioProfileTitle = config.getString("messages.player-hub.inicio.profile-title",
                "<gold>{player}</gold>");
        this.hubInicioChoosePath = config.getString("messages.player-hub.inicio.choose-path",
                "<yellow><bold>Escolher Caminho</bold></yellow>");
        this.hubInicioChoosePathLore = config.getString("messages.player-hub.inicio.choose-path-lore",
                "<gray>Guerreiro, Mercador ou Construtor — aceite direto da Central.</gray>");
        this.hubInicioNextQuest = config.getString("messages.player-hub.inicio.next-quest",
                "<green><bold>Próxima Missão</bold></green>");
        this.hubInicioNextQuestLore = config.getString("messages.player-hub.inicio.next-quest-lore",
                "<aqua>{quest}</aqua> <dark_gray>— clique para rastrear</dark_gray>");
        this.hubInicioNoQuest = config.getString("messages.player-hub.inicio.no-quest",
                "<gray>Sem missões pendentes</gray>");
        this.hubCivsTownTitle = config.getString("messages.player-hub.civs.town-title",
                "<gold><bold>Menu Civs</bold></gold>");
        this.hubCivsTownLore = config.getString("messages.player-hub.civs.town-lore",
                "<gray>Menu principal Civs — cidades, magias, farms.</gray>");
        this.hubCivsLocationsTitle = config.getString("messages.player-hub.civs.locations-title",
                "<aqua><bold>Locais / Teleportes</bold></aqua>");
        this.hubCivsLocationsLore = config.getString("messages.player-hub.civs.locations-lore",
                "<gray>Portais e destinos como no menu Civs.</gray>");
        this.hubCivsTownInfoTitle = config.getString("messages.player-hub.civs.town-info-title",
                "<yellow><bold>Minha Cidade</bold></yellow>");
        this.hubCivsTownInfoLore = config.getString("messages.player-hub.civs.town-info-lore",
                "<gray>Ver cidade, membros e convites.</gray>");
        this.hubCivsRegionsTitle = config.getString("messages.player-hub.civs.regions-title",
                "<green><bold>Regiões</bold></green>");
        this.hubCivsRegionsLore = config.getString("messages.player-hub.civs.regions-lore",
                "<gray>Farms, armazéns, altares e bônus.</gray>");
        this.hubCivsAuctionTitle = config.getString("messages.player-hub.civs.auction-title",
                "<gold><bold>Leilão</bold></gold>");
        this.hubCivsAuctionLore = config.getString("messages.player-hub.civs.auction-lore",
                "<gray>Casa de leilões Civs.</gray>");
        this.hubCivsSpellsTitle = config.getString("messages.player-hub.civs.spells-title",
                "<light_purple><bold>Magias</bold></light_purple>");
        this.hubCivsSpellsLore = config.getString("messages.player-hub.civs.spells-lore",
                "<gray>Altar e grimório no menu Civs.</gray>");
        this.hubCivsFarmsTitle = config.getString("messages.player-hub.civs.farms-title",
                "<yellow><bold>Farms</bold></yellow>");
        this.hubCivsFarmsLore = config.getString("messages.player-hub.civs.farms-lore",
                "<gray>Regiões agrícolas e produção.</gray>");
        this.hubCivsCombatTitle = config.getString("messages.player-hub.civs.combat-title",
                "<red><bold>Combate</bold></red>");
        this.hubCivsCombatLore = config.getString("messages.player-hub.civs.combat-lore",
                "<gray>Mobs customizados e eventos.</gray>");
        this.hubCivsChestShopTitle = config.getString("messages.player-hub.civs.chestshop-title",
                "<aqua><bold>Lojas de Jogadores</bold></aqua>");
        this.hubCivsChestShopLore = config.getString("messages.player-hub.civs.chestshop-lore",
                "<gray>Use placas ChestShop para comprar e vender.</gray>");
        this.hubRpgJournalTitle = config.getString("messages.player-hub.rpg.journal-title",
                "<gold><bold>Diário de Quests</bold></gold>");
        this.hubRpgJournalLore = config.getString("messages.player-hub.rpg.journal-lore",
                "<gray>Ver, aceitar e rastrear missões.</gray>");
        this.hubRpgQuestTreeTitle = config.getString("messages.player-hub.rpg.quest-tree-title",
                "<green><bold>Árvore de Quests</bold></green>");
        this.hubRpgQuestTreeLore = config.getString("messages.player-hub.rpg.quest-tree-lore",
                "<gray>Cadeia vertical do seu caminho — clique para aceitar/rastrear.</gray>");
        this.hubRpgPerksTitle = config.getString("messages.player-hub.rpg.perks-title",
                "<light_purple><bold>Perks</bold></light_purple>");
        this.hubRpgPerksLore = config.getString("messages.player-hub.rpg.perks-lore",
                "<gray>Ver perks desbloqueados e disponíveis.</gray>");
        this.hubRpgPerkSummaryTitle = config.getString("messages.player-hub.rpg.perk-summary-title",
                "<yellow>Perks: {unlocked}/{total}</yellow>");
        this.hubRpgPerkSummaryLore = config.getString("messages.player-hub.rpg.perk-summary-lore",
                "<gray>Bônus de combate e territorial por caminho.</gray>");
        this.hubRpgDailyTitle = config.getString("messages.player-hub.rpg.daily-title",
                "<gold><bold>Missões Diárias</bold></gold>");
        this.hubRpgWeeklyTitle = config.getString("messages.player-hub.rpg.weekly-title",
                "<gold><bold>Missões Semanais</bold></gold>");
        this.hubRpgScheduleAvailable = config.getString("messages.player-hub.rpg.schedule-available",
                "<green>● Disponível</green>");
        this.hubRpgScheduleDone = config.getString("messages.player-hub.rpg.schedule-done",
                "<aqua>✔ Concluídas</aqua>");
        this.hubRpgScheduleLocked = config.getString("messages.player-hub.rpg.schedule-locked",
                "<red>● Bloqueadas</red>");
        this.hubRpgScheduleNone = config.getString("messages.player-hub.rpg.schedule-none",
                "<gray>Sem missões deste tipo</gray>");
        this.hubRpgScheduleHint = config.getString("messages.player-hub.rpg.schedule-hint",
                "<dark_gray>Abra o diário para detalhes.</dark_gray>");
        this.hubRpgProfileTitle = config.getString("messages.player-hub.rpg.profile-title",
                "<white><bold>Meu Perfil</bold></white>");
        this.hubRpgProfileLore = config.getString("messages.player-hub.rpg.profile-lore",
                "<gray>Arquétipo, quests e progresso.</gray>");
        this.hubConfigNotificationsTitle = config.getString("messages.player-hub.config.notifications-title",
                "<yellow><bold>Notificações</bold></yellow>");
        this.hubConfigNotificationsLore = config.getString("messages.player-hub.config.notifications-lore",
                "<gray>Sons e títulos ao completar objetivos.</gray>");
        this.hubConfigBossBarTitle = config.getString("messages.player-hub.config.bossbar-title",
                "<yellow><bold>Boss Bar / HUD</bold></yellow>");
        this.hubConfigBossBarLore = config.getString("messages.player-hub.config.bossbar-lore",
                "<gray>Barra de progresso da quest rastreada.</gray>");
        this.hubConfigHintTitle = config.getString("messages.player-hub.config.hint-title",
                "<gray>Preferências pessoais</gray>");
        this.hubConfigHintLore = config.getString("messages.player-hub.config.hint-lore",
                "<dark_gray>Salvas no seu perfil RPG.</dark_gray>");
        this.hubQuestsOpenJournalTitle = config.getString("messages.player-hub.quests.open-journal-title",
                "<gold><bold>Abrir Diário Completo</bold></gold>");
        this.hubQuestsOpenJournalLore = config.getString("messages.player-hub.quests.open-journal-lore",
                "<gray>Ver todas as missões do seu caminho.</gray>");
        this.settingsNotificationsOn = config.getString("messages.settings.notifications-on",
                "<green>Notificações de quest ligadas.</green>");
        this.settingsNotificationsOff = config.getString("messages.settings.notifications-off",
                "<gray>Notificações de quest desligadas.</gray>");
        this.settingsBossBarOn = config.getString("messages.settings.bossbar-on",
                "<green>Boss bar de quest ligada.</green>");
        this.settingsBossBarOff = config.getString("messages.settings.bossbar-off",
                "<gray>Boss bar de quest desligada.</gray>");
        this.questJournalChainNext = config.getString("messages.quest-journal.chain-next",
                "<aqua>Próximo:</aqua> <white>{quest}</white>");
        this.questJournalChainRequires = config.getString("messages.quest-journal.chain-requires",
                "<red>Requer:</red> <white>{quests}</white>");
        this.dailyCtaEnabled = config.getBoolean("messages.daily-cta.enabled", true);
        this.dailyCtaTitle = config.getString("messages.daily-cta.title",
                "<gold><bold>Missão Diária disponível!</bold></gold>");
        this.dailyCtaSubtitle = config.getString("messages.daily-cta.subtitle",
                "<gray>Abra o diário com /rpg journal</gray>");
        this.trackedHudMode = TrackedHudMode.fromConfig(config.getString("feedback.tracked-hud", "both"));
        this.transientHudChannel = TransientHudChannel.fromConfig(
                config.getString("feedback.transient-channel", "auto"));
        this.composedHudEnabled = config.getBoolean("hud.composed.enabled", false);
        this.composedHudIntervalTicks = Math.max(5, config.getInt("hud.composed.interval-ticks", 10));
        this.composedHudFormat = config.getString("hud.composed.format",
                "<red>❤ %auraskills_hp%/%auraskills_hp_max%</red> <dark_gray>|</dark_gray> "
                        + "<aqua>✦ %civs_mana_pair%</aqua> <dark_gray>|</dark_gray> "
                        + "<gold>{quest}</gold>");
        this.hideVanillaHeartsEnabled = config.getBoolean("hud.hide-vanilla-hearts.enabled", false);
        this.hideVanillaHeartsHttpPort = Math.max(1, config.getInt("hud.hide-vanilla-hearts.http-port", 8765));
        this.hideVanillaHeartsHost = config.getString("hud.hide-vanilla-hearts.host", "");
        this.hideVanillaHeartsUrl = config.getString("hud.hide-vanilla-hearts.url", "");
        this.hideVanillaHeartsForce = config.getBoolean("hud.hide-vanilla-hearts.force", true);
        this.hideVanillaHeartsPrompt = config.getString("hud.hide-vanilla-hearts.prompt",
                "<yellow>Pacote HUD</yellow><gray> — esconde corações vanilla (vida/mana no ActionBar)</gray>");
        this.questProgressPulse = config.getBoolean("feedback.quest-progress.pulse", true);
        this.questProgressSound = config.getString("feedback.quest-progress.sound", "BLOCK_NOTE_BLOCK_PLING");
        this.questProgressSoundVolume = (float) config.getDouble("feedback.quest-progress.sound-volume", 0.35);
        this.questProgressSoundPitch = (float) config.getDouble("feedback.quest-progress.sound-pitch", 1.6);
        this.questObjectiveCompleteSound = config.getString("feedback.quest-objective-complete.sound",
                "ENTITY_EXPERIENCE_ORB_PICKUP");
        this.questObjectiveCompleteSoundVolume = (float) config.getDouble(
                "feedback.quest-objective-complete.sound-volume", 0.8);
        this.questObjectiveCompleteSoundPitch = (float) config.getDouble(
                "feedback.quest-objective-complete.sound-pitch", 1.3);
        this.questObjectiveCompleteParticles = config.getBoolean("feedback.quest-objective-complete.particles", true);
        this.questObjectiveTitleFadeIn = config.getInt("feedback.quest-objective-complete.title-fade-in", 5);
        this.questObjectiveTitleStay = config.getInt("feedback.quest-objective-complete.title-stay", 40);
        this.questObjectiveTitleFadeOut = config.getInt("feedback.quest-objective-complete.title-fade-out", 10);
        this.questCompleteSound = config.getString("feedback.quest-complete.sound", "ENTITY_PLAYER_LEVELUP");
        this.questCompleteSoundVolume = (float) config.getDouble("feedback.quest-complete.sound-volume", 1.0);
        this.questCompleteSoundPitch = (float) config.getDouble("feedback.quest-complete.sound-pitch", 1.2);
        this.questCompleteFirework = config.getBoolean("feedback.quest-complete.firework", true);
        this.questCompleteParticles = config.getBoolean("feedback.quest-complete.particles", true);
        this.questCompleteTitleFadeIn = config.getInt("feedback.quest-complete.title-fade-in", 10);
        this.questCompleteTitleStay = config.getInt("feedback.quest-complete.title-stay", 70);
        this.questCompleteTitleFadeOut = config.getInt("feedback.quest-complete.title-fade-out", 20);
        this.journalOpenSound = config.getString("feedback.journal.open-sound", "ITEM_BOOK_PAGE_TURN");
        this.journalOpenSoundPitch = (float) config.getDouble("feedback.journal.open-sound-pitch", 1.0);
        this.journalAcceptSound = config.getString("feedback.journal.accept-sound", "ENTITY_PLAYER_LEVELUP");
        this.journalAcceptSoundPitch = (float) config.getDouble("feedback.journal.accept-sound-pitch", 1.4);
        this.journalTrackSound = config.getString("feedback.journal.track-sound", "BLOCK_NOTE_BLOCK_CHIME");
        this.journalTrackSoundPitch = (float) config.getDouble("feedback.journal.track-sound-pitch", 1.2);
        this.journalClickSound = config.getString("feedback.journal.click-sound", "UI_BUTTON_CLICK");
        this.journalSoundVolume = (float) config.getDouble("feedback.journal.sound-volume", 0.7);
        this.welcomeEnabled = config.getBoolean("feedback.welcome.enabled", true);
        this.welcomeTitle = config.getString("messages.welcome.title",
                "<gradient:#FFD700:#FF8C00:#FFD700><bold>Bem-vindo ao Reino</bold></gradient>");
        this.welcomeSubtitle = config.getString("messages.welcome.subtitle",
                "<gray>Abra </gray><yellow>/rpg journal</yellow><gray> para começar</gray>");
        this.welcomeMessage = config.getString("messages.welcome.message",
                "<gold>★</gold> <white>Sua aventura começa agora!</white> "
                        + "<gray>Use</gray> <yellow>/rpg journal</yellow> <gray>para escolher um caminho.</gray>");
        this.welcomeSound = config.getString("feedback.welcome.sound", "ENTITY_PLAYER_LEVELUP");
        this.welcomeSoundVolume = (float) config.getDouble("feedback.welcome.sound-volume", 0.9);
        this.welcomeSoundPitch = (float) config.getDouble("feedback.welcome.sound-pitch", 0.9);
        this.welcomeGiveHubItem = config.getBoolean("feedback.welcome.give-hub-item",
                config.getBoolean("player-hub.on-join",
                        config.getBoolean("guide-book.on-join", true)));
        this.welcomeTitleFadeIn = config.getInt("feedback.welcome.title-fade-in", 15);
        this.welcomeTitleStay = config.getInt("feedback.welcome.title-stay", 80);
        this.welcomeTitleFadeOut = config.getInt("feedback.welcome.title-fade-out", 25);
        this.rewardMoneyMultiplier = config.getDouble("progression.reward-multipliers.money", 1.0);
        this.rewardSkillXpMultiplier = config.getDouble("progression.reward-multipliers.skill-xp", 1.0);
        this.rewardCivsSkillXpMultiplier = config.getDouble("progression.reward-multipliers.civs-skill-xp", 1.0);
        this.rewardSummaryHeader = config.getString("messages.reward-summary.header",
                "<gold>★ Recompensas recebidas</gold>");

        this.rebirthEnabled = config.getBoolean("progression.rebirth.enabled", true);
        java.util.List<String> capstones = config.getStringList("progression.rebirth.capstone-quests");
        this.rebirthCapstoneIds = capstones.isEmpty()
                ? java.util.List.of("warrior_champion", "construtor_mestre", "mercador_mestre")
                : java.util.List.copyOf(capstones);
        this.rebirthEssenceRefundPercent = config.getDouble("progression.rebirth.essence-refund-percent", 0.6);
        this.pathEssencePerTier = config.getInt("progression.path-essence-per-tier", 10);
        this.pathTraits = loadPathTraits(config.getConfigurationSection("progression.path-traits"));
        this.huntSpawnPartyRadius = config.getDouble("progression.hunt-spawn.party-radius", 32.0);
        this.huntSpawnCooldownSeconds = config.getLong("progression.hunt-spawn.cooldown-seconds", 300L);
        this.dailyRotationPool = java.util.List.copyOf(config.getStringList("quests.rotation.daily-pool"));
        this.weeklyRotationPool = java.util.List.copyOf(config.getStringList("quests.rotation.weekly-pool"));
        this.dailyRotationCount = config.getInt("quests.rotation.daily-count", 0);
        this.weeklyRotationCount = config.getInt("quests.rotation.weekly-count", 0);
    }

    private static java.util.Map<String, PathTraitConfig> loadPathTraits(
            org.bukkit.configuration.ConfigurationSection section) {
        java.util.Map<String, PathTraitConfig> traits = new java.util.LinkedHashMap<>();
        if (section == null) {
            return traits;
        }
        for (String key : section.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection trait = section.getConfigurationSection(key);
            if (trait == null) {
                continue;
            }
            org.bukkit.configuration.ConfigurationSection buff = trait.getConfigurationSection("buff");
            org.bukkit.configuration.ConfigurationSection debuff = trait.getConfigurationSection("debuff");
            traits.put(key.toLowerCase(java.util.Locale.ROOT), new PathTraitConfig(
                    buff == null ? null : buff.getString("stat"),
                    buff == null ? 0 : buff.getDouble("value", 0),
                    buff == null ? "add" : buff.getString("operation", "add"),
                    debuff == null ? null : debuff.getString("stat"),
                    debuff == null ? 0 : debuff.getDouble("value", 0),
                    debuff == null ? "add" : debuff.getString("operation", "add")));
        }
        return traits;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isCivsEnabled() {
        return civsEnabled;
    }

    public boolean isAuraSkillsEnabled() {
        return auraSkillsEnabled;
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    public boolean isRequireEconomy() {
        return requireEconomy;
    }

    public boolean isPlaceholderEnabled() {
        return placeholderEnabled;
    }

    public boolean isLuckPermsEnabled() {
        return luckPermsEnabled;
    }

    public boolean isChestShopEnabled() {
        return chestShopEnabled;
    }

    public boolean isEssentialsEnabled() {
        return essentialsEnabled;
    }

    public boolean isEssentialsKitRewardsEnabled() {
        return essentialsKitRewardsEnabled;
    }

    public boolean isEssentialsWarpRewardsEnabled() {
        return essentialsWarpRewardsEnabled;
    }

    public boolean isGuideBookOnJoin() {
        return guideBookOnJoin;
    }

    public boolean isVeinMinerEnabled() {
        return veinMinerEnabled;
    }

    public String getQuestPermissionPrefix() {
        return questPermissionPrefix;
    }

    public int getAutosaveMinutes() {
        return autosaveMinutes;
    }

    public boolean isSyncOnJoinFromCivs() {
        return syncOnJoinFromCivs;
    }

    public int getMaxActiveQuests() {
        return maxActiveQuests;
    }

    /** Id of the neutral onboarding quest offered to new players (config {@code quests.starter-quest-id}). */
    public String getStarterQuestId() {
        return starterQuestId;
    }

    public boolean isAllowAbandon() {
        return allowAbandon;
    }

    public String getQuestResetTimezone() {
        return questResetTimezone;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public String getReloadSuccessMessage() {
        return reloadSuccessMessage;
    }

    public boolean isQuestNotificationsEnabled() {
        return questNotificationsEnabled;
    }

    public boolean isQuestBossBarEnabled() {
        return questBossBarEnabled;
    }

    public String getQuestObjectiveActionBar() {
        return questObjectiveActionBar;
    }

    public String getQuestObjectiveTitle() {
        return questObjectiveTitle;
    }

    public String getQuestObjectiveSubtitle() {
        return questObjectiveSubtitle;
    }

    public String getQuestCompleteActionBar() {
        return questCompleteActionBar;
    }

    public String getQuestCompleteTitle() {
        return questCompleteTitle;
    }

    public String getQuestCompleteSubtitle() {
        return questCompleteSubtitle;
    }

    public String getQuestBossBarTitle() {
        return questBossBarTitle;
    }

    public String getQuestAcceptSuccess() {
        return questAcceptSuccess;
    }

    public String getQuestAcceptNotFound() {
        return questAcceptNotFound;
    }

    public String getQuestAcceptAlreadyComplete() {
        return questAcceptAlreadyComplete;
    }

    public String getQuestAcceptAlreadyStarted() {
        return questAcceptAlreadyStarted;
    }

    public String getQuestAcceptLocked() {
        return questAcceptLocked;
    }

    public String getQuestAcceptNoPermission() {
        return questAcceptNoPermission;
    }

    public String getQuestAcceptMaxActive() {
        return questAcceptMaxActive;
    }

    public String getQuestAcceptArchetypeLocked() {
        return questAcceptArchetypeLocked;
    }

    public String getQuestTrackSuccess() {
        return questTrackSuccess;
    }

    public String getQuestTrackFailed() {
        return questTrackFailed;
    }

    public int getQuestProgressNotifyInterval() {
        return questProgressNotifyInterval;
    }

    public String getQuestProgressActionBar() {
        return questProgressActionBar;
    }

    public String getQuestJournalLockedPrerequisite() {
        return questJournalLockedPrerequisite;
    }

    public String getQuestJournalLockedPermission() {
        return questJournalLockedPermission;
    }

    public String getQuestJournalLockedGeneric() {
        return questJournalLockedGeneric;
    }

    public String getQuestJournalTrackedBadge() {
        return questJournalTrackedBadge;
    }

    public String getQuestJournalTrackHint() {
        return questJournalTrackHint;
    }

    public String getQuestJournalAlreadyTracked() {
        return questJournalAlreadyTracked;
    }

    public String getQuestJournalAcceptHint() {
        return questJournalAcceptHint;
    }

    public String getQuestJournalAbandonHint() {
        return questJournalAbandonHint;
    }

    public String getQuestJournalCompletedHint() {
        return questJournalCompletedHint;
    }

    public String getQuestJournalPageIndicator() {
        return questJournalPageIndicator;
    }

    public String getQuestSyncSuccessAll() {
        return questSyncSuccessAll;
    }

    public String getQuestSyncSuccessOne() {
        return questSyncSuccessOne;
    }

    public String getHubTitle() {
        return hubTitle;
    }

    public String getHubItemName() {
        return hubItemName;
    }

    public String getHubItemLore() {
        return hubItemLore;
    }

    public String getHubGranted() {
        return hubGranted;
    }

    public String getHubRefreshed() {
        return hubRefreshed;
    }

    public String getHubInventoryFull() {
        return hubInventoryFull;
    }

    public String getHubAlreadyHave() {
        return hubAlreadyHave;
    }

    public String getHubTabInicio() {
        return hubTabInicio;
    }

    public String getHubTabCivs() {
        return hubTabCivs;
    }

    public String getHubTabRpg() {
        return hubTabRpg;
    }

    public String getHubTabConfig() {
        return hubTabConfig;
    }

    public String getHubTabQuests() {
        return hubTabQuests;
    }

    public String getHubFooterRefresh() {
        return hubFooterRefresh;
    }

    public String getHubFooterBack() {
        return hubFooterBack;
    }

    public String getHubFooterTrack() {
        return hubFooterTrack;
    }

    public String getHubFooterSync() {
        return hubFooterSync;
    }

    public String getHubFooterClose() {
        return hubFooterClose;
    }

    public String getHubFooterJournal() {
        return hubFooterJournal;
    }

    public String getHubInicioProfileTitle() {
        return hubInicioProfileTitle;
    }

    public String getHubInicioChoosePath() {
        return hubInicioChoosePath;
    }

    public String getHubInicioChoosePathLore() {
        return hubInicioChoosePathLore;
    }

    public String getHubInicioNextQuest() {
        return hubInicioNextQuest;
    }

    public String getHubInicioNextQuestLore() {
        return hubInicioNextQuestLore;
    }

    public String getHubInicioNoQuest() {
        return hubInicioNoQuest;
    }

    public String getHubCivsTownTitle() {
        return hubCivsTownTitle;
    }

    public String getHubCivsTownLore() {
        return hubCivsTownLore;
    }

    public String getHubCivsLocationsTitle() {
        return hubCivsLocationsTitle;
    }

    public String getHubCivsLocationsLore() {
        return hubCivsLocationsLore;
    }

    public String getHubCivsTownInfoTitle() {
        return hubCivsTownInfoTitle;
    }

    public String getHubCivsTownInfoLore() {
        return hubCivsTownInfoLore;
    }

    public String getHubCivsRegionsTitle() {
        return hubCivsRegionsTitle;
    }

    public String getHubCivsRegionsLore() {
        return hubCivsRegionsLore;
    }

    public String getHubCivsAuctionTitle() {
        return hubCivsAuctionTitle;
    }

    public String getHubCivsAuctionLore() {
        return hubCivsAuctionLore;
    }

    public String getHubCivsSpellsTitle() {
        return hubCivsSpellsTitle;
    }

    public String getHubCivsSpellsLore() {
        return hubCivsSpellsLore;
    }

    public String getHubCivsFarmsTitle() {
        return hubCivsFarmsTitle;
    }

    public String getHubCivsFarmsLore() {
        return hubCivsFarmsLore;
    }

    public String getHubCivsCombatTitle() {
        return hubCivsCombatTitle;
    }

    public String getHubCivsCombatLore() {
        return hubCivsCombatLore;
    }

    public String getHubCivsChestShopTitle() {
        return hubCivsChestShopTitle;
    }

    public String getHubCivsChestShopLore() {
        return hubCivsChestShopLore;
    }

    public String getHubRpgJournalTitle() {
        return hubRpgJournalTitle;
    }

    public String getHubRpgJournalLore() {
        return hubRpgJournalLore;
    }

    public String getHubRpgQuestTreeTitle() {
        return hubRpgQuestTreeTitle;
    }

    public String getHubRpgQuestTreeLore() {
        return hubRpgQuestTreeLore;
    }

    public String getHubRpgPerksTitle() {
        return hubRpgPerksTitle;
    }

    public String getHubRpgPerksLore() {
        return hubRpgPerksLore;
    }

    public String getHubRpgPerkSummaryTitle() {
        return hubRpgPerkSummaryTitle;
    }

    public String getHubRpgPerkSummaryLore() {
        return hubRpgPerkSummaryLore;
    }

    public String getHubRpgDailyTitle() {
        return hubRpgDailyTitle;
    }

    public String getHubRpgWeeklyTitle() {
        return hubRpgWeeklyTitle;
    }

    public String getHubRpgScheduleAvailable() {
        return hubRpgScheduleAvailable;
    }

    public String getHubRpgScheduleDone() {
        return hubRpgScheduleDone;
    }

    public String getHubRpgScheduleLocked() {
        return hubRpgScheduleLocked;
    }

    public String getHubRpgScheduleNone() {
        return hubRpgScheduleNone;
    }

    public String getHubRpgScheduleHint() {
        return hubRpgScheduleHint;
    }

    public String getHubRpgProfileTitle() {
        return hubRpgProfileTitle;
    }

    public String getHubRpgProfileLore() {
        return hubRpgProfileLore;
    }

    public String getHubConfigNotificationsTitle() {
        return hubConfigNotificationsTitle;
    }

    public String getHubConfigNotificationsLore() {
        return hubConfigNotificationsLore;
    }

    public String getHubConfigBossBarTitle() {
        return hubConfigBossBarTitle;
    }

    public String getHubConfigBossBarLore() {
        return hubConfigBossBarLore;
    }

    public String getHubConfigHintTitle() {
        return hubConfigHintTitle;
    }

    public String getHubConfigHintLore() {
        return hubConfigHintLore;
    }

    public String getHubQuestsOpenJournalTitle() {
        return hubQuestsOpenJournalTitle;
    }

    public String getHubQuestsOpenJournalLore() {
        return hubQuestsOpenJournalLore;
    }

    public String getSettingsNotificationsOn() {
        return settingsNotificationsOn;
    }

    public String getSettingsNotificationsOff() {
        return settingsNotificationsOff;
    }

    public String getSettingsBossBarOn() {
        return settingsBossBarOn;
    }

    public String getSettingsBossBarOff() {
        return settingsBossBarOff;
    }

    public double getRewardMoneyMultiplier() {
        return rewardMoneyMultiplier;
    }

    public double getRewardSkillXpMultiplier() {
        return rewardSkillXpMultiplier;
    }

    public double getRewardCivsSkillXpMultiplier() {
        return rewardCivsSkillXpMultiplier;
    }

    public String getRewardSummaryHeader() {
        return rewardSummaryHeader;
    }

    public String getQuestJournalChainNext() {
        return questJournalChainNext;
    }

    public String getQuestJournalChainRequires() {
        return questJournalChainRequires;
    }

    public boolean isDailyCtaEnabled() {
        return dailyCtaEnabled;
    }

    public String getDailyCtaTitle() {
        return dailyCtaTitle;
    }

    public String getDailyCtaSubtitle() {
        return dailyCtaSubtitle;
    }

    public String getQuestAcceptMessage(QuestAcceptResult result) {
        return switch (result) {
            case SUCCESS -> questAcceptSuccess;
            case NOT_FOUND -> questAcceptNotFound;
            case ALREADY_COMPLETE -> questAcceptAlreadyComplete;
            case ALREADY_STARTED -> questAcceptAlreadyStarted;
            case LOCKED -> questAcceptLocked;
            case NO_PERMISSION -> questAcceptNoPermission;
            case MAX_ACTIVE -> questAcceptMaxActive;
            case ARCHETYPE_LOCKED -> questAcceptArchetypeLocked;
        };
    }

    public TrackedHudMode getTrackedHudMode() {
        return trackedHudMode;
    }

    public TransientHudChannel getTransientHudChannel() {
        return transientHudChannel;
    }

    public boolean isComposedHudEnabled() {
        return composedHudEnabled;
    }

    public int getComposedHudIntervalTicks() {
        return composedHudIntervalTicks;
    }

    public String getComposedHudFormat() {
        return composedHudFormat;
    }

    public boolean isHideVanillaHeartsEnabled() {
        return hideVanillaHeartsEnabled;
    }

    public int getHideVanillaHeartsHttpPort() {
        return hideVanillaHeartsHttpPort;
    }

    public String getHideVanillaHeartsHost() {
        return hideVanillaHeartsHost;
    }

    public String getHideVanillaHeartsUrl() {
        return hideVanillaHeartsUrl;
    }

    public boolean isHideVanillaHeartsForce() {
        return hideVanillaHeartsForce;
    }

    public String getHideVanillaHeartsPrompt() {
        return hideVanillaHeartsPrompt;
    }

    public boolean isQuestProgressPulse() {
        return questProgressPulse;
    }

    public String getQuestProgressSound() {
        return questProgressSound;
    }

    public float getQuestProgressSoundVolume() {
        return questProgressSoundVolume;
    }

    public float getQuestProgressSoundPitch() {
        return questProgressSoundPitch;
    }

    public String getQuestObjectiveCompleteSound() {
        return questObjectiveCompleteSound;
    }

    public float getQuestObjectiveCompleteSoundVolume() {
        return questObjectiveCompleteSoundVolume;
    }

    public float getQuestObjectiveCompleteSoundPitch() {
        return questObjectiveCompleteSoundPitch;
    }

    public boolean isQuestObjectiveCompleteParticles() {
        return questObjectiveCompleteParticles;
    }

    public int getQuestObjectiveTitleFadeIn() {
        return questObjectiveTitleFadeIn;
    }

    public int getQuestObjectiveTitleStay() {
        return questObjectiveTitleStay;
    }

    public int getQuestObjectiveTitleFadeOut() {
        return questObjectiveTitleFadeOut;
    }

    public String getQuestCompleteSound() {
        return questCompleteSound;
    }

    public float getQuestCompleteSoundVolume() {
        return questCompleteSoundVolume;
    }

    public float getQuestCompleteSoundPitch() {
        return questCompleteSoundPitch;
    }

    public boolean isQuestCompleteFirework() {
        return questCompleteFirework;
    }

    public boolean isQuestCompleteParticles() {
        return questCompleteParticles;
    }

    public int getQuestCompleteTitleFadeIn() {
        return questCompleteTitleFadeIn;
    }

    public int getQuestCompleteTitleStay() {
        return questCompleteTitleStay;
    }

    public int getQuestCompleteTitleFadeOut() {
        return questCompleteTitleFadeOut;
    }

    public String getJournalOpenSound() {
        return journalOpenSound;
    }

    public float getJournalOpenSoundPitch() {
        return journalOpenSoundPitch;
    }

    public String getJournalAcceptSound() {
        return journalAcceptSound;
    }

    public float getJournalAcceptSoundPitch() {
        return journalAcceptSoundPitch;
    }

    public String getJournalTrackSound() {
        return journalTrackSound;
    }

    public float getJournalTrackSoundPitch() {
        return journalTrackSoundPitch;
    }

    public String getJournalClickSound() {
        return journalClickSound;
    }

    public float getJournalSoundVolume() {
        return journalSoundVolume;
    }

    public boolean isWelcomeEnabled() {
        return welcomeEnabled;
    }

    public String getWelcomeTitle() {
        return welcomeTitle;
    }

    public String getWelcomeSubtitle() {
        return welcomeSubtitle;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getWelcomeSound() {
        return welcomeSound;
    }

    public float getWelcomeSoundVolume() {
        return welcomeSoundVolume;
    }

    public float getWelcomeSoundPitch() {
        return welcomeSoundPitch;
    }

    public boolean isWelcomeGiveHubItem() {
        return welcomeGiveHubItem;
    }

    public int getWelcomeTitleFadeIn() {
        return welcomeTitleFadeIn;
    }

    public int getWelcomeTitleStay() {
        return welcomeTitleStay;
    }

    public int getWelcomeTitleFadeOut() {
        return welcomeTitleFadeOut;
    }

    public boolean isRebirthEnabled() {
        return rebirthEnabled;
    }

    public java.util.List<String> getRebirthCapstoneIds() {
        return rebirthCapstoneIds;
    }

    public double getRebirthEssenceRefundPercent() {
        return rebirthEssenceRefundPercent;
    }

    public int getPathEssencePerTier() {
        return pathEssencePerTier;
    }

    public PathTraitConfig getPathTrait(String archetype) {
        if (archetype == null) {
            return null;
        }
        return pathTraits.get(archetype.toLowerCase(java.util.Locale.ROOT));
    }

    public double getHuntSpawnPartyRadius() {
        return huntSpawnPartyRadius;
    }

    public long getHuntSpawnCooldownSeconds() {
        return huntSpawnCooldownSeconds;
    }

    public java.util.List<String> getDailyRotationPool() {
        return dailyRotationPool;
    }

    public java.util.List<String> getWeeklyRotationPool() {
        return weeklyRotationPool;
    }

    public int getDailyRotationCount() {
        return dailyRotationCount;
    }

    public int getWeeklyRotationCount() {
        return weeklyRotationCount;
    }
}
