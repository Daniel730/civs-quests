package dev.daniel730.rpgserver.hud;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.daniel730.rpgserver.RpgServerPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Serves a minimal resource pack that blanks vanilla heart sprites so the composed
 * ActionBar can own HP/mana display. Hunger is untouched.
 */
public final class HideHeartsPackService implements Listener {

    private static final String PACK_RESOURCE = "resource-packs/hide-vanilla-hearts.zip";
    private static final UUID PACK_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private final RpgServerPlugin plugin;
    private HttpServer httpServer;
    private byte[] packBytes;
    private byte[] packHash;
    private String packUrl;
    private boolean listenerRegistered;

    public HideHeartsPackService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        if (!plugin.getPluginConfig().isHideVanillaHeartsEnabled()) {
            return;
        }
        try {
            preparePackFile();
            startHttpIfNeeded();
            resolveUrl();
            if (!listenerRegistered) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                listenerRegistered = true;
            }
            plugin.getLogger().info("Pacote hide-hearts ativo: " + packUrl);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Falha ao ativar pacote hide-hearts: " + ex.getMessage(), ex);
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (packUrl == null || packBytes == null) {
            return;
        }
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendPack(player), 40L);
    }

    public void sendPack(Player player) {
        if (player == null || !player.isOnline() || packUrl == null || packHash == null) {
            return;
        }
        Component prompt = MiniMessage.miniMessage().deserialize(
                plugin.getPluginConfig().getHideVanillaHeartsPrompt());
        boolean force = plugin.getPluginConfig().isHideVanillaHeartsForce();
        player.setResourcePack(PACK_UUID, packUrl, packHash, prompt, force);
    }

    private void preparePackFile() throws Exception {
        Path dir = plugin.getDataFolder().toPath().resolve("resource-packs");
        Files.createDirectories(dir);
        Path zip = dir.resolve("hide-vanilla-hearts.zip");
        try (InputStream in = plugin.getResource(PACK_RESOURCE)) {
            if (in != null) {
                Files.copy(in, zip, StandardCopyOption.REPLACE_EXISTING);
            } else if (!Files.exists(zip)) {
                // Fall back to sibling checkout / deploy path if jar resource missing.
                Path external = Path.of("resource-packs", "hide-vanilla-hearts.zip");
                if (Files.exists(external)) {
                    Files.copy(external, zip, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    throw new IOException("hide-vanilla-hearts.zip not found in jar or " + external);
                }
            }
        }
        packBytes = Files.readAllBytes(zip);
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        packHash = sha1.digest(packBytes);
        plugin.getLogger().info("hide-hearts sha1=" + HexFormat.of().formatHex(packHash));
    }

    private void startHttpIfNeeded() throws IOException {
        String configured = plugin.getPluginConfig().getHideVanillaHeartsUrl();
        if (configured != null && !configured.isBlank()) {
            return;
        }
        int port = plugin.getPluginConfig().getHideVanillaHeartsHttpPort();
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/hide-vanilla-hearts.zip", this::servePack);
        httpServer.setExecutor(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpg-hide-hearts-http");
            t.setDaemon(true);
            return t;
        }));
        httpServer.start();
    }

    private void servePack(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        exchange.getResponseHeaders().add("Content-Type", "application/zip");
        exchange.sendResponseHeaders(200, packBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(packBytes);
        }
    }

    private void resolveUrl() {
        String configured = plugin.getPluginConfig().getHideVanillaHeartsUrl();
        if (configured != null && !configured.isBlank()) {
            packUrl = configured.trim();
            return;
        }
        String host = plugin.getPluginConfig().getHideVanillaHeartsHost();
        if (host == null || host.isBlank()) {
            host = Bukkit.getIp();
            if (host == null || host.isBlank() || "0.0.0.0".equals(host)) {
                host = "127.0.0.1";
            }
        }
        int port = plugin.getPluginConfig().getHideVanillaHeartsHttpPort();
        packUrl = "http://" + host + ":" + port + "/hide-vanilla-hearts.zip";
    }
}
