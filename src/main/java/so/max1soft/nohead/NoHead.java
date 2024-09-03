package so.max1soft.nohead;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class NoHead implements Listener {
   private final JavaPlugin plugin;
   private final FileConfiguration config;

   public NoHead(JavaPlugin plugin) {
      this.plugin = plugin;
      this.config = plugin.getConfig();
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      if (event.getBlock().getType() == Material.PLAYER_HEAD ||
              event.getBlock().getType() == Material.CREEPER_HEAD ||
              event.getBlock().getType() == Material.ZOMBIE_HEAD ||
              event.getBlock().getType() == Material.SKELETON_SKULL ||
              event.getBlock().getType() == Material.WITHER_SKELETON_SKULL ||
              event.getBlock().getType() == Material.PLAYER_WALL_HEAD||
              event.getBlock().getType() == Material.DRAGON_HEAD) {
         event.setCancelled(true);
      }
   }
}
