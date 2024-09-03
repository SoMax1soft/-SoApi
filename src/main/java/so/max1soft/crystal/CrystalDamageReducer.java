package so.max1soft.crystal;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import so.max1soft.Main;

public class CrystalDamageReducer implements Listener {
   @EventHandler
   public void onEntityDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager().getType() == EntityType.ENDER_CRYSTAL) {
         FileConfiguration config = Main.getInstance().getConfig();
         double reductionPercentage = config.getDouble("Utils.crystal.reduce", 0.0) / 100.0;
         double reducedDamage = event.getDamage() * (1.0 - reductionPercentage);
         event.setDamage(reducedDamage);
      }
   }
}
