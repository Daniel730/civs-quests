package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.util.ArchetypeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Single persistent player guide — tabbed Civs + RPG help, dynamic status, per-player settings.
 * Opens via {@code /rpg guide} or right-click on the inventory guide book item.
 */
public final class PlayerGuideBookService {

    public static final String GUIDE_BOOK_MARKER = "rpg-guide-book";

    private static final int PAGE_INICIO = 1;
    private static final int PAGE_CIVS = 2;
    private static final int PAGE_RPG = 3;
    private static final int PAGE_CONFIG = 4;

    private final RpgServerPlugin plugin;

    public PlayerGuideBookService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGuide(Player player) {
        player.openBook(buildGuideBook(player));
        plugin.getQuestFeedbackService().playJournalOpen(player);
    }

    public void giveGuideBook(Player player) {
        if (hasGuideBookInInventory(player)) {
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getGuideBookAlreadyHave());
            return;
        }
        ItemStack book = createGuideBookItem();
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(book);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), book);
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getGuideBookInventoryFull());
        } else {
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getGuideBookGranted());
        }
    }

    public void openOrGiveGuide(Player player) {
        if (!hasGuideBookInInventory(player)) {
            ItemStack book = createGuideBookItem();
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(book);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), book);
            }
        }
        openGuide(player);
    }

    public void refreshGuide(Player player) {
        openGuide(player);
        plugin.getMessageUtil().send(player, plugin.getPluginConfig().getGuideBookRefreshed());
    }

    public boolean hasGuideBookInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isGuideBookItem(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean isGuideBookItem(ItemStack item) {
        if (item == null || item.getType() != Material.WRITTEN_BOOK || !item.hasItemMeta()) {
            return false;
        }
        BookMeta meta = (BookMeta) item.getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }
        String firstLine = PlainTextComponentSerializer.plainText().serialize(lore.getFirst());
        return GUIDE_BOOK_MARKER.equals(firstLine);
    }

    public ItemStack createGuideBookItem() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.title(Component.text("Guia do Reino", NamedTextColor.GOLD));
        meta.author(Component.text("RPG Server", NamedTextColor.DARK_GRAY));
        meta.displayName(plugin.getMessageUtil().parse("<gold><italic>Guia do Reino</italic></gold>"));
        meta.lore(List.of(
                Component.text(GUIDE_BOOK_MARKER, NamedTextColor.DARK_GRAY),
                plugin.getMessageUtil().parse("<gray>Clique direito ou </gray><yellow>/rpg guide</yellow>")
        ));
        meta.pages(List.of(Component.text("Abra com clique direito\nou /rpg guide")));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack buildGuideBook(Player player) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.title(Component.text("Guia do Reino", NamedTextColor.GOLD));
        meta.author(Component.text("RPG Server", NamedTextColor.DARK_GRAY));
        meta.pages(buildPages(player));
        item.setItemMeta(meta);
        return item;
    }

    private List<Component> buildPages(Player player) {
        List<Component> pages = new ArrayList<>();
        pages.add(buildInicioPage(player));
        pages.add(buildCivsPage());
        pages.add(buildRpgPage(player));
        pages.add(buildConfigPage(player));
        return pages;
    }

    private Component buildInicioPage(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();

        Component page = Component.text("Guia do Reino", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.newline())
                .append(Component.text("━━━━━━━━━━━━", NamedTextColor.DARK_GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Bem-vindo, aventureiro!", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.newline());

        String archetype = profile.getArchetype();
        if (archetype == null || archetype.isBlank()) {
            page = page.append(Component.text("Caminho: ", NamedTextColor.GRAY))
                    .append(Component.text("Não escolhido", NamedTextColor.YELLOW))
                    .append(Component.newline())
                    .append(Component.text("Escolha Guerreiro, Mercador ou Construtor no diário.", NamedTextColor.GRAY))
                    .append(Component.newline());
        } else {
            page = page.append(Component.text("Caminho: ", NamedTextColor.GRAY))
                    .append(plugin.getMessageUtil().parse(ArchetypeUtil.coloredDisplayName(archetype)))
                    .append(Component.newline());
        }

        Quest tracked = questManager.findTrackedQuest(profile);
        if (tracked != null) {
            page = page.append(Component.text("Rastreando: ", NamedTextColor.GRAY))
                    .append(Component.text(tracked.getName(), NamedTextColor.AQUA))
                    .append(Component.newline());
        }

        String nextQuest = questManager.formatNextQuestName(player, profile);
        if (!"Nenhuma".equals(nextQuest)) {
            page = page.append(Component.text("Próxima missão: ", NamedTextColor.GRAY))
                    .append(Component.text(nextQuest, NamedTextColor.WHITE))
                    .append(Component.newline());
        }

        page = page.append(Component.newline())
                .append(Component.text("Seções", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.newline())
                .append(tabLink("Civs (/cv)", PAGE_CIVS, "Towns, regiões, leilão"))
                .append(Component.newline())
                .append(tabLink("RPG", PAGE_RPG, "Diário, quests, perks"))
                .append(Component.newline())
                .append(tabLink("Configurações", PAGE_CONFIG, "Notificações e HUD"))
                .append(Component.newline())
                .append(Component.newline())
                .append(clickableAction("Abrir Diário", "/rpg journal",
                        "Escolher caminho e gerenciar quests", NamedTextColor.GREEN));

        return page;
    }

    private Component buildCivsPage() {
        Component page = Component.text("Civs — Território", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.newline())
                .append(Component.text("Comando: /cv", NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Menu principal", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("Use /cv menu para abrir o painel Civs: cidades, regiões, magias, leilão e farms.", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Cidades & Regiões", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("• Construa abrigos e edifícios com itens Civs no inventário.", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("• Aceite convites de cidade para expandir território.", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("• Regiões dão bônus: farms, armazéns, altares.", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Leilão & Economia", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("• Leilão Civs: /cv menu → Casa de Leilões.", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("• Lojas de jogadores: placas ChestShop.", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Combate & Mobs", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("• Magias no altar (/cv menu → Magias).", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("• Mobs customizados em eventos de combate.", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.newline())
                .append(clickableAction("Menu Civs", "/cv menu", "Abrir menu territorial", NamedTextColor.GREEN))
                .append(Component.newline())
                .append(tabLink("← Início", PAGE_INICIO, "Voltar ao índice"));

        return page;
    }

    private Component buildRpgPage(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();

        Component page = Component.text("RPG — Aventuras", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Diário de Quests", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("Use /rpg journal para ver missões do seu caminho, diárias e semanais.", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Caminhos (escolha única)", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("Guerreiro — combate, magias, caça.", NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.text("Mercador — lojas, leilão, riqueza.", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Component.text("Construtor — regiões, cidades, obras.", NamedTextColor.GREEN))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Rastreamento", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("Clique numa quest no diário para rastrear. Boss bar e scoreboard mostram progresso.", NamedTextColor.GRAY))
                .append(Component.newline());

        Optional<Quest> next = questManager.findNextAvailableQuest(player, profile);
        if (next.isPresent()) {
            page = page.append(Component.newline())
                    .append(Component.text("Sugestão: ", NamedTextColor.GRAY))
                    .append(Component.text(next.get().getName(), NamedTextColor.WHITE))
                    .append(Component.newline());
        }

        page = page.append(Component.newline())
                .append(Component.text("Recompensas", NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("Moedas, XP AuraSkills, perks únicos por caminho. Sem duplicar progresso.", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(clickableAction("Abrir Diário", "/rpg journal", "Ver e aceitar quests", NamedTextColor.GREEN))
                .append(Component.newline())
                .append(clickableAction("Ver Perks", "/rpg perks", "Perks desbloqueados", NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(tabLink("← Início", PAGE_INICIO, "Voltar ao índice"));

        return page;
    }

    private Component buildConfigPage(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean notifications = plugin.getQuestFeedbackService().isNotificationsEnabled(profile);
        boolean bossBar = plugin.getQuestFeedbackService().isBossBarEnabled(profile);

        Component page = Component.text("Configurações", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Preferências pessoais (salvas no seu perfil):", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Notificações: ", NamedTextColor.WHITE))
                .append(toggleState(notifications))
                .append(Component.newline())
                .append(clickableAction(notifications ? "Desligar" : "Ligar",
                        "/rpg settings notifications toggle",
                        "Alternar sons e títulos de quest", NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Boss bar / HUD: ", NamedTextColor.WHITE))
                .append(toggleState(bossBar))
                .append(Component.newline())
                .append(clickableAction(bossBar ? "Desligar" : "Ligar",
                        "/rpg settings bossbar toggle",
                        "Alternar barra de progresso da quest rastreada", NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Comandos úteis:", NamedTextColor.AQUA))
                .append(Component.newline())
                .append(Component.text("/rpg profile — ver arquétipo", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("/rpg quest list — listar quests", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(clickableAction("Atualizar guia", "/rpg guide refresh",
                        "Recarregar páginas com dados atuais", NamedTextColor.AQUA))
                .append(Component.newline())
                .append(tabLink("← Início", PAGE_INICIO, "Voltar ao índice"));

        return page;
    }

    private Component toggleState(boolean enabled) {
        return Component.text(enabled ? "Ligado" : "Desligado",
                enabled ? NamedTextColor.GREEN : NamedTextColor.RED);
    }

    private Component tabLink(String label, int page, String hover) {
        return Component.text("[" + label + "]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.changePage(page))
                .hoverEvent(HoverEvent.showText(Component.text(hover, NamedTextColor.WHITE)));
    }

    private Component clickableAction(String label, String command, String hover, NamedTextColor color) {
        return Component.text("[" + label + "]", color, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(hover, NamedTextColor.WHITE)));
    }
}
