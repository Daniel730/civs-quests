package dev.daniel730.rpgserver.util;

import dev.daniel730.rpgserver.config.PluginConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;

public final class MessageUtil {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final PluginConfig config;

    public MessageUtil(PluginConfig config) {
        this.config = config;
    }

    public void send(CommandSender sender, String message) {
        sender.sendMessage(parse(config.getMessagePrefix() + message));
    }

    public Component parse(String message) {
        return miniMessage.deserialize(message);
    }

    public void sendActionBar(Player player, String message) {
        player.sendActionBar(parse(message));
    }

    public void sendTitle(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeInTicks * 50L),
                Duration.ofMillis(stayTicks * 50L),
                Duration.ofMillis(fadeOutTicks * 50L));
        player.showTitle(Title.title(
                title.isBlank() ? Component.empty() : parse(title),
                subtitle.isBlank() ? Component.empty() : parse(subtitle),
                times));
    }
}
