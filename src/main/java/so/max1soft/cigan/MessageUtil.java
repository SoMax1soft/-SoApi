package so.max1soft.cigan;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;

public class  MessageUtil {

    public static void sendMessage(Player player, Collection<String> messages) {
        if (player != null && messages != null) {
            messages.forEach(message -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', message)));
        }
    }
    public static void sendAll(Collection<String> messages) {
        for (Player player :Bukkit.getOnlinePlayers()) {
            if (messages != null) {
                messages.forEach(message -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', message)));
            }
        }
    }
    public static void sendMessage(Player player, Collection<String> messages, String... placeholders) {
        if (player != null && messages != null) {
            messages.forEach(message -> {
                String replacedMessage = message;
                for (int i = 0; i < placeholders.length; i += 2) {
                    replacedMessage = replacedMessage.replace(placeholders[i], placeholders[i + 1]);
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));
            });
        }
    }
    public static String sendConsoleCommand(Collection<String> messages, String... placeholders) {
        String replacedMessage = null;
        for (String message : messages) {
            replacedMessage = message;
            for (int i = 0; i < placeholders.length; i += 2) {
                replacedMessage = replacedMessage.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return replacedMessage;
    }
}

