package so.max1soft.spawnutils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import so.max1soft.Main;

public class TeleportOnJoin implements Listener {
   private final Main plugin;

   public TeleportOnJoin(Main plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      long secondsPlayed = this.getSecondsPlayed(player);
      if (Main.getInstance().getConfig().getBoolean("Utils.teleportonjoin.enable") && Main.getInstance().getConfig().getBoolean("Utils.teleportonjoin.teleport.enabled")) {
         double x = this.plugin.getConfig().getDouble("Utils.teleportonjoin.teleport.location.x");
         double y = this.plugin.getConfig().getDouble("Utils.teleportonjoin.teleport.location.y");
         double z = this.plugin.getConfig().getDouble("Utils.teleportonjoin.teleport.location.z");
         float yaw = (float)this.plugin.getConfig().getDouble("Utils.teleportonjoin.teleport.location.yaw");
         String worldName = this.plugin.getConfig().getString("Utils.teleportonjoin.teleport.location.world");
         if (Bukkit.getWorld(worldName) != null) {
            Location teleportLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, 0.0F);
            player.teleport(teleportLocation);
         } else {
            this.plugin.getLogger().warning("Мир '" + worldName + "' не существует!");
         }
      }
   }

   private long getSecondsPlayed(Player player) {
      String placeholder = "%statistic_seconds_played%";
      String result = PlaceholderAPI.setPlaceholders(player, placeholder);

      try {
         return Long.parseLong(result);
      } catch (NumberFormatException var5) {
         return 0L;
      }
   }

   private void saveDataConfig() {
      this.plugin.saveConfig();
   }
}
