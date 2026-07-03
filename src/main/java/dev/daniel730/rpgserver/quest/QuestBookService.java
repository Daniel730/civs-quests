package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class QuestBookService {

    private static final int OBJECTIVES_PER_PAGE = 4;

    private final RpgServerPlugin plugin;

    public QuestBookService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void grantOnQuestStart(Player player, Quest quest) {
        if (!plugin.getPluginConfig().isQuestBookAutoGrant()) {
            return;
        }
        giveQuestBook(player, quest);
        plugin.getMessageUtil().send(player,
                "<gray>Livro de quest recebido:</gray> <white>" + quest.getName() + "</white>");
    }

    public ItemStack buildQuestBook(Player player, Quest quest) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);

        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.title(Component.text(truncatePlain(quest.getName(), 32), NamedTextColor.GOLD));
        meta.author(Component.text("RPG Server", NamedTextColor.DARK_GRAY));
        meta.pages(buildPages(player, profile, quest, status, questManager));
        item.setItemMeta(meta);
        return item;
    }

    public void giveQuestBook(Player player, Quest quest) {
        ItemStack book = buildQuestBook(player, quest);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(book);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), book);
            plugin.getMessageUtil().send(player,
                    "<yellow>Inventário cheio — livro dropado no chão.</yellow>");
        }
    }

    public void openQuestBook(Player player, Quest quest) {
        player.openBook(buildQuestBook(player, quest));
    }

    public Quest resolveQuest(Player player, String questId) {
        QuestManager questManager = plugin.getQuestManager();
        if (questId != null && !questId.isBlank()) {
            return questManager.getQuest(questId);
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        if (profile.getTrackedQuestId() != null) {
            Quest tracked = questManager.getQuest(profile.getTrackedQuestId());
            if (tracked != null && !profile.isQuestComplete(tracked.getId())) {
                return tracked;
            }
        }
        return questManager.findPrimaryActiveQuest(profile);
    }

    private List<Component> buildPages(Player player, PlayerProfile profile, Quest quest,
                                       QuestManager.QuestStatus status, QuestManager questManager) {
        List<Component> pages = new ArrayList<>();
        pages.add(buildHeaderPage(quest, status, questManager.getQuestProgress(profile, quest)));

        List<Quest.Objective> objectives = quest.getObjectives();
        for (int i = 0; i < objectives.size(); i += OBJECTIVES_PER_PAGE) {
            pages.add(buildObjectivesPage(profile, quest, objectives, i,
                    Math.min(i + OBJECTIVES_PER_PAGE, objectives.size())));
        }

        if (!quest.getRewards().isEmpty()) {
            pages.add(buildRewardsPage(quest.getRewards()));
        }

        pages.add(buildActionsPage(player, profile, quest, status));
        return pages;
    }

    private Component buildHeaderPage(Quest quest, QuestManager.QuestStatus status,
                                      QuestManager.QuestProgress progress) {
        Component page = Component.text(quest.getName(), NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.newline())
                .append(Component.text("Status: ", NamedTextColor.GRAY))
                .append(Component.text(status.getDisplay(), statusColor(status)))
                .append(Component.newline());

        if (quest.getArchetype() != null && !quest.getArchetype().isBlank()) {
            page = page.append(Component.text("Arquetipo: ", NamedTextColor.GRAY))
                    .append(Component.text(quest.getArchetype(), NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        if (quest.isScheduled()) {
            page = page.append(Component.text("Tipo: ", NamedTextColor.GRAY))
                    .append(Component.text(quest.getSchedule().displayName(), NamedTextColor.LIGHT_PURPLE))
                    .append(Component.newline());
        }
        if (progress.total() > 0) {
            page = page.append(Component.text("Progresso: ", NamedTextColor.GRAY))
                    .append(Component.text(progress.completed() + "/" + progress.total(), NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        if (quest.getDescription() != null && !quest.getDescription().isBlank()) {
            page = page.append(Component.newline())
                    .append(Component.text(quest.getDescription(), NamedTextColor.WHITE));
        }
        return page;
    }

    private Component buildObjectivesPage(PlayerProfile profile, Quest quest,
                                          List<Quest.Objective> objectives, int from, int to) {
        Component page = Component.text("Objetivos", NamedTextColor.YELLOW, TextDecoration.BOLD)
                .append(Component.newline())
                .append(Component.newline());
        for (int i = from; i < to; i++) {
            Quest.Objective objective = objectives.get(i);
            boolean done = profile.isObjectiveComplete(quest.getId(), objective.getId());
            NamedTextColor markColor = done ? NamedTextColor.GREEN : NamedTextColor.RED;
            String mark = done ? "✓ " : "○ ";
            page = page.append(Component.text(mark, markColor))
                    .append(Component.text(objective.getDescription(), NamedTextColor.GRAY));
            if (!done && objective.isCountBased()) {
                int current = profile.getObjectiveProgress(quest.getId(), objective.getId());
                page = page.append(Component.text(" (" + current + "/" + objective.getAmount() + ")",
                        NamedTextColor.DARK_GRAY));
            }
            page = page.append(Component.newline());
        }
        return page;
    }

    private Component buildRewardsPage(RewardDefinition rewards) {
        Component page = Component.text("Recompensas", NamedTextColor.GREEN, TextDecoration.BOLD)
                .append(Component.newline())
                .append(Component.newline());
        if (rewards.getMoney() > 0) {
            page = page.append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text(rewards.getMoney() + " moedas", NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        for (Map.Entry<String, Double> entry : rewards.getSkillXp().entrySet()) {
            page = page.append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text("+" + entry.getValue().intValue() + " XP em "
                            + entry.getKey(), NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        if (rewards.getPermission() != null) {
            page = page.append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text("Desbloqueio: " + rewards.getPermission(), NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        if (rewards.getEssentialsKit() != null) {
            page = page.append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text("Kit: " + rewards.getEssentialsKit(), NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        if (rewards.getWarp() != null) {
            page = page.append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text("Warp: " + rewards.getWarp(), NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        return page;
    }

    private Component buildActionsPage(Player player, PlayerProfile profile, Quest quest,
                                       QuestManager.QuestStatus status) {
        Component page = Component.text("Ações", NamedTextColor.AQUA, TextDecoration.BOLD)
                .append(Component.newline())
                .append(Component.newline());

        String questId = quest.getId();
        if (status == QuestManager.QuestStatus.NOT_STARTED) {
            page = page.append(clickableAction("Aceitar", "/rpg quest accept " + questId,
                    "Iniciar esta quest", NamedTextColor.GREEN))
                    .append(Component.newline());
        } else if (status == QuestManager.QuestStatus.IN_PROGRESS) {
            boolean tracked = questId.equals(profile.getTrackedQuestId());
            if (!tracked) {
                page = page.append(clickableAction("Rastrear", "/rpg quest track " + questId,
                        "Definir como quest rastreada", NamedTextColor.YELLOW))
                        .append(Component.newline());
            } else {
                page = page.append(Component.text("★ Quest rastreada", NamedTextColor.GOLD))
                        .append(Component.newline());
            }
        } else if (status == QuestManager.QuestStatus.LOCKED) {
            page = page.append(Component.text("Quest bloqueada.", NamedTextColor.RED))
                    .append(Component.newline());
            QuestManager questManager = plugin.getQuestManager();
            if (!questManager.meetsRequirements(profile, quest)) {
                for (String requiredId : quest.getRequiredQuestIds()) {
                    Quest required = questManager.getQuest(requiredId);
                    String name = required != null ? required.getName() : requiredId;
                    boolean done = profile.isQuestComplete(requiredId);
                    NamedTextColor color = done ? NamedTextColor.GREEN : NamedTextColor.RED;
                    page = page.append(Component.text((done ? "✓ " : "✗ ") + name, color))
                            .append(Component.newline());
                }
            }
            if (!plugin.getLuckPermsHook().hasQuestPermission(player, questId)) {
                page = page.append(Component.text("Requer permissão de desbloqueio.", NamedTextColor.RED))
                        .append(Component.newline());
            }
        } else {
            page = page.append(Component.text("Quest concluída!", NamedTextColor.GREEN))
                    .append(Component.newline());
        }

        page = page.append(Component.newline())
                .append(clickableAction("Atualizar", "/rpg quest book " + questId,
                        "Receber livro atualizado", NamedTextColor.AQUA))
                .append(Component.newline())
                .append(clickableAction("Diário", "/rpg journal",
                        "Abrir diário de quests", NamedTextColor.GRAY));
        return page;
    }

    private Component clickableAction(String label, String command, String hover, NamedTextColor color) {
        return Component.text("[" + label + "]", color, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(hover, NamedTextColor.WHITE)));
    }

    private NamedTextColor statusColor(QuestManager.QuestStatus status) {
        return switch (status) {
            case IN_PROGRESS -> NamedTextColor.GREEN;
            case NOT_STARTED -> NamedTextColor.YELLOW;
            case LOCKED -> NamedTextColor.RED;
            case COMPLETED -> NamedTextColor.AQUA;
        };
    }

    private String truncatePlain(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 3) + "...";
    }
}
