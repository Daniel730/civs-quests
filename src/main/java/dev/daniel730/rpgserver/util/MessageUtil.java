package dev.daniel730.rpgserver.util;

import dev.daniel730.rpgserver.config.PluginConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

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
}
