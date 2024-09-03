package so.max1soft.fix;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;




public class RegionInfo implements Listener {


   private final CooldownManager cooldownManager = new CooldownManager();

   @EventHandler
   public void onLeatherInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      ItemStack item = event.getItem();
      if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)
         && item != null
         && item.getType() == Material.LEATHER) {
         if (!this.cooldownManager.isOnCooldown(player)) {
            this.cooldownManager.setCooldown(player, 15);
         } else {
            event.setCancelled(true);
            player.sendMessage("Нельзя так часто проверять регионы.");
         }
      }

   }


   public static class CooldownManager {
      private final Map<Player, Long> cooldowns = new HashMap<>();

      private CooldownManager() {
      }

      boolean isOnCooldown(Player player) {
         return this.cooldowns.containsKey(player) && System.currentTimeMillis() < this.cooldowns.get(player);
      }

      void setCooldown(Player player, int seconds) {
         this.cooldowns.put(player, System.currentTimeMillis() + (long)(seconds * 1000));
      }
   }

}
