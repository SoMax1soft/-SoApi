package so.max1soft.border;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BorderRtpFix implements Listener {
   private final JavaPlugin plugin;
   private final FileConfiguration config;

   public BorderRtpFix(JavaPlugin plugin) {
      this.plugin = plugin;
      this.config = plugin.getConfig();
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      Location to = event.getTo();
      World world = to.getWorld();

      // Check if the player is in the "world" world
      if (!"world".equals(world.getName())) {
         return;
      }

      double borderSize = world.getWorldBorder().getSize() / 2.0;
      if (Math.abs(to.getX()) > borderSize || Math.abs(to.getZ()) > borderSize) {
         if (this.config.contains("Utils.border.teleportCoordinates.world")
                 && this.config.contains("Utils.border.teleportCoordinates.x")
                 && this.config.contains("Utils.border.teleportCoordinates.y")
                 && this.config.contains("Utils.border.teleportCoordinates.z")) {
            String worldName = this.config.getString("Utils.border.teleportCoordinates.world");
            double x = this.config.getDouble("Utils.border.teleportCoordinates.x");
            double y = this.config.getDouble("Utils.border.teleportCoordinates.y");
            double z = this.config.getDouble("Utils.border.teleportCoordinates.z");
            Location teleportLocation = new Location(Bukkit.getWorld(worldName), x, y, z);
            if (teleportLocation.getWorld() != null) {
               player.teleport(teleportLocation);
               Bukkit.getScheduler()
                       .runTaskLater(
                               this.plugin,
                               () -> player.sendTitle(this.config.getString("Utils-Message.border.title.first", ""), this.config.getString("Utils-Message.border.title.second", ""), 10, 40, 10),
                               25L
                       );
            } else {
               this.plugin.getLogger().warning(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "Мир не найден: " + worldName);
            }
         } else {
            this.plugin.getLogger().warning(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "Координаты не указаны в конфиге!");
         }
      }
   }
}
