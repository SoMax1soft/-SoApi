package so.max1soft.fix;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityMountEvent;

public class NoMountEvent implements Listener {
   @EventHandler
   public void onEntityMount(EntityMountEvent event) {
      if (event.getMount() instanceof Horse || event.getMount() instanceof Pig || event.getMount() instanceof Ocelot) {
         event.setCancelled(true);
         if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            player.sendMessage("Вы не можете садиться на этот вид животного!");
         }
      }
   }
}
