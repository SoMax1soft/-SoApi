package so.max1soft.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import so.max1soft.Main;

import java.io.File;
import java.io.IOException;

public class DynamicSlot implements Listener {

    private final Main plugin;
    private int playerLimit;
    private File dynamicConfigFile;
    private FileConfiguration dynamicConfig;

    public DynamicSlot(Main plugin) {
        this.plugin = plugin;
        this.playerLimit = Bukkit.getMaxPlayers(); // Initialize with default server max players
        loadDynamicConfig(); // Load dynamic configuration on plugin startup
        Bukkit.getPluginManager().registerEvents(this, plugin); // Register events for PlayerLoginEvent
    }

    public void setPlayerLimit(CommandSender sender, int newLimit) {
        FileConfiguration config = plugin.getConfig();
        if (config == null) {
            sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + ChatColor.RED + "Ошибка: Конфигурация плагина не загружена.");
            return;
        }

        String limitSetMessage = config.getString("Utils-Message.limit-set").replace("&", "§") + newLimit;
        if (limitSetMessage == null) {
            sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + ChatColor.RED + "Ошибка: Сообщение для установки лимита не найдено в конфигурации.");
            return;
        }

        try {
            playerLimit = newLimit;
            dynamicConfig.set("player-limit", playerLimit);
            saveDynamicConfig();
            sender.sendMessage(String.format(limitSetMessage, playerLimit)); // Send message with formatted player limit
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "Неверное число: " + newLimit);
        }
    }



    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        FileConfiguration config = plugin.getConfig();
        if (!event.getPlayer().hasPermission("slots.bypass")) {
            if (Bukkit.getOnlinePlayers().size() >= playerLimit) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, config.getString(plugin.getConfig().getString("Utils-Message.kick-whitelist").replace("&", "§") + "messages.server-full"));
            }
        }
    }

    private void loadDynamicConfig() {
        dynamicConfigFile = new File(plugin.getDataFolder(), "dynamicslots.yml");
        if (!dynamicConfigFile.exists()) {
            dynamicConfigFile.getParentFile().mkdirs();
            plugin.saveResource("dynamicslots.yml", false);
        }

        dynamicConfig = YamlConfiguration.loadConfiguration(dynamicConfigFile);
        if (!dynamicConfig.contains("player-limit")) {
            dynamicConfig.set("player-limit", Bukkit.getMaxPlayers());
            saveDynamicConfig();
        }

        playerLimit = dynamicConfig.getInt("player-limit", Bukkit.getMaxPlayers());
    }

    private void saveDynamicConfig() {
        if (dynamicConfig != null && dynamicConfigFile != null) {
            try {
                dynamicConfig.save(dynamicConfigFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
