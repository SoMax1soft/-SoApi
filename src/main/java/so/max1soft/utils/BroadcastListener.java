package so.max1soft.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class BroadcastListener implements Listener, CommandExecutor {

    private HashMap<UUID, Long> cooldowns = new HashMap<>();
    private JavaPlugin plugin;

    public BroadcastListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommand() {
        plugin.getCommand("ytb").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ytb")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("ytb.use")) {
                    UUID playerUUID = player.getUniqueId();
                    long currentTime = System.currentTimeMillis();

                    if (cooldowns.containsKey(playerUUID)) {
                        long timeLeft = (cooldowns.get(playerUUID) - currentTime) / 1000;
                        if (timeLeft > 0) {
                            player.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + ChatColor.RED + "Вы не можете использовать эту команду еще " + timeLeft + " секунд.");
                            return true;
                        }
                    }

                    if (args.length == 0) {
                        player.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + ChatColor.RED + "Пожалуйста, введите сообщение для отправки.");
                        return false;
                    }


                    cooldowns.put(playerUUID, currentTime + (2 * 60 * 60 * 1000));


                    String message = plugin.getConfig().getString("Utils-Message.yt-prefix").replace("&", "§") + String.join(" ", args);
                    message = ChatColor.translateAlternateColorCodes('&', message);


                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(message);
                    }

                    return true;
                } else {
                    player.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + ChatColor.RED + "У вас нет прав на использование этой команды.");
                    return true;
                }
            } else {
                sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "Эту команду может использовать только игрок.");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        onCommand(event.getPlayer(), null, "ytb", event.getMessage().split("\\s+"));
        event.setCancelled(true);
    }
}
