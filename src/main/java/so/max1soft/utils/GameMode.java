package so.max1soft.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GameMode implements Listener {
   private final JavaPlugin plugin;
   private final FileConfiguration config;

   public GameMode(JavaPlugin plugin) {
      this.plugin = plugin;
      this.config = plugin.getConfig();
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();

         if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            player.sendMessage(this.config.getString("Utils-Message.gamemode-message"));
         }

   }
}
