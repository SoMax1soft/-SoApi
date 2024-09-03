package so.max1soft.fix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import so.max1soft.Main; // Assuming Main is your main plugin class

public class RegionLength implements Listener {
   @NonNull
   private int regionsNameLengthLimit;
   private List<String> regionCommands = new ArrayList<>(Arrays.asList("region", "rg", "regions"));
   private List<String> createCommands = new ArrayList<>(Arrays.asList("claim", "create"));
   private final Main plugin;
   private org.bukkit.configuration.file.FileConfiguration config;

   public RegionLength(Main plugin) {
      this.plugin = plugin;
      this.config = plugin.getConfig();
   }

   @EventHandler(priority = EventPriority.NORMAL)
   public void onRegionCreate(PlayerCommandPreprocessEvent event) {
      String message = event.getMessage();
      String[] args = message.replace("/", "").split(" ");
      if (args.length >= 3 && this.regionCommands.contains(args[0].toLowerCase()) && this.createCommands.contains(args[1].toLowerCase())) {
         String regionName = args[2];
         if (regionName.length() > plugin.getConfig().getInt("Utils.fix.length")) {
            event.getPlayer().sendMessage(plugin.getConfig().getString(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "Utils-Message.error-region-length"));
            event.setCancelled(true);
         }
      }
   }
}
