package so.max1soft.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class automessage implements Listener {
    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public automessage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Показ тайтла при входе на сервер
        player.sendTitle(config.getString("Utils-Message.jointitle"), config.getString("Utils-Message.joinsubtitle"), 10, 70, 20);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Добавляем задержку перед показом тайтла, чтобы игрок успел возродиться
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("SoAPI"), () -> {
            player.sendTitle(config.getString("Utils-Message.revivetitle"), config.getString("Utils-Message.revivesubtitle"), 10, 70, 20);
        }, 10L); // Задержка в 20 тиков (1 секунда)
    }
}
